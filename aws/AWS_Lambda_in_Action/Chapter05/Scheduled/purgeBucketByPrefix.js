var AWS = require('aws-sdk');
var util = require('util');

var s3 = new AWS.S3();

var dstBucket = 'danilop-eventdrivenapps';
var dstPrefix = 'tmp/';
var maxElapsedInSeconds = 3600;

var dstPrefixLength = dstPrefix.length;

function getObjectKeys(marker) {
    var params = {
        Bucket: dstBucket,
        Prefix: dstPrefix
    };
    if (marker !== null) {
        params.Marker = marker;
    }
    console.log(params);
    s3.listObjects(params, function(err, data) {
        if (err) {
            console.log(err, err.stack);
        } else {
            if (data.IsTruncated) {
                getObjectKeys(data.NextMarker);
            }
            data.Contents.forEach(function(item) {
                var fileName = item.Key;
                var fileDate = new Date(
                    fileName.substr(dstPrefixLength,4),
                    fileName.substr(dstPrefixLength + 4,2) - 1,
                    fileName.substr(dstPrefixLength + 6,2),
                    fileName.substr(dstPrefixLength + 8,2),
                    fileName.substr(dstPrefixLength + 10,2),
                    fileName.substr(dstPrefixLength + 12,2)
                );
                var elapsedInSeconds = (now - fileDate) / 1000;
                if (elapsedInSeconds > maxElapsedInSeconds) {
                    var params = {
                        Bucket: dstBucket,
                        Key: fileName
                    };
                    s3.deleteObject(params, function(err, data) {
                        if (err) {
                            console.log(err, err.stack);
                        } else {
                            console.log('Deleted ' + fileName);
                        }
                    });
                }
            });
        }
    });
}

exports.handler = (event, context) => {
    console.log("Reading options from event:\n", util.inspect(event, {depth: 5}));

    now = new Date();
    console.log('Now is ' + now.toISOString());

    getObjectKeys(null);
};
