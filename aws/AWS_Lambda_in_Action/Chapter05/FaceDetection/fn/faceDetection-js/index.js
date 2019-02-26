var cv = require('opencv');
var util = require('util');
var request = require('request').defaults({ encoding: null });
var uuid = require('node-uuid');
var AWS = require('aws-sdk');

var s3 = new AWS.S3();

var dstBucket = 'danilop-eventdrivenapps';
var dstPrefix = 'tmp/';
var outputDomain = 'eventdrivenapps.com';

function getFormattedDate() {
  var now = new Date().toISOString(); // YYYY-MM-DDTHH:mm:ss.sssZ
  var formattedNow = now.substr(0,4) + now.substr(5,2) + now.substr(8,2)
    + now.substr(11,2) + now.substr(14,2) + now.substr(17,2);
  return formattedNow;
}

exports.handler = (event, context, callback) => {
  console.log("Reading options from event:\n", util.inspect(event, {depth: 5}));
  var imageUrl = event.imageUrl;
  request.get(imageUrl, function (err, res, body) {
    if (err) {
      console.log(err);
      callback(err);
    }
    cv.readImage(body, function(err, im) {
      if (err) {
        console.log(err);
        callback(err);
      }
      if (im.width() < 1 || im.height() < 1) callback('Image has no size');
      im.detectObject("node_modules/opencv/data/haarcascade_frontalface_alt.xml", {}, function(err, faces) {
        if (err) callback(err);
        for (var i = 0; i < faces.length; i++){
          var face = faces[i];
          im.rectangle([face.x, face.y], [face.width, face.height], [255, 255, 255], 2);
        }
        if (faces.length > 0) {
          var dstKey = dstPrefix + getFormattedDate() + '-' + uuid.v4() + '.jpg';
          var contentType = 'image/jpeg';
          s3.putObject({
            Bucket: dstBucket,
            Key: dstKey,
            Body: im.toBuffer(),
            ContentType: contentType
          }, function(err, data) {
            if (err) console.log(err);
            if (err) callback(err);
            console.log(data);
            outputUrl = 'https://' + outputDomain + '/' + dstKey;
            var result = {
              faces: faces.length,
              outputUrl: outputUrl
            };
            callback(null, result);
          });
        } else {
          var result = {
            faces: 0,
            outputUrl: imageUrl
          };
          callback(null, result);
        }
      });
    });
  });
}
