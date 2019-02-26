// dependencies
var async = require('async');
var AWS = require('aws-sdk');
var gm = require('gm')
            .subClass({ imageMagick: true }); // Enable ImageMagick integration.
var util = require('util');

// constants
var DEFAULT_MAX_WIDTH  = 200;
var DEFAULT_MAX_HEIGHT = 200;
var DDB_TABLE = 'images';

// get reference to AWS services client
var s3 = new AWS.S3();
var dynamodb = new AWS.DynamoDB();

function getImageType(key, callback) {
  var typeMatch = key.match(/\.([^.]*)$/);
  if (!typeMatch) {
      callback("Could not determine the image type for key: ${key}");
      return;
  }
  var imageType = typeMatch[1];
  if (imageType != "jpg" && imageType != "png") {
      callback('Unsupported image type: ${imageType}');
      return;
  }
  return imageType;
}

exports.handler = (event, context, callback) => {
  // Read options from the event.
  console.log("Reading options from event:\n", util.inspect(event, {depth: 5}));
  var srcBucket = event.Records[0].s3.bucket.name;
  var srcKey    = event.Records[0].s3.object.key;
  var dstBucket = srcBucket;
  var dstKey    = "thumbs/" + srcKey;

  var imageType = getImageType(srcKey, callback);

  // Download the image from S3, transform, upload to a different S3 bucket
  // and write the metadata to DynamoDB
  async.waterfall([
    function downloadImage(next) {
      // Download the image from S3 into a buffer.
      s3.getObject({
          Bucket: srcBucket,
          Key: srcKey
        },
        next);
      },
    function tranformImage(response, next) {
      gm(response.Body).size(function(err, size) {
        var metadata = response.Metadata;
        console.log("Metadata:\n", util.inspect(metadata, {depth: 5}));
        var max_width;
        if ('width' in metadata) {
          max_width = metadata.width;
        } else {
          max_width = DEFAULT_MAX_WIDTH;
        }
        var max_height;
        if ('height' in metadata) {
          max_height = metadata.height;
        } else {
          max_height = DEFAULT_MAX_HEIGHT;
        }
        // Infer the scaling factor to avoid stretching the image unnaturally.
        var scalingFactor = Math.min(
          max_width / size.width,
          max_height / size.height
        );
        var width  = scalingFactor * size.width;
        var height = scalingFactor * size.height;

        // Transform the image buffer in memory.
        this.resize(width, height)
          .toBuffer(imageType, function(err, buffer) {
            if (err) {
              next(err);
            } else {
              next(null, response.ContentType, metadata, buffer);
            }
          });
      });
    },
    function uploadThumbnail(contentType, metadata, data, next) {
      // Stream the transformed image to a different S3 bucket.
      s3.putObject({
          Bucket: dstBucket,
          Key: dstKey,
          Body: data,
          ContentType: contentType,
          Metadata: metadata
      }, function(err, buffer) {
        if (err) {
          next(err);
        } else {
          next(null, metadata);
        }
      });
    },
    function storeMetadata(metadata, next) {
      // adds metadata do DynamoDB
      var params = {
        TableName: DDB_TABLE,
        Item: {
          name: { S: srcKey },
          thumbnail: { S: dstKey },
          timestamp: { S: (new Date().toJSON()).toString() },
        }
      };
      if ('author' in metadata) {
        params.Item.author = { S: metadata.author };
      }
      if ('title' in metadata) {
        params.Item.title = { S: metadata.title };
      }
      if ('description' in metadata) {
        params.Item.description = { S: metadata.description };
      }
      dynamodb.putItem(params, next);
    }], function (err) {
      if (err) {
        console.error(err);
      } else {
        console.log(
          'Successfully resized ' + srcBucket + '/' + srcKey +
          ' and uploaded to ' + dstBucket + '/' + dstKey
        );
      }
      callback();
    }
  );
};
