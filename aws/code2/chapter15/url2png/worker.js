const fs = require('fs');
const AWS = require('aws-sdk');
const webshot = require('node-webshot');
const config = require('./config.json');
const sqs = new AWS.SQS({
  region: 'us-east-1'
});
const s3 = new AWS.S3({
  region: 'us-east-1'
});

const acknowledge = (message, cb) => {
  const params = {
    QueueUrl: config.QueueUrl,
    ReceiptHandle: message.ReceiptHandle
  };
  sqs.deleteMessage(params, cb);
};

const process = (message, cb) => {
  const body = JSON.parse(message.Body);
  const file = body.id + '.png';
  webshot(body.url, file, (err) => {
    if (err) {
      cb(err);
    } else {
      fs.readFile(file, (err, buf) => {
        if (err) {
          cb(err);
        } else {
          const params = {
            Bucket: config.Bucket,
            Key: file,
            ACL: 'public-read',
            ContentType: 'image/png',
            Body: buf
          };
          s3.putObject(params, (err) => {
            if (err) {
              cb(err);
            } else {
              fs.unlink(file, cb);
            }
          });
        }
      });
    }
  });
};

const receive = (cb) => {
  const params = {
    QueueUrl: config.QueueUrl,
    MaxNumberOfMessages: 1,
    VisibilityTimeout: 120,
    WaitTimeSeconds: 10
  };
  sqs.receiveMessage(params, (err, data) => {
    if (err) {
      cb(err);
    } else {
      if (data.Messages === undefined) {
        cb(null, null);
      } else {
        cb(null, data.Messages[0]);
      }
    }
  });
};

const run = () => {
  receive((err, message) => {
    if (err) {
      throw err;
    } else {
      if (message === null) {
        console.log('nothing to do');
        setTimeout(run, 1000);
      } else {
        console.log('process');
        process(message, (err) => {
          if (err) {
            throw err;
          } else {
            acknowledge(message, (err) => {
              if (err) {
                throw err;
              } else {
                console.log('done');
                setTimeout(run, 1000);
              }
            });
          }
        });
      }
    }
  });
};

run();
