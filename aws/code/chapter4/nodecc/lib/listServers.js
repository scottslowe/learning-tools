var jmespath = require('jmespath');
var AWS = require('aws-sdk');
var ec2 = new AWS.EC2({
	"region": "us-east-1"
});

module.exports = function(cb) {
	ec2.describeInstances({
		"Filters": [{
			"Name": "instance-state-name",
			"Values": ["pending", "running"]
		}],
		"MaxResults": 10
	}, function(err, data) {
		if (err) {
			cb(err);
		} else {
			var instanceIds = jmespath.search(data, 'Reservations[].Instances[].InstanceId');
			cb(null, instanceIds);
		}
	});
};
