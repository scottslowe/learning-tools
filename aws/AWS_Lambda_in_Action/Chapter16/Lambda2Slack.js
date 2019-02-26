const https = require('https');

var webhook_host = '<YOUR_WEBHOOK_HOST>';
var webhook_path = '<YOUR_WEBHOOK_PATH_STARTING_WITH_A_SLASH>';

exports.handler = (event, context, callback) => {
    var post_data;
    if ('text' in event) {
        post_data = '{"text":"' + event['text'] + '"}';
    } else {
        post_data = '{"text":"Hello from AWS Lambda!"}';
    }

    var post_options = {
        hostname: webhook_host,
        port: 443,
        path: webhook_path,
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Content-Length': Buffer.byteLength(post_data)
        }
    };

    var post_req = https.request(post_options, function(res) {
        res.setEncoding('utf8');
        res.on('data', function(chunk) {
            console.log('Response: ' + chunk);
        });
    });

    post_req.write(post_data);
    post_req.end();
};
