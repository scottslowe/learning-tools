AWS.config.region = 'us-east-1';
AWS.config.credentials = new AWS.CognitoIdentityCredentials({
  IdentityPoolId: 'us-east-1:3909a295-f5f8-4d6b-97ea-994ed4d324dd'
});

var lambda = new AWS.Lambda();

function login() {

  var result = document.getElementById('result');
  var email = document.getElementById('email');
  var password = document.getElementById('password');

  result.innerHTML = 'Login...';

  if (email.value == null || email.value == '') {
    result.innerHTML = 'Please specify your email address.';
  } else if (password.value == null || password.value == '') {
    result.innerHTML = 'Please specify a password.';
  } else {

    var input = {
      email: email.value,
      password: password.value
    };

    lambda.invoke({
      FunctionName: 'sampleAuthLogin',
      Payload: JSON.stringify(input)
    }, function(err, data) {
      if (err) console.log(err, err.stack);
      else {
        var output = JSON.parse(data.Payload);
        if (!output.login) {
          result.innerHTML = '<b>Not</b> logged in';
        } else {
          result.innerHTML = 'Logged in with IdentityId: ' + output.identityId + '<br>';

          var creds = AWS.config.credentials;
          creds.params.IdentityId = output.identityId;
          creds.params.Logins = {
            'cognito-identity.amazonaws.com': output.token
          };
          creds.expired = true;

          // Do something with the authenticated role

        }
      }
    });

	}
}

var form = document.getElementById('login-form');
form.addEventListener('submit', function(evt) {
  evt.preventDefault();
  login();
});
