var lambda = new AWS.Lambda();
var params = {
    FunctionName: 'anotherGreetingsOnDemand',
    Publish: true,
    S3Bucket: 'danilop-functions',
    S3Key: 'code/greetingsOnDemand-v2.zip'
};
lambda.updateFunctionCode(params, function(err, data) {
    if (err) console.log(err, err.stack);
    else console.log(data);
});
