var userController = {
  data: {
    auth0Lock: null,
    config: null
  },
  authentication: null,
  uiElements: {
    loginButton: null,
    logoutButton: null,
    profileButton: null,
    profileNameLabel: null,
    profileImage: null,
    uploadButton: null,
    videoList: null
  },
  init: function(config) {
    this.uiElements.loginButton = $('#auth0-login');
    this.uiElements.logoutButton = $('#auth0-logout');
    this.uiElements.profileButton = $('#user-profile');
    this.uiElements.profileNameLabel = $('#profilename');
    this.uiElements.profileImage = $('#profilepicture');
    this.uiElements.uploadButton = $('#upload-video-button');
    this.uiElements.videoList = $('#video-list');

    this.data.config = config;
    this.data.auth0Lock = new Auth0Lock(config.auth0.clientId, config.auth0.domain);

    this.wireEvents();
    return this.deferredAuthentication();
  },
  deferredAuthentication: function() {
    var that = this;
    this.authentication = $.Deferred();

    var idToken = localStorage.getItem('userToken');

    if (idToken) {
      this.configureAuthenticatedRequests();
      this.data.auth0Lock.getProfile(idToken, function(err, profile) {
        if (err) {
          return alert('There was an error getting the profile: ' + err.message);
        }
        that.showUserAuthenticationDetails(profile);
      });

      var firebaseToken = localStorage.getItem('firebaseToken');

      if (firebaseToken) {
        this.authentication.resolve();
      }  else {
        this.getFirebaseToken(idToken);
      }
    }

    return this.authentication;
  },
  configureAuthenticatedRequests: function() {
    $.ajaxSetup({
      'beforeSend': function(xhr) {
        xhr.setRequestHeader('Authorization', 'Bearer ' + localStorage.getItem('userToken'));
      }
    });
  },
  showUserAuthenticationDetails: function(profile) {
    var showAuthenticationElements = !!profile;

    if (showAuthenticationElements) {
      this.uiElements.profileNameLabel.text(profile.nickname);
      this.uiElements.profileImage.attr('src', profile.picture);
      this.uiElements.uploadButton.css('display', 'inline-block');
    }

    this.uiElements.loginButton.toggle(!showAuthenticationElements);
    this.uiElements.logoutButton.toggle(showAuthenticationElements);
    this.uiElements.profileButton.toggle(showAuthenticationElements);
  },
  getFirebaseToken: function(token){
    var that = this;
    var config = this.data.config.auth0;

    var url = 'https://' + config.domain + '/delegation';

    var data = {
      id_token: token,
      scope: config.scope,
      api_type: config.api_type,
      grant_type: config.grant_type,
      target: config.target,
      client_id: config.clientId
    }

    $.post(url, data, function(data, status) {
      if (status === 'success') {
        localStorage.setItem('firebaseToken', data.id_token);
        that.authentication.resolve();
      } else {
        console.log('Could not get retrieve firebase delegation token', data, status);
        that.authentication.fail();
      }
    }, 'json');
  },
  wireEvents: function() {
    var that = this;

    this.uiElements.loginButton.click(function(e) {
      var params = {
        authParams: {
          scope: 'openid email user_metadata picture'
        }
      };

      that.data.auth0Lock.show(params, function(err, profile, token) {
        if (err) {
          // Error callback
          alert('There was an error');
        } else {
          // Save the JWT token.
          localStorage.setItem('userToken', token);

          that.getFirebaseToken(token);
          that.configureAuthenticatedRequests();
          that.showUserAuthenticationDetails(profile);
        }
      });
    });

    this.uiElements.logoutButton.click(function(e) {
      localStorage.removeItem('userToken');
      localStorage.removeItem('firebaseToken');

      that.uiElements.videoList.empty();
      that.uiElements.logoutButton.hide();
      that.uiElements.profileButton.hide();
      that.uiElements.uploadButton.hide();
      that.uiElements.loginButton.show();
    });

    this.uiElements.profileButton.click(function(e) {
      var url = that.data.config.apiBaseUrl + '/user-profile';

      $.get(url, function(data, status) {
        $('#user-profile-raw-json').text(JSON.stringify(data, null, 2));
        $('#user-profile-modal').modal();
      })
    });
  }
}
