const AWS = require('aws-sdk');
const ec2 = new AWS.EC2({
  region: 'us-east-1'
});

module.exports = (instanceId, cb) => {
  ec2.describeInstances({
    InstanceIds: [instanceId]
  }, (err, data) => {
    if (err) {
      cb(err);
    } else {
      cb(null, data.Reservations[0].Instances[0]);
    }
  });
};
