var blessed = require('blessed');

var screen = blessed.screen({
	autoPadding: true,
	smartCSR: true,
	log: "./nodecc.log"
});

screen.title = 'Node Control Center for AWS';

var content = blessed.box({  
	parent: screen,
	width: '70%',
	height: '90%',
	top: '10%',
	left: '30%',
	border: {
		type: 'none',
		fg: '#ffffff'
	},
	fg: 'white',
	bg: 'blue',
	content: '{bold}Node Control Center for AWS{/bold}\n\nPlease select one of the actions from the left and press return.\n\nYou can always go back with the left arrow key.\n\nYou can terminate the application by pressing ESC or q.',
	tags: true
});

var progress = blessed.progressbar({
	parent: screen,
	width: '70%',
	height: '10%',
	top: '0%',
	left: '30%',
	orientation: 'horizontal',
	border: {
		type: 'line',
		fg: '#ffffff'
	},
	fg: 'white',
	bg: 'blue',
	barFg: 'green',
	barBg: 'green',
	filled: 0
});

var list = blessed.list({  
	parent: screen,
	width: '30%',
	height: '100%',
	top: '0%',
	left: '0%',
	border: {
		type: 'line',
		fg: '#ffffff'
	},
	fg: 'white',
	bg: 'blue',
	selectedBg: 'green',
	mouse: true,
	keys: true,
	vi: true,
	label: 'actions',
	items: ['list servers', 'create server', 'terminate server']
});
list.on('select', function(ev, i) {
	content.border.type = 'line';
	content.focus();
	list.border.type = 'none';
	open(i);
	screen.render(); 
});
list.focus();

function open(i) {
	screen.log('open(' + i + ')');
	if (i === 0) {
		loading();
		require('./lib/listServers.js')(function(err, instanceIds) {
			loaded();
			if (err) {
				log('error', 'listServers cb err: ' + err);
			} else {
				var instanceList = blessed.list({
					fg: 'white',
					bg: 'blue',
					selectedBg: 'green',
					mouse: true,
					keys: true,
					vi: true,
					items: instanceIds
				});
				content.append(instanceList);
				instanceList.focus();
				instanceList.on('select', function(ev, i) {
					loading();
					require('./lib/showServer.js')(instanceIds[i], function(err, instance) {
						loaded();
						if (err) {
							log('error', 'showServer cb err: ' + err);
						} else {
							var serverContent = blessed.box({  
								fg: 'white',
								bg: 'blue',
								content:
									'InstanceId: ' + instance.InstanceId + '\n' +
									'InstanceType: ' + instance.InstanceType + '\n' +
									'LaunchTime: ' + instance.LaunchTime + '\n' +
									'ImageId: ' + instance.ImageId + '\n' +
									'PublicDnsName: ' + instance.PublicDnsName
							});
							content.append(serverContent);
						}
						screen.render(); 
					});
				});
				screen.render(); 
			}
			screen.render(); 
		});
	} else if (i === 1) {
		loading();
		require('./lib/listAMIs.js')(function(err, result) {
			loaded();
			if (err) {
				log('error', 'listAMIs cb err: ' + err);
			} else {
				var amiList = blessed.list({
					fg: 'white',
					bg: 'blue',
					selectedBg: 'green',
					mouse: true,
					keys: true,
					vi: true,
					items: result.descriptions
				});
				content.append(amiList);
				amiList.focus();
				amiList.on('select', function(ev, i) {
					var amiId = result.amiIds[i];
					loading();
					require('./lib/listSubnets.js')(function(err, subnetIds) {
						loaded();
						if (err) {
							log('error', 'listSubnets cb err: ' + err);
						} else {
							var subnetList = blessed.list({
								fg: 'white',
								bg: 'blue',
								selectedBg: 'green',
								mouse: true,
								keys: true,
								vi: true,
								items: subnetIds
							});
							content.append(subnetList);
							subnetList.focus();
							subnetList.on('select', function(ev, i) {
								loading();
								require('./lib/createServer.js')(amiId, subnetIds[i], function(err) {
									loaded();
									if (err) {
										log('error', 'createServer cb err: ' + err);
									} else {
										var serverContent = blessed.box({  
											fg: 'white',
											bg: 'blue',
											content: 'starting ...'
										});
										content.append(serverContent);
									}
									screen.render(); 
								});
							});
							screen.render(); 
						}
						screen.render(); 
					});
				});
				screen.render(); 
			}
			screen.render(); 
		});
	} else if (i === 2) {
		loading();
		require('./lib/listServers.js')(function(err, instanceIds) {
			loaded();
			if (err) {
				log('error', 'listServers cb err: ' + err);
			} else {
				var instanceList = blessed.list({
					fg: 'white',
					bg: 'blue',
					selectedBg: 'green',
					mouse: true,
					keys: true,
					vi: true,
					items: instanceIds
				});
				content.append(instanceList);
				instanceList.focus();
				instanceList.on('select', function(ev, i) {
					loading();
					require('./lib/terminateServer.js')(instanceIds[i], function(err) {
						loaded();
						if (err) {
							log('error', 'terminateServer cb err: ' + err);
						} else {
							var serverContent = blessed.box({  
								fg: 'white',
								bg: 'blue',
								content: 'terminating ...'
							});
							content.append(serverContent);
						}
						screen.render(); 
					});
				});
				screen.render(); 
			}
			screen.render(); 
		});
	} else {
		log('error', 'not supported');
		screen.render(); 
	}
}

screen.key('left', function(ch, key) {  
	content.border.type = 'none';
	content.children.slice().forEach(function(child) {
		content.remove(child);
	});
	list.border.type = 'line';
	list.focus();
	screen.render(); 
});

screen.key(['escape', 'q', 'C-c'], function(ch, key) { 
	return process.exit(0);
});

var loadingInterval;

function loading() {
	progress.reset();
	clearInterval(loadingInterval);
	loadingInterval = setInterval(function() {
		if (progress.filled < 75) {
			progress.progress(progress.filled + 5);
		}
		screen.render(); 
	}, 200);
}

function loaded() {
	clearInterval(loadingInterval);
	progress.progress(100);
	screen.render(); 
}

function log(level, message) {
	screen.log('[' + level + ']: ' + message);
}

screen.render(); 
