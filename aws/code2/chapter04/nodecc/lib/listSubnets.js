const jmespath = require('jmespath');
const AWS = require('aws-sdk');
const ec2 = new AWS.EC2({
  region: 'us-east-1'
});

module.exports = (cb) => {
  ec2.describeVpcs({
    Filters: [{
      Name: 'isDefault',
      Values: ['true']
    }]
  }, (err, data) => {
    if (err) {
      cb(err);
    } else {
      const vpcId = data.Vpcs[0].VpcId;
      ec2.describeSubnets({
        Filters: [{
          Name: 'vpc-id',
          Values: [vpcId]
        }]
      }, (err, data) => {
        if (err) {
          cb(err);
        } else {
          const subnetIds = jmespath.search(data, 'Subnets[*].SubnetId');
          cb(null, subnetIds);
        }
      });
    }
  });
};
