console.log('Loading function');

// dependencies
var AWS = require('aws-sdk');
var cryptoUtils = require('./lib/cryptoUtils');
var config = require('./config');

// Get reference to AWS clients
var dynamodb = new AWS.DynamoDB();

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
			if (('Item' in data) && ('lostToken' in data.Item)) {
				var lostToken = data.Item.lostToken.S;
				fn(null, lostToken);
			} else {
				fn(null, null); // User or token not found
			}
		}
	});
}

function updateUser(email, password, salt, fn) {
	dynamodb.updateItem({
			TableName: config.DDB_TABLE,
			Key: {
				email: {
					S: email
				}
			},
			AttributeUpdates: {
				passwordHash: {
					Action: 'PUT',
					Value: {
						S: password
					}
				},
				passwordSalt: {
					Action: 'PUT',
					Value: {
						S: salt
					}
				},
				lostToken: {
					Action: 'DELETE'
				}
			}
		},
		fn);
}

exports.handler = (event, context, callback) => {
	var email = event.email;
	var lostToken = event.lost;
	var newPassword = event.password;

	getUser(email, function(err, correctToken) {
		if (err) {
			callback('Error in getUser: ' + err);
		} else if (!correctToken) {
			console.log('No lostToken for user: ' + email);
			callback(null, { changed: false });
		} else if (lostToken != correctToken) {
			// Wrong token, no password lost
			console.log('Wrong lostToken for user: ' + email);
			callback(null, { changed: false });
		} else {
			console.log('User logged in: ' + email);
			cryptoUtils.computeHash(newPassword, function(err, newSalt, newHash) {
				if (err) {
					callback('Error in computeHash: ' + err);
				} else {
					updateUser(email, newHash, newSalt, function(err, data) {
						if (err) {
							callback('Error in updateUser: ' + err);
						} else {
							console.log('User password changed: ' + email);
							callback(null, { changed: true });
						}
					});
				}
			});
		}
	});
}
