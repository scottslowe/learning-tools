/**
 * Serverless Architectures on AWS
 * http://book.acloud.guru/
 * Last Updated: Feb 12, 2017
 */

module.exports.log = (event, context, callback) => {
 console.log(event);
 callback(null, {});
};
