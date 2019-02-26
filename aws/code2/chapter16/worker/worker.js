var express = require('express');
var bodyParser = require('body-parser');
var AWS = require('aws-sdk');
var assert = require('assert-plus');
var Jimp = require('jimp');
var fs = require('fs');

var lib = require('./lib.js');

var db = new AWS.DynamoDB({
  'region': 'us-east-1'
});
var s3 = new AWS.S3({
  'region': 'us-east-1'
});

var app = express();
app.use(bodyParser.json());

function getImage(id, cb) {
  db.getItem({
    'Key': {
      'id': {
        'S': id
      }
    },
    'TableName': 'imagery-image'
  }, function(err, data) {
    if (err) {
      cb(err);
    } else {
      if (data.Item) {
        cb(null, lib.mapImage(data.Item));
      } else {
        cb(new Error('image not found'));
      }
    }
  });
}

app.get('/', function(request, response) {
  response.json({});
});

app.post('/sqs', function(request, response) {
  assert.string(request.body.imageId, 'imageId');
  assert.string(request.body.desiredState, 'desiredState');
  getImage(request.body.imageId, function(err, image) {
    if (err) {
      throw err;
    } else {
      if (typeof states[request.body.desiredState] === 'function') {
        states[request.body.desiredState](image, request, response);
      } else {
        throw new Error('unsupported desiredState');
      }
    }
  });
});

var states = {
  'processed': processed
};

function processImage(image, cb) {
  var processedS3Key = 'processed/' + image.id + '-' + Date.now() + '.png';
  var rawFile = './tmp_raw_' + image.id;
  var processedFile = './tmp_processed_' + image.id;
  s3.getObject({
    'Bucket': process.env.ImageBucket,
    'Key': image.rawS3Key
  }, function(err, data) {
    if (err) {
      cb(err);
    } else {
      fs.writeFile(rawFile, data.Body, {'encoding': null}, function(err) {
        if (err) {
          cb(err);
        } else {
          Jimp.read(rawFile, (err, lenna) => {
            if (err) {
              throw err;
            } else {
              lenna.sepia().write(processedFile);
              fs.unlink(rawFile, function() {
                fs.readFile(processedFile, {'encoding': null}, function(err, buf) {
                  if (err) {
                    cb(err);
                  } else {
                    s3.putObject({
                      'Bucket': process.env.ImageBucket,
                      'Key': processedS3Key,
                      'ACL': 'public-read',
                      'Body': buf,
                      'ContentType': 'image/png'
                    }, function(err) {
                      if (err) {
                        cb(err);
                      } else {
                        fs.unlink(processedFile, function() {
                          cb(null, processedS3Key);
                        });
                      }
                    });
                  }
                });
              });
            }
          });
        }
      });
    }
  });
}

function processed(image, request, response) {
  processImage(image, function(err, processedS3Key) {
    if (err) {
      throw err;
    } else {
      db.updateItem({
        'Key': {
          'id': {
            'S': image.id
          }
        },
        'UpdateExpression': 'SET #s=:newState, version=:newVersion, processedS3Key=:processedS3Key',
        'ConditionExpression': 'attribute_exists(id) AND version=:oldVersion AND #s IN (:stateUploaded, :stateProcessed)',
        'ExpressionAttributeNames': {
          '#s': 'state'
        },
        'ExpressionAttributeValues': {
          ':newState': {
            'S': 'processed'
          },
          ':oldVersion': {
            'N': image.version.toString()
          },
          ':newVersion': {
            'N': (image.version + 1).toString()
          },
          ':processedS3Key': {
            'S': processedS3Key
          },
          ':stateUploaded': {
            'S': 'uploaded'
          },
          ':stateProcessed': {
            'S': 'processed'
          }
        },
        'ReturnValues': 'ALL_NEW',
        'TableName': 'imagery-image'
      }, function(err, data) {
        if (err) {
          throw err;
        } else {
          response.json(lib.mapImage(data.Attributes));
        }
      });
    }
  });
}

app.listen(process.env.PORT || 8080, function() {
  console.log('Worker started on port ' + (process.env.PORT || 8080));
});
