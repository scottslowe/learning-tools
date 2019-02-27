var AWS = require('aws-sdk');
var stepFunctions = new AWS.StepFunctions();

var params = {
  stateMachineArn: 'arn:aws:states:us-east-1:038221756127:stateMachine:HelloWorldStepMachine', /* required */
  input: "{'bucket':'serverless-image-transform', 'key':'image.png'}",
  name: 'MyTest'
};

stepFunctions.startExecution(params, function(err, data) {
  if (err) {
    callback(err);
  }
  else {
    callback(null, 'Step Functions executionARN: ' + data.executionArn);
  }
});
