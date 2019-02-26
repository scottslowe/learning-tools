const AWS = require('aws-sdk');
const uuid = require('uuid/v4');
const config = require('./config.json');
const sqs = new AWS.SQS({
  region: 'us-east-1'
});

if (process.argv.length !== 3) {
  console.log('URL missing');
  process.exit(1);
}

const id = uuid();
const body = {
  id: id,
  url: process.argv[2]
};

sqs.sendMessage({
  MessageBody: JSON.stringify(body),
  QueueUrl: config.QueueUrl
}, (err) => {
  if (err) {
    console.log('error', err);
  } else {
    console.log('PNG will be soon available at http://' + config.Bucket + '.s3-website-us-east-1.amazonaws.com/' + id + '.png');
  }
});
