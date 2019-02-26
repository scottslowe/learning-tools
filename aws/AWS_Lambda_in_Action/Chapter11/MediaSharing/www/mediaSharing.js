// Config
var S3_BUCKET = '<BUCKET>';
var ITEMS_TABLE = '<DYNAMODB_TABLE>';
var IDENTITY_POOL_ID = '<IDENTITY_POOL_ID>';
var REGION = '<REGION>'

// AWS Credentials via Amazon Cognito
AWS.config.region = REGION;
AWS.config.credentials = new AWS.CognitoIdentityCredentials({
  IdentityPoolId: IDENTITY_POOL_ID
});

// Login status
var identityId = null;
var publicContent = emptyContent();
var privateContent = emptyContent();
var index = {};

// Page content
var result = document.getElementById('result');
var actions = document.getElementById('actions');
var detail = document.getElementById('detail');
var content = document.getElementById('content');

// AWS Service Objects
var lambda = new AWS.Lambda();
var s3 = new AWS.S3();
var dynamodb = new AWS.DynamoDB();

function emptyContent() {
  return { lastUpdate: null, index: null };
}

function login() {

  var email = document.getElementById('email');
  var password = document.getElementById('password');

  result.innerHTML = getAlert('info', 'Login...');

  if (email.value == null || email.value == '') {
    result.innerHTML = getAlert('warning', 'Please specify your email address.');
  } else if (password.value == null || password.value == '') {
    result.innerHTML = getAlert('warning', 'Please specify a password.');
  } else {

    var input = {
      email: email.value,
      password: password.value
    };

    lambda.invoke({
      FunctionName: 'sampleAuthLogin',
      Payload: JSON.stringify(input)
    }, function(err, data) {
      if (err) {
        console.log(err, err.stack);
        result.innerHTML = getAlert('danger', err);
      } else {
        var output = JSON.parse(data.Payload);
        if (!output.login) {
          result.innerHTML = getAlert('warning', '<b>Not</b> logged in');
        } else {
          result.innerHTML = getAlert('success', 'Logged in with IdentityId: ' + output.identityId + '<br>');
          identityId = output.identityId;
          var creds = AWS.config.credentials;
          creds.params.IdentityId = output.identityId;
          creds.params.Logins = {
            'cognito-identity.amazonaws.com': output.token
          };
          creds.expired = true;
          updateActions();
          updateContent();
        }
      }
    });
  }
}

function logout() {

  identityId = null;
  result.innerHTML = getAlert('info', 'Logged out.');
  privateContent = emptyContent();

  var creds = AWS.config.credentials;
  creds.params.Logins = {};
  creds.refresh(function() {
    renderContent();
    updateActions();
  });
}

function updateActions() {

  if (identityId == null) {
    // Unauthenticated
    result.innerHTML = getAlert('info', '<p>Please login to upload and see your private content.</p>');
    actions.innerHTML =
      '<form class="form-inline" role="form" id="login-form">' +
        '<div class="form-group">' +
          '<label for="email">Email </label>' +
            '<input type="text" class="form-control" id="email">' +
          '</div> ' +
        '<div class="form-group">' +
          '<label for="password">Password </label>' +
          '<input type="password" class="form-control" id="password">' +
        '</div>' +
        '<button type="submit" class="btn btn-default">Login</button>' +
      '</form>';
    var form = document.getElementById('login-form');
    form.addEventListener('submit', function(evt) {
      evt.preventDefault();
      login();
    });
  } else {
    // Authenticated
    actions.innerHTML =
     '<form class="form-horizontal" role="form" id="add-picture-form">' +
        '<div class="form-group">' +
          '<label class="control-label col-sm-2" for="mediaFile">Photo to Upload</label>' +
          '<div class="col-sm-10">' +
            '<input type="file" name="mediaFile" id="mediaFile">' +
          '</div>' +
        '</div>' +
        '<div class="form-group">' +
          '<label class="control-label col-sm-2" for="is-public">Public</label>' +
            '<div class="col-sm-10">' +
              '<input type="checkbox" value="" name="is-public" id="is-public" placeholder="is-public">' +
            '</div>' +
          '</div>' +
        '</div>' +
        '<div class="form-group">' +
          '<label class="control-label col-sm-2" for="title">Title</label>' +
          '<div class="col-sm-10">' +
            '<input type="text" class="form-control" name="title" id="title" placeholder="title">' +
          '</div>' +
        '</div>' +
        '<div class="form-group">' +
          '<label class="control-label col-sm-2" for="description">Description</label>' +
          '<div class="col-sm-10">' +
            '<input type="text" class="form-control" name="description" id="description" placeholder="description">' +
          '</div>' +
        '</div>' +
        '<div class="form-group">' +
          '<div class="col-sm-offset-2 col-sm-10">' +
            '<button type="submit" class="btn btn-default"> Add Picture</button>' +
            '<button type="button" id="logout-button" class="btn btn-default"> Logout</button>' +
          '</div>' +
        '</div>' +
      '</form>';
    var form = document.getElementById('add-picture-form');
    form.addEventListener('submit', function(evt) {
      evt.preventDefault();
      addPicture();
    });
    var logoutButton = document.getElementById('logout-button');
    logoutButton.addEventListener('click', logout);
  }
}

