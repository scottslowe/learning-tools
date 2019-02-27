/**
 * Serverless Architectures on AWS
 * http://book.acloud.guru/
 * Last Updated: Feb 12, 2017
 */

const db = require('db').connect();
const mailer = require('mailer');
const users = require('users')(db, mailer);

module.exports.saveUser = (event, context, callback) => {
 users.save(event.email, callback); 
};
