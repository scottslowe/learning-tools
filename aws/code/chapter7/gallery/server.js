var express = require("express");
var AWS = require("aws-sdk");
var mu = require("mu2-updated");
var uuid = require("uuid");
var multiparty = require("multiparty");

var app = express();
var s3 = new AWS.S3({
	"region": "us-east-1"
});

var bucket = process.argv[2];
if (!bucket || bucket.length < 1) {
	console.error("Missing S3 bucket. Start with node server.js BUCKETNAME instead.");
	process.exit(1);
}

function listImages(response) {
	var params = {
		Bucket: bucket
	};
	s3.listObjects(params, function(err, data) {
		if (err) {
			console.error(err);
			response.status(500);
			response.send("Internal server error.");
		} else {
			var stream = mu.compileAndRender(
				"index.html", 
				{
					Objects: data.Contents, 
					Bucket: bucket
				}
			);
			stream.pipe(response);
		}
	});
}

function uploadImage(image, response) {
	var params = {
		Body: image,
		Bucket: bucket,
		Key: uuid.v4(),
		ACL: "public-read",
		ContentLength: image.byteCount,
		ContentType: image.headers["content-type"]
	};
	s3.putObject(params, function(err, data) {
		if (err) {
			console.error(err);
			response.status(500);
			response.send("Internal server error.");
		} else {
			response.redirect("/");
		}
	});
}

app.get('/', function (request, response) {
	listImages(response);
});

app.post('/upload', function (request, response) {
	var form = new multiparty.Form();
	form.on("part", function(part) {
		uploadImage(part, response);
	});
	form.parse(request);
});
 
app.listen(8080);

console.log("Server started. Open http://localhost:8080 with browser.");
