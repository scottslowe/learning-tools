AWS.config.region = 'us-east-1';
AWS.config.credentials = new AWS.CognitoIdentityCredentials({
  IdentityPoolId: 'us-east-1:3909a295-f5f8-4d6b-97ea-994ed4d324dd'
});

var lambda = new AWS.Lambda();

function lostPassword() {

  var result = document.getElementById('result');
  var email = document.getElementById('email');

  result.innerHTML = 'Password Lost...';

	if (email.value == null || email.value == '') {
  	result.innerHTML = 'Please specify your email address.';
	} else {

    var input = {
      email: email.value
    };

    lambda.invoke({
      FunctionName: 'sampleAuthLostPassword',
      Payload: JSON.stringify(input)
    }, function(err, data) {
      if (err) console.log(err, err.stack);
      else {
        var output = JSON.parse(data.Payload);
        if (output.sent) {
          result.innerHTML = 'Email sent. Please check your email to reset your password.';
        } else {
          result.innerHTML = 'Email <b>not</b> sent.';
        }
      }
    });
    
	}
}

var form = document.getElementById('lost-password-form');
form.addEventListener('submit', function(evt) {
  evt.preventDefault();
  lostPassword();
});
