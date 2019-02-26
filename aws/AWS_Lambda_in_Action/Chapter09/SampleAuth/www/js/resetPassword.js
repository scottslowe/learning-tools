AWS.config.region = '<REGION>';
AWS.config.credentials = new AWS.CognitoIdentityCredentials({
  IdentityPoolId: '<IDENTITY_POOL_ID>'
});

var lambda = new AWS.Lambda();

function getUrlParams() {
  var p = {};
  var match,
    pl     = /\+/g,  // Regex for replacing addition symbol with a space
    search = /([^&=]+)=?([^&]*)/g,
    decode = function (s) { return decodeURIComponent(s.replace(pl, " ")); },
    query  = window.location.search.substring(1);
  while (match = search.exec(query))
    p[decode(match[1])] = decode(match[2]);
  return p;
}

function resetPassword() {

  var result = document.getElementById('result');
  var password = document.getElementById('new-password');
  var verifyPassword = document.getElementById('verify-new-password');

  var urlParams = getUrlParams();
  var email = urlParams['email'] || null;
  var lost = urlParams['lost'] || null;

	if (password.value == null || password.value == '') {
    result.innerHTML = 'Please specify a password.';
  } else if (password.value != verifyPassword.value) {
    result.innerHTML = 'Passwords are <b>not</b> the same, please check.';
  } else {
    if ((!email)||(!lost)) {
      result.innerHTML = 'Please specify email and lost token in the URL.';
    } else {
	    result.innerHTML = 'Trying to reset password for user ' + email + ' ...';

      var input = {
        email: email,
        lost: lost,
				password: password.value
      };

      lambda.invoke({
        FunctionName: 'sampleAuthResetPassword',
        Payload: JSON.stringify(input)
      }, function(err, data) {
        if (err) console.log(err, err.stack);
        else {
          var output = JSON.parse(data.Payload);
          if (output.changed) {
	          result.innerHTML = 'Password changed for user ' + email;
	        } else {
	          result.innerHTML = 'Password <b>not</b> changed.';
	        }
	      }
	    });

    }
  }
}

function init() {
  if (email) {
	   result.innerHTML = 'Type your new password for user ' + email;
   }
}

var form = document.getElementById('reset-password-form');
form.addEventListener('submit', function(evt) {
  evt.preventDefault();
  resetPassword();
});

window.onload = init();
