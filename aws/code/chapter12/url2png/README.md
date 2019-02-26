# URL2PNG

![URL2PNG](./url2png.png?raw=true "URL2PNG")

Install the dependencies ...

	$ npm install

... and create a S3 bucket

	$ aws s3 mb s3://url2png

... and activate web hosting for bucket

	$ aws s3 website s3://url2png --index-document index.html --error-document error.html

... and create a SQS message queue with the help of the AWS CLI

	$ aws sqs create-queue --queue-name url2png
	{
		"QueueUrl": "https://queue.amazonaws.com/878533158213/url2png"
	}

... edit config.json and set QueueUrl and Bucket

... and run the URL2PNG worker

	$ node worker.js

... open another terminal and start a URL2PNG process

	$ node index.js "http://aws.amazon.com/"
	PNG will be soon available at http://aws-in-action-url2png.s3-website-us-east-1.amazonaws.com/6dbe4a05-82b3-4cbd-bd2b-65bbc8a51539.png

... wait and open the image
