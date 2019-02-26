console.log('Loading function');

// dependencies
var AWS = require('aws-sdk');
var config = require('./config.json');
var cryptoUtils = require('./lib/cryptoUtils');

// Get reference to AWS clients
var dynamodb = new AWS.DynamoDB();
var cognitoidentity = new AWS.CognitoIdentity();

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
				var hash = data.Item.passwordHash.S;
				var salt = data.Item.passwordSalt.S;
				var verified = data.Item.verified.BOOL;
				fn(null, hash, salt, verified);
			} else {
				fn(null, null); // User not found
			}
		}
	});
}

function getToken(email, fn) {
	var param = {
		IdentityPoolId: config.IDENTITY_POOL_ID,
		Logins: {} // To have provider name in a variable
	};
	param.Logins[config.DEVELOPER_PROVIDER_NAME] = email;
	cognitoidentity.getOpenIdTokenForDeveloperIdentity(param,
		function(err, data) {
			if (err) return fn(err); // an error occurred
			else fn(null, data.IdentityId, data.Token); // successful response
		});
}

exports.handler = (event, context, callback) => {
	var email = event.email;
	var clearPassword = event.password;

	getUser(email, function(err, correctHash, salt, verified) {
		if (err) {
			callback('Error in getUser: ' + err);
		} else {
			if (correctHash == null) {
				// User not found
				console.log('User not found: ' + email);
				callback(null, { login: false	});
			} else if (!verified) {
				// User not verified
				console.log('User not verified: ' + email);
				callback(null, { login: false });
			} else {
				cryptoUtils.computeHash(clearPassword, salt, function(err, salt, hash) {
					if (err) {
						callback('Error in hash: ' + err);
					} else {
						console.log('correctHash: ' + correctHash + ' hash: ' + hash);
						if (hash == correctHash) {
							// Login ok
							console.log('User logged in: ' + email);
							getToken(email, function(err, identityId, token) {
								if (err) {
									callback('Error in getToken: ' + err);
								} else {
									callback(null, {
										login: true,
										identityId: identityId,
										token: token
									});
								}
							});
						} else {
							// Login failed
							console.log('User login failed: ' + email);
							callback(null, { login: false });
						}
					}
				});
			}
		}
	});
}
