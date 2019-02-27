/**
 * Serverless Architectures on AWS
 * http://book.acloud.guru/
 * Last Updated: Feb 12, 2017
 */

module.exports.run = (event, context) => {
 const time = new Date();
 console.log(`Your cron function "${context.functionName}" ran at ${time}`);
};
