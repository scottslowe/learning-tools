var videoController = {
    data: {
        config: null
    },
    uiElements: {
        videoCardTemplate: null,
        videoList: null,
        loadingIndicator: null
    },
    init: function (config) {
        this.uiElements.videoCardTemplate = $('#video-template');
        this.uiElements.videoList = $('#video-list');
        this.uiElements.loadingIndicator = $('#loading-indicator');

        this.data.config = config;
        this.authenticateWithFirebase();
    },
    addVideoToScreen: function (videoId, videoObj) {
        // clone the template video element
        var newVideoElement = this.uiElements.videoCardTemplate.clone().attr('id', videoId);

        newVideoElement.click(function() {
            // the user has clicked on the video... let's play it, or pause it depending on state
            var video = newVideoElement.find('video').get(0);

            if (newVideoElement.is('.video-playing')) {
                video.pause();
                $(video).removeAttr('controls'); // remove controls
            }
            else {
                $(video).attr('controls', ''); // show controls
                video.play();
            }

            newVideoElement.toggleClass('video-playing');

        });

        this.updateVideoOnScreen(newVideoElement, videoObj);

        this.uiElements.videoList.prepend(newVideoElement);
    },
    updateVideoOnScreen: function(videoElement, videoObj) {
        if (!videoObj){
          return;
        }

        if (videoObj.transcoding) {
            // the video is currently transcoding... hide the video and show the spinner
            videoElement.find('video').hide();
            videoElement.find('.transcoding-indicator').show();
        } else {
            // the video is not transcoding... show the video and hide the spinner
            videoElement.find('video').show();
            videoElement.find('.transcoding-indicator').hide();

            var getSignedUrl = this.data.config.apiBaseUrl + '/signed-url?key=' + encodeURI(videoObj.key);

            $.get(getSignedUrl, function(data, result) {
              if (result === 'success' && data.url) {
                videoElement.find('video').attr('src', data.url);
              }
            })
        }
    },
    getElementForVideo: function(videoId) {
        return $('#' + videoId);
    },
    connectToFirebase: function () {
        var that = this;

        that.uiElements.loadingIndicator.show();

        var isConnectedRef = firebase.database().ref(".info/connected");
        var nodeRef = firebase.database().ref('videos');

        isConnectedRef.on('value', function(snap) {
          if (snap.val() === true) {
            that.uiElements.loadingIndicator.hide();
          }
        });

        // fired when a new movie is added to firebase
        nodeRef
            .on('child_added', function (childSnapshot) {
                // add elements to the screen for the new video
                that.addVideoToScreen(childSnapshot.key, childSnapshot.val());
            });

        // fired when a movie is updated
        nodeRef
            .on('child_changed', function (childSnapshot) {

                // update the video object on screen with the new video details from firebase
                that.updateVideoOnScreen(that.getElementForVideo(childSnapshot.key), childSnapshot.val());
            });
    },
    authenticateWithFirebase: function() {
      var that = this;
      var ref = firebase.initializeApp(this.data.config.firebase);
      var firebaseToken = localStorage.getItem('firebaseToken');

      if (!firebaseToken) {
        console.log('Could not find a firebase delegation token. Please authenticate again.');
        return;
      }

      ref
        .auth()
        .signInWithCustomToken(firebaseToken)
        .then(function(result){
          that.connectToFirebase();
      }).catch(function(error){
        console.log(error);
      })
    },
};
