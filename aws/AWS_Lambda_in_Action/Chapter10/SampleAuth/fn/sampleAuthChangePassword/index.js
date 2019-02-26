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
      if ('Item' in data) {
        var hash = data.Item.passwordHash.S;
        var salt = data.Item.passwordSalt.S;
        fn(null, hash, salt);
      } else {
        fn(null, null); // User not found
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
        }
      }
    },
    fn);
}

exports.handler = function(event, context) {

  var email = event.email;
  var oldPassword = event.oldPassword;
  var newPassword = event.newPassword;

  getUser(email, function(err, correctHash, salt) {
    if (err) {
      context.fail('Error in getUser: ' + err);
    } else {
      if (correctHash == null) {
        // User not found
        console.log('User not found: ' + email);
        context.succeed({
          changed: false
        });
      } else {
        computeHash(oldPassword, salt, function(err, salt, hash) {
          if (err) {
            context.fail('Error in hash: ' + err);
          } else {
            if (hash == correctHash) {
              // Login ok
              console.log('User logged in: ' + email);
              computeHash(newPassword, function(err, newSalt, newHash) {
                if (err) {
                  context.fail('Error in computeHash: ' + err);
                } else {
                  updateUser(email, newHash, newSalt, function(err, data) {
                    if (err) {
                      context.fail('Error in updateUser: ' + err);
                    } else {
                      console.log('User password changed: ' + email);
                      context.succeed({
                        changed: true
                      });
                    }
                  });
                }
              });
            } else {
              // Login failed
              console.log('User login failed: ' + email);
              context.succeed({
                changed: false
              });
            }
          }
        });
      }
    }
  });
}
