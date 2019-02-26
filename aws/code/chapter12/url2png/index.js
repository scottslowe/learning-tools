var AWS = require('aws-sdk');
var uuid = require('node-uuid');
var config = require('./config.json');
var sqs = new AWS.SQS({
	"region": "us-east-1"
});

if (process.argv.length !== 3) {
	console.log('URL missing');
	process.exit(1);
}

var id = uuid.v4();
var body = {
	"id": id,
	"url": process.argv[2]
};

sqs.sendMessage({
	"MessageBody": JSON.stringify(body),
	"QueueUrl": config.QueueUrl
}, function(err) {
	if (err) {
		console.log('error', err);
	} else {
		console.log('PNG will be soon available at http://' + config.Bucket + '.s3-website-us-east-1.amazonaws.com/' + id + '.png');
	}
});
