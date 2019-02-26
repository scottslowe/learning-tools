var fs = require('fs');
var AWS = require('aws-sdk');
var webshot = require('webshot');
var config = require('./config.json');
var sqs = new AWS.SQS({
	"region": "us-east-1"
});
var s3 = new AWS.S3({
	"region": "us-east-1"
});

function acknowledge(message, cb) {
	var params = {
		"QueueUrl": config.QueueUrl,
		"ReceiptHandle": message.ReceiptHandle
	};
	sqs.deleteMessage(params, cb);
}

function process(message, cb) {
	var body = JSON.parse(message.Body);
	var file = body.id + '.png';
	webshot(body.url, file, function(err) {
		if (err) {
			cb(err);
		} else {
			fs.readFile(file, function(err, buf) {
				if (err) {
					cb(err);
				} else {
					var params = {
						"Bucket": config.Bucket,
						"Key": file,
						"ACL": "public-read",
						"ContentType": "image/png",
						"Body": buf
					};
					s3.putObject(params, function(err) {
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
}

function receive(cb) {
	var params = {
		"QueueUrl": config.QueueUrl,
		"MaxNumberOfMessages": 1,
		"VisibilityTimeout": 120,
		"WaitTimeSeconds": 10
	};
	sqs.receiveMessage(params, function(err, data) {
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
}

function run() {
	receive(function(err, message) {
		if (err) {
			throw err;
		} else {
			if (message === null) {
				console.log('nothing to do');
				setTimeout(run, 1000);
			} else {
				console.log('process');
				process(message, function(err) {
					if (err) {
						throw err;
					} else {
						acknowledge(message, function(err) {
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
}

run();
