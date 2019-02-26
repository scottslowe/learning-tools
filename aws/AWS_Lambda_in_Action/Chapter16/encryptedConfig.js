var AWS = require('aws-sdk');

var kms = new AWS.KMS();

var fnEncryptedConfig = '<ENCRYPED_CONFIG>';
var fnConfig;

exports.handler = (event, context, callback) => {
    if (fnConfig) {
        processEvent(event, context, callback);
    } else {
        var encryptedBuf = new Buffer(fnEncryptedConfig, 'base64');
        var cipherText = { CiphertextBlob: encryptedBuf };

        kms.decrypt(cipherText, function (err, data) {
            if (err) {
                console.log("Decrypt error: " + err);
                callback(err);
            } else {
                fnConfig = JSON.parse(data.Plaintext.toString('ascii'));
                processEvent(event, context, callback);
            }
        });
    }
};

var processEvent = function (event, context, callback) {  #K
    console.log('user: ' + functionConfig.user);
    console.log('password: ' + functionConfig.password);
    console.log('event: ' + event);
};
