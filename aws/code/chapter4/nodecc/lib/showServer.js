var AWS = require('aws-sdk');
var ec2 = new AWS.EC2({
	"region": "us-east-1"
});

module.exports = function(instanceId, cb) {
	ec2.describeInstances({
		"InstanceIds": [instanceId]
	}, function(err, data) {
		if (err) {
			cb(err);
		} else {
			cb(null, data.Reservations[0].Instances[0]);
		}
	});
};
