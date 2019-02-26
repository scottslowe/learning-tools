const AWS = require('aws-sdk');
const ec2 = new AWS.EC2({
  region: 'us-east-1'
});

module.exports = (instanceId, cb) => {
  ec2.terminateInstances({
    InstanceIds: [instanceId]
  }, (err) => {
    if (err) {
      cb(err);
    } else {
      cb(null);
    }
  });
};
