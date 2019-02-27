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

exports.handler = function(event, context, callback){
    var body = JSON.parse(event.body);
    var urls = [];

    async.forEachOf(body, function(video, index, next) {
      s3.getSignedUrl('getObject', {Bucket: process.env.BUCKET, Key: video.key, Expires: 9000}, function(err, url) {
        if (err) {
          console.log('Error generating signed URL for', video.key);
          next(err);
        } else {
          urls.push({firebaseId: video.firebaseId, url: url});
          next();
        }
      });

    }, function (err) {
      if (err) {
        console.log('Could not generate signed URLs');
        callback(err);
      } else {
        console.log('Successfully generated URLs');

        var response = {
          'statusCode': 200,
          'headers' : {'Access-Control-Allow-Origin':'*'},
          'body' : JSON.stringify({'urls': urls})
        }

        callback(null, response);
      }
    });
}
