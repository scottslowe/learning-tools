/**
 * Created by Peter Sbarski
 * Serverless Architectures on AWS
 * http://book.acloud.guru/
 * Last Updated: Feb 12, 2017
 */

'use strict';

var AWS = require('aws-sdk');
var async = require('async');

var s3 = new AWS.S3();

function createBucketParams(next) {
  var params = {
    Bucket: process.env.BUCKET,
    EncodingType: 'url'
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

function createList(data, next) {
  var urls = [];
  for (var i = 0; i < data.Contents.length; i++) {
    var file = data.Contents[i];

    if (file.Key && file.Key.substr(-3, 3) === 'mp4') {
      urls.push(file);
    }
  }

  var result = {
    baseUrl: process.env.BASE_URL,
    bucket: process.env.BUCKET,
    urls: urls
  }

  next(null, result);
}

exports.handler = function(event, context, callback){
  async.waterfall([createBucketParams, getVideosFromBucket, createList],
    function (err, result) {
      if (err) {
        callback(err);
      } else {
        callback(null, result);
      }
  });
};
