var AWS = require('aws-sdk');
var ec2 = new AWS.EC2({
	"region": "us-east-1"
});

module.exports = function(amiId, subnetId, cb) {
	ec2.runInstances({
		"ImageId": amiId,
		"MinCount": 1,
		"MaxCount": 1,
		"KeyName": "mykey",
		"InstanceType": "t2.micro",
		"SubnetId": subnetId
	}, function(err) {
		if (err) {
			cb(err);
		} else {
			cb(null);
		}
	});
};
