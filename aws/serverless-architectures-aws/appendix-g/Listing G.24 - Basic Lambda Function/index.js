/**
 * Created by Peter Sbarski
 * Serverless Architectures on AWS
 * http://book.acloud.guru/
 * Last Updated: Feb 12, 2017
 */

var AWS = require('aws-sdk');

exports.handler = function(event, context, callback) {
  var message = process.env.HELLO_SAM;
  callback(null, message);
}
