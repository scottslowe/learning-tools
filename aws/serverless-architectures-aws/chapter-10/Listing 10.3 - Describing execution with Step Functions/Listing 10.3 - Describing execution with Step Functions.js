var AWS = require('aws-sdk');
var stepFunctions = new AWS.StepFunctions();

stepFunctions.describeExecution(params, function(err, data) {
  if (err) console.log(err, err.stack); // an error occurred
  else     console.log(data);           // successful response
});
