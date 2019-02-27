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

        this.connectToFirebase();
    },
    addVideoToScreen: function (videoObjs) {
        var that = this;

        $.each(videoObjs.urls, function(index, video) {
          var newVideoElement = that.uiElements.videoCardTemplate.clone().attr('id', video.firebaseId);

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

          newVideoElement.find('video').attr('src', video.url);
          newVideoElement.find('.transcoding-indicator').hide();

          that.uiElements.videoList.prepend(newVideoElement);
        })
    },
    updateVideoOnScreen: function(firebaseId, videoObj) {
        var videoElement = getElementForVideo(firebaseId);
        if (!videoObj)
        {
          return;
        }

        if (videoObj.transcoding) {
            videoElement.find('video').hide();
            videoElement.find('.transcoding-indicator').show();
        } else {
            videoElement.find('video').show();
            videoElement.find('.transcoding-indicator').hide();

            getSignedUrls([{firebaseId: firebaseId, key: videoObj.key}], function(videos) {
              videoElement.find('video').attr('src', videos[0].url);
            })
        }
    },
    getSignedUrls: function(videoObjs, callback) {
      var that = this;

      if (videoObjs) {
        var objectMap = $.map(videoObjs, function (video, firebaseId) {
          return {firebaseId: firebaseId, key: video.key};
        })

        var getSignedUrl = this.data.config.apiBaseUrl + '/signed-url';

        $.post(getSignedUrl, JSON.stringify(objectMap), function(data, status){
          if (status === 'success') {
            callback.call(that, data);
          }
        });
      }
    },
    getElementForVideo: function(videoId) {
        return $('#' + videoId);
    },
    connectToFirebase: function () {
        var that = this;

        firebase.initializeApp(this.data.config.firebase);

        var isConnectedRef = firebase.database().ref(".info/connected");

        var nodeRef = firebase.database().ref();
        var childRef = firebase.database().ref('videos');
        //var nodeRef = firebaseRef.child('videos');

        isConnectedRef.on('value', function(snap) {
          if (snap.val() === true) {
            that.uiElements.loadingIndicator.hide();
          }
        });

        nodeRef
            .on('value', function(result){
              console.log(result.val());
            })

        // fired when a new movie is added to firebase
        nodeRef
            .on('child_added', function (childSnapshot) {
              that.getSignedUrls(childSnapshot.val(), that.addVideoToScreen);
            });

        // fired when a movie is updated
        childRef
            .on('child_changed', function (childSnapshot) {
              that.updateVideoOnScreen(childSnapshot.key, childSnapshot.val());
            });
    }
};