function addPicture() {

  var mediaFile = document.getElementById('mediaFile');
  var isPublic = document.getElementById('is-public');
  var title = document.getElementById('title');
  var description = document.getElementById('description');
  var file = mediaFile.files[0];

  if (!file) {
    result.innerHTML = getAlert('warning', 'Nothing to upload.');
    return;
  }
  if (description.value == '') {
    result.innerHTML = getAlert('warning', 'Please provide a description.');
    return;
  }

  result.innerHTML = '';
  var key = (isPublic.checked ? 'public' : 'private') +
      '/content/' + identityId + '/' + file.name;
  console.log(key);
  console.log(isPublic.checked);
  var params = {
    Bucket: S3_BUCKET,
    Key: key,
    ContentType: file.type,
    Body: file,
    Metadata: {
      data: JSON.stringify({
        isPublic: isPublic.checked,
        title: title.value,
        description: description.value
      })
    }};
  uploadToS3(params);
}

function uploadToS3(params) {

  if (identityId == null) {
    result.innerHTML = getAlert('warning', 'Please login to upload.');
  } else {
    result.innerHTML = getAlert('info', 'Uploading...');
    var s3 = new AWS.S3();
    s3.putObject(params, function(err, data) {
      result.innerHTML =
        err ? getAlert('danger', 'Error!' + err + err.stack)
           : getAlert('success', 'Uploaded.');
    });
  }

}

function updateContent() {

    var publicContentIndexKey = 'public/index/content.json';
    checkContent(publicContentIndexKey, publicContent);
    if (identityId != null) {
      var privateContentIndexKey = 'private/index/' + identityId + '/content.json';
      checkContent(privateContentIndexKey, privateContent);
    }

}

function checkContent(key, content) {

    var params = {
      Bucket: S3_BUCKET,
      Key: key
    };
    if (content.lastUpdate != null) {
      params.IfModifiedSince = content.lastUpdate;
    }
    s3.getObject(params, function(err, data) {
      if (err) {
        if (err.code == 'NotModified') {
          console.log('Not Modified');
        } else {
          console.log(err, err.stack);
        }
      } else {
        console.log(key);
        console.log(data);
        currentUpdate = new Date(data.LastModified);
        console.log('currentUpdate: ' + currentUpdate);
        console.log('lastUpdate: ' + content.lastUpdate);
        if (content.lastUpdate == null ||
          currentUpdate > content.lastUpdate) {
            content.lastUpdate = currentUpdate;
            content.index = JSON.parse(data.Body);
            renderContent();
            console.log("Updated");
        }
      }
    });

}

function getSignedUrlFromKey(key) {

  var params = {Bucket: S3_BUCKET, Key: key, Expires: 60};
  var url = s3.getSignedUrl('getObject', params);
  console.log('The URL is', url); // expires in 60 seconds
  return url;

}

function renderContent() {

    index = {};
    console.log(publicContent.index);
    if (publicContent.index != null) {
      publicContent.index.forEach(function(element) {
        element.isPublic = true;
        element.isOwner = (identityId != null && element.identityId == identityId);
        index[element.objectKey] = element;
      });
    }
    console.log(privateContent.index);
    if (privateContent.index != null) {
      privateContent.index.forEach(function(element) {
        element.isPublic = false;
        element.isOwner = (identityId != null && element.identityId == identityId);
        index[element.objectKey] = element;
      });
    }
    var html = '';
    for(var objectKey in index) {
      var element = index[objectKey];
      console.log(element);

      html += '<div class="col-sm-3 thumbnail alert ' +
         (element.isPublic ? 'alert-success' : 'alert-warning') + '"">' +
        (element.isOwner ? '<button type="button" class="close" onclick=deleteContent("' +
          objectKey + '")>&times;</button>' : '') +
        '<h4 class="text-center">' + element.title + '</h4>' +
        '<a data-toggle="modal" data-target="#myModalDetail" ' +
        'onclick=showContent("' + objectKey + '")>' +
        '<img class="img-rounded" ' +
        'src="' + getSignedUrlFromKey(element.thumbnailKey) + '" ' +
        'alt="' + element.title + '" ' + '>' +
        '</a>' +
        '<p class="text-center">' + element.description + '</p>' +
        '</div>';
    }
    content.innerHTML = html;

}

function showContent(objectKey) {

  var element = index[objectKey];
  detail.innerHTML =
    '<div class="modal-content">' +
      '<div class="modal-header">' +
        '<button type="button" class="close" data-dismiss="modal">&times;</button>' +
          '<h4 class="modal-title">' + element.title + ' (' +
          (element.isPublic ? "Public" : "Private") +')</h4>' +
        '</div>' +
        '<div class="modal-body">' +
          '<p>' + element.description + '</p>' +
          '<div class="thumbnail">' +
            '<img class="img-responsive" src="' + getSignedUrlFromKey(objectKey) + '">' +
          '</div>' +
        '</div>' +
        '<div class="modal-footer">' +
          '<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>' +
        '</div>' +
      '</div>' +
    '</div>';

}

function deleteContent(objectKey) {
  console.log(objectKey);
  var params = {
    Bucket: S3_BUCKET,
    Key: objectKey
  }
  deleteFromS3(params);

}

function deleteFromS3(params) {
  result.innerHTML = getAlert('info', 'Deleting...');
  s3.deleteObject(params, function(err, data) {
    result.innerHTML =
      err ? getAlert('danger', 'Error!' + err + err.stack)
         : getAlert('success', 'Deleted.');
  });
}

function getAlert(type, message) {
  return '<div class="alert alert-' + type + '"  >' +
    '<a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>' +
    message + '</div>';
}

function init() {
  updateActions();
  updateContent();
  setInterval(updateContent, 3000);
}

window.onload = init();
