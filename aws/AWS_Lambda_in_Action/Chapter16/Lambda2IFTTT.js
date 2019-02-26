console.log('Loading function');

var https = require('https');
var querystring = require("querystring");

var iftttMakerEventName = '<EVENT>'
var iftttMakerSecretKey = '<IFTTT_MAKER_SECRET_KEY>';

var iftttMakerUrl =
    'https://maker.ifttt.com/trigger/' +
    iftttMakerEventName +
    '/with/key/' +
    iftttMakerSecretKey;

exports.handler = (event, context, callback) => {
    var output;
    if ('message' in event) {
        output = event.message;
    } else {
        callback('Error: no message in the event');
    }
    console.log('Output: ', output);

    var params = querystring.stringify({
        value1: output
    });

    https.get(encodeURI(iftttMakerUrl) + '?' + params, function(res) {
        console.log('Got response: ' + res.statusCode);
        res.setEncoding('utf8');
        res.on('data', function(d) {
            console.log('Body: ' + d);
        });
        callback(null, res.statusCode);
    }).on('error', function(e) {
        console.log("Got error: " + e.message);
        callback(e.message);
    });
};
