console.log('Loading function');

// dependencies
var AWS = require('aws-sdk');
var crypto = require('crypto');
var config = require('./config.json');

// Get reference to AWS clients
var dynamodb = new AWS.DynamoDB();
var ses = new AWS.SES();

function getUser(email, fn) {
	dynamodb.getItem({
		TableName: config.DDB_TABLE,
		Key: {
			email: {
				S: email
			}
		}
	}, function(err, data) {
		if (err) return fn(err);
		else {
			if ('Item' in data) {
				fn(null, email);
			} else {
				fn(null, null); // User not found
			}
		}
	});
}

function storeLostToken(email, fn) {
	// Bytesize
	var len = 128;
	crypto.randomBytes(len, function(err, token) {
		if (err) return fn(err);
		token = token.toString('hex');
		dynamodb.updateItem({
				TableName: config.DDB_TABLE,
				Key: {
					email: {
						S: email
					}
				},
				AttributeUpdates: {
					lostToken: {
						Action: 'PUT',
						Value: {
							S: token
						}
					}
				}
			},
		 function(err, data) {
			if (err) return fn(err);
			else fn(null, token);
		});
	});
}

function sendLostPasswordEmail(email, token, fn) {
	var subject = 'Password Lost for ' + config.EXTERNAL_NAME;
	var lostLink = config.RESET_PAGE + '?email=' + email + '&lost=' + token;
	ses.sendEmail({
		Source: config.EMAIL_SOURCE,
		Destination: {
			ToAddresses: [
				email
			]
		},
		Message: {
			Subject: {
				Data: subject
			},
			Body: {
				Html: {
					Data: '<html><head>'
					+ '<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />'
					+ '<title>' + subject + '</title>'
					+ '</head><body>'
					+ 'Please <a href="' + lostLink + '">click here to reset your password</a> or copy & paste the following link in a browser:'
					+ '<br><br>'
					+ '<a href="' + lostLink + '">' + lostLink + '</a>'
					+ '</body></html>'
				}
			}
		}
	}, fn);
}

exports.handler = (event, context, callback) => {
	var email = event.email;

	getUser(email, function(err, emailFound) {
		if (err) {
			callback('Error in getUserFromEmail: ' + err);
		} else if (!emailFound) {
			console.log('User not found: ' + email);
			callback(null, { sent: false });
		} else {
			storeLostToken(email, function(err, token) {
				if (err) {
					callback('Error in storeLostToken: ' + err);
				} else {
					sendLostPasswordEmail(email, token, function(err, data) {
						if (err) {
							callback('Error in sendLostPasswordEmail: ' + err);
						} else {
							console.log('User found: ' + email);
							callback(null, { sent: true });
						}
					});
				}
			});
		}
	});
}
