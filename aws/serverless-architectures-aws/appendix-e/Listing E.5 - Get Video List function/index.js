/**
 * Created by Peter Sbarski
 * Serverless Architectures on AWS
 * http://book.acloud.guru/
 * Last Updated: Feb 11, 2017
 */

'use strict';

var AWS = require('aws-sdk');
var async = require('async');

var s3 = new AWS.S3();

function createErrorResponse(code, message, encoding) {
  var result = {
    code: code,
    message: message,
    encoding: encoding
  };

  return JSON.stringify(result);
}

function createBucketParams(next) {
  var params = {
    Bucket: process.env.BUCKET
  };

  next(null, params);
}

function getVideosFromBucket(params, next) {
  s3.listObjects(params, function(err, data){
    if (err) {
      next(err);
    } else {
      next(null, data);
    }
  });
}

function createList(encoding, data, next) {
  var files = [];
  for (var i = 0; i < data.Contents.length; i++) {
    var file = data.Contents[i];

    if (encoding) {
      var type = file.Key.substr(file.Key.lastIndexOf('-') + 1);
      if (type !== encoding + '.mp4') {
        continue;
      }
    } else {
      if (file.Key.slice(-4) !== '.mp4') {
        continue;
      }
    }

    files.push(file);
  }

  var result = {
    baseUrl: process.env.BASE_URL,
    bucket: process.env.BUCKET,
    urls: files
  }

  next(null, result)
}

exports.handler = function(event, context, callback){
  var encoding = null;

  if (event.encoding) {
    encoding = decodeURIComponent(event.encoding);
  }

  async.waterfall([createBucketParams, getVideosFromBucket, async.apply(createList, encoding)],
    function (err, result) {
      if (err) {
        callback(createErrorResponse(500, err, event.encoding));
      } else {
        if (result.urls.length > 0) {
          callback(null, result);
        } else {
          callback(createErrorResponse(404, 'no files for the given encoding were found', event.encoding));
        }
      }
  });
};
