(function(){
	$(document).ready(function(){
		userController.init(configConstants)
			.then(function() {
				videoController.init(configConstants);
				uploadController.init(configConstants);
				}
			);
	});
}());
