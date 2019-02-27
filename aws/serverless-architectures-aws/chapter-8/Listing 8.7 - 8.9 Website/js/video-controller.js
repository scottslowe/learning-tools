var videoController = {
	data: {
	    config: null
	},
  uiElements: {
      videoCardTemplate: null,
      videoList: null,
      loadingIndicator: null
  },
	init: function(config) {
		this.uiElements.videoCardTemplate = $('#video-template');
		this.uiElements.videoList = $('#video-list');

		this.data.config = config;

		this.getVideoList();
	},
	getVideoList: function() {
		var that = this;
		var url = this.data.config.apiBaseUrl + '/videos?encoding=' + encodeURIComponent('720p');

		$.get(url, function(data, status){
			that.updateVideoFrontpage(data);
		});
	},
	updateVideoFrontpage: function(data) {
		var baseUrl = data.domain;
		var bucket = data.bucket;

		for (var i = 0; i < data.files.length; i++) {
				var video = data.files[i];

				var clone = this.uiElements.videoCardTemplate.clone().attr('id', 'video-' + i);

				clone.find('source')
						 .attr('src', baseUrl + '/' + bucket + '/' + video.filename);

				this.uiElements.videoList.prepend(clone);
		}
	}
}
