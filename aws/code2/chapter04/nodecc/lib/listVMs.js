const jmespath = require('jmespath');
const AWS = require('aws-sdk');
const ec2 = new AWS.EC2({
  region: 'us-east-1'
});

module.exports = (cb) => {
  ec2.describeInstances({
    Filters: [{
      Name: 'instance-state-name',
      Values: ['pending', 'running']
    }],
    MaxResults: 10
  }, (err, data) => {
    if (err) {
      cb(err);
    } else {
      const instanceIds = jmespath.search(data, 'Reservations[].Instances[].InstanceId');
      cb(null, instanceIds);
    }
  });
};
