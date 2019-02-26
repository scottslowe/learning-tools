console.log('Loading function');

var AWS = require('aws-sdk');
var dynamodb = new AWS.DynamoDB();
var s3 = new AWS.S3();

var S3_BUCKET = '<BUCKET>';
var ITEMS_TABLE = '<DYNAMODB_TABLE>';

function uploadToS3(params) {
		s3.putObject(params, function(err, data) {
      if (err) console.log(err);
      else console.log(data);
		});
}

function indexContent(dynamodb_params, s3_params) {
    var content = [];
    dynamodb.query(dynamodb_params, function(err, data) {
        if (err) {
          console.log(err, err.stack);
        } else {
          data.Items.forEach((item) => {
            console.log(item);
            content.push({
                identityId: item.identityId.S,
                objectKey: item.objectKey.S,
                thumbnailKey: item.thumbnailKey.S,
                uploadDate: item.uploadDate.S,
                title: item.title.S,
                description: item.description.S
            });
          });
          s3_params.Body = JSON.stringify(content);
          uploadToS3(s3_params);
        }
    });
}

function indexPublicContent(day) {
  console.log('Getting public content for ' + day);
  var dynamodb_params = {
    TableName: ITEMS_TABLE,
    IndexName: 'uploadDay-uploadDate-index',
    Limit: 100,
    ScanIndexForward: false,
    KeyConditionExpression: 'uploadDay = :uploadDayVal',
    FilterExpression: 'isPublic = :isPublicVal',
    ExpressionAttributeValues: {
      ':uploadDayVal' : { S: day },
      ':isPublicVal' : { BOOL: true }
    }
  };
  var s3_params = {
  	Bucket: S3_BUCKET,
    Key: 'public/index/content.json',
    ContentType: 'application/json'
  };
  indexContent(dynamodb_params, s3_params);
}

function indexPrivateContent(identityId) {
  console.log('Getting private content for ' + identityId);
  var dynamodb_params = {
    TableName: ITEMS_TABLE,
    ScanIndexForward: false,
    KeyConditionExpression: 'identityId = :identityIdVal',
    FilterExpression: 'isPublic = :isPublicVal',
    ExpressionAttributeValues: {
      ':identityIdVal' : { S: identityId },
      ':isPublicVal' : { BOOL: false }
    }
  };
  var s3_params = {
    Bucket: S3_BUCKET,
    Key: 'private/index/' + identityId + '/content.json',
    ContentType: 'application/json'
  };
  indexContent(dynamodb_params, s3_params);
}

exports.handler = (event, context, callback) => {
    //console.log('Received event:', JSON.stringify(event, null, 2));
    var uploadDays = {};
    var identityIds = {};
    event.Records.forEach((record) => {
        console.log(record.eventID);
        console.log(record.eventName);
        console.log('DynamoDB Record: %j', record.dynamodb);
        var image;
        if ('NewImage' in record.dynamodb) {
          image = record.dynamodb.NewImage;
        } else if ('OldImage' in record.dynamodb) {
          image = record.dynamodb.OldImage;
        } else {
          console.log('Unknown event format: ' + record);
        }
        if ('isPublic' in image && image.isPublic.BOOL && 'uploadDay' in image) {
          var uploadDay = image.uploadDay.S;
          uploadDays[uploadDay] = true;
          console.log('Public content found for ' + uploadDay);
        } else {
          var identityId = record.dynamodb.Keys.identityId.S;
          identityIds[identityId] = true;
          console.log('Private content found for ' + identityId);
        }
    });
    var latestUploadDay = Object.keys(uploadDays).sort().pop();
    if (latestUploadDay) {
      indexPublicContent(latestUploadDay);
    }
    Object.keys(identityIds).forEach((identityId) => {
      indexPrivateContent(identityId);
    });
};
