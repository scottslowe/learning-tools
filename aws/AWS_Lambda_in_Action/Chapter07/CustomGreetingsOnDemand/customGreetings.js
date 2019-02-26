// Initialize the Amazon Cognito credentials provider
AWS.config.region = 'eu-west-1'; // Region
AWS.config.credentials = new AWS.CognitoIdentityCredentials({
  IdentityPoolId: 'eu-west-1:40f0910c-72f5-4f73-83bc-89f44d0afe92',
});

var lambda = new AWS.Lambda();

function returnGreetings() {
  var greet = document.getElementById('greet');
  var name = document.getElementById('name');
  var input = {};
  if (greet.value != null && greet.value != '') {
    input.greet = greet.value;
  }
  if (name.value != null && name.value != '') {
    input.name = name.value;
  }
  lambda.invoke({
    FunctionName: 'customGreetingsOnDemand',
    Payload: JSON.stringify(input)
  }, function(err, data) {
    var result = document.getElementById('result');
    if (err) {
      console.log(err, err.stack);
      result.innerHTML =
        '<div class="alert alert-danger">' + err + '</div>';
    } else {
      var output = JSON.parse(data.Payload);
      result.innerHTML =
        '<div class="alert alert-success">' + output + '</div>';
    }
  });
}

var form = document.getElementById('greetingsForm');
form.addEventListener('submit', function(evt) {
  evt.preventDefault();
  returnGreetings();
});
