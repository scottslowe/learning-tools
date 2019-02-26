console.log('Loading function');

// dependencies
var AWS = require('aws-sdk');
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
			if ('Item' in data) {
				var verified = data.Item.verified.BOOL;
				var verifyToken = null;
				if (!verified) {
					verifyToken = data.Item.verifyToken.S;
				}
				fn(null, verified, verifyToken);
			} else {
				fn(null, null); // User not found
			}
		}
	});
}

function updateUser(email, fn) {
	dynamodb.updateItem({
			TableName: config.DDB_TABLE,
			Key: {
				email: {
					S: email
				}
			},
			AttributeUpdates: {
				verified: {
					Action: 'PUT',
					Value: {
						BOOL: true
					}
				},
				verifyToken: {
					Action: 'DELETE'
				}
			}
		},
		fn);
}

exports.handler = (event, context, callback) => {
	var email = event.email;
	var verifyToken = event.verify;

	getUser(email, function(err, verified, correctToken) {
		if (err) {
			callback('Error in getUser: ' + err);
		} else if (verified) {
			console.log('User already verified: ' + email);
			callback(null, { verified: true });
		} else if (verifyToken == correctToken) {
			// User verified
			updateUser(email, function(err, data) {
				if (err) {
					callback('Error in updateUser: ' + err);
				} else {
					console.log('User verified: ' + email);
					callback(null, { verified: true });
				}
			});
		} else {
			// Wrong token, not verified
			console.log('User not verified: ' + email);
			callback(null, { verified: false });
		}
	});
}
