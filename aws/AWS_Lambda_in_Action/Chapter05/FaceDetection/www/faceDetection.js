// Initialize the Amazon Cognito credentials provider
AWS.config.region = 'eu-west-1'; // Region
AWS.config.credentials = new AWS.CognitoIdentityCredentials({
  IdentityPoolId: 'eu-west-1:40f0910c-72f5-4f73-83bc-89f44d0afe92',
});

var lambda = new AWS.Lambda();

function returnFaces() {
  var imageUrl = null;
  var input;
  if (document.getElementById('monalisa').checked) {
    imageUrl = 'https://eventdrivenapps.com/examples/FaceDetection/monalisa.jpg';
  } else if (document.getElementById('astronauts').checked) {
    imageUrl = 'https://eventdrivenapps.com/examples/FaceDetection/astronauts.jpg';
  } else {
    imageUrl = document.getElementById('customUrl').value;
  }
  if (imageUrl == null || imageUrl == '') {
    result.innerHTML =
      '<div class="alert alert-danger">Please provide a URL</div>';
    return;
  } else {
    input = {
      imageUrl: imageUrl
    };
  }
  lambda.invoke({
    FunctionName: 'faceDetection',
    Payload: JSON.stringify(input)
  }, function(err, data) {
    var result = document.getElementById('result');
    if (err) {
      console.log(err, err.stack);
      result.innerHTML =
        '<div class="alert alert-danger">' + err + '</div>';
    } else {
      var output = JSON.parse(data.Payload);
      var formattedOutput = '<div class="alert alert-success">';
      if (output.faces < 1) {
        formattedOutput += 'There are no faces';
      } else if (output.faces == 1) {
        formattedOutput += 'There is 1 face';
      } else {
        formattedOutput += 'There are ' + output.faces + ' faces';
      }
      formattedOutput += ' in the picture.</div>' +
      '<div class="row">' +
      '<div class="col-sm-6"><img src="' + imageUrl + '"/></div>' +
      '<div class="col-sm-6"><img src="' + output.outputUrl + '"/></div>';
      '<div/>';
      result.innerHTML = formattedOutput;
    }
  });
}

var form = document.getElementById('inputForm');
form.addEventListener('submit', function(evt) {
  evt.preventDefault();
  returnFaces();
});
