/**
 * Serverless Architectures on AWS
 * http://book.acloud.guru/
 * Last Updated: Feb 12, 2017
 */

'use strict';

module.exports.endpoint = (event, context, callback) => {
 const response = {
   statusCode: 200,
   body: JSON.stringify({
     message: 'Hello, the current time is ${new Date().toTimeString()}.'
   }),
 };

 callback(null, response);
};
