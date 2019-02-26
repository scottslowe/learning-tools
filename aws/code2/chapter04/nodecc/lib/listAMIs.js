const jmespath = require('jmespath');
const AWS = require('aws-sdk');
const ec2 = new AWS.EC2({
  region: 'us-east-1'
});

module.exports = (cb) => {
  ec2.describeImages({
    Filters: [{
      Name: 'name',
      Values: ['amzn-ami-hvm-2017.09.1.*-x86_64-gp2']
    }]
  }, (err, data) => {
    if (err) {
      cb(err);
    } else {
      const amiIds = jmespath.search(data, 'Images[*].ImageId');
      const descriptions = jmespath.search(data, 'Images[*].Description');
      cb(null, {amiIds: amiIds, descriptions: descriptions});
    }
  });
};
