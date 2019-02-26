var AWS = require('aws-sdk');
var ec2 = new AWS.EC2({
	"region": "us-east-1"
});

module.exports = function(instanceId, cb) {
	ec2.terminateInstances({
		"InstanceIds": [instanceId]
	}, function(err) {
		if (err) {
			cb(err);
		} else {
			cb(null);
		}
	});
};
