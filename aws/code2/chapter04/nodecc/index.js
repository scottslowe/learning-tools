const blessed = require('blessed');

const screen = blessed.screen({
  autoPadding: true,
  smartCSR: true,
  log: './nodecc.log'
});
screen.title = 'Node Control Center for AWS';

const content = blessed.box({  
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

const progress = blessed.progressbar({
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

const list = blessed.list({  
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
  items: ['list virtual machines', 'create virtual machine', 'terminate virtual machine']
});
list.on('select', (ev, i) => {
  content.border.type = 'line';
  content.focus();
  list.border.type = 'none';
  open(i);
  screen.render(); 
});
list.focus();

const open = (i) => {
  screen.log('open(' + i + ')');
  if (i === 0) {
    loading();
    require('./lib/listVMs.js')((err, instanceIds) => {
      loaded();
      if (err) {
        log('error', 'listVMs cb err: ' + err);
      } else {
        const instanceList = blessed.list({
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
        instanceList.on('select', (ev, i) => {
          loading();
          require('./lib/showVM.js')(instanceIds[i], (err, instance) => {
            loaded();
            if (err) {
              log('error', 'showVM cb err: ' + err);
            } else {
              const vmContent = blessed.box({  
                fg: 'white',
                bg: 'blue',
                content:
                  'InstanceId: ' + instance.InstanceId + '\n' +
                  'InstanceType: ' + instance.InstanceType + '\n' +
                  'LaunchTime: ' + instance.LaunchTime + '\n' +
                  'ImageId: ' + instance.ImageId + '\n' +
                  'PublicDnsName: ' + instance.PublicDnsName
              });
              content.append(vmContent);
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
    require('./lib/listAMIs.js')((err, result) => {
      loaded();
      if (err) {
        log('error', 'listAMIs cb err: ' + err);
      } else {
        const amiList = blessed.list({
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
        amiList.on('select', (ev, i) => {
          const amiId = result.amiIds[i];
          loading();
          require('./lib/listSubnets.js')((err, subnetIds) => {
            loaded();
            if (err) {
              log('error', 'listSubnets cb err: ' + err);
            } else {
              const subnetList = blessed.list({
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
              subnetList.on('select', (ev, i) => {
                loading();
                require('./lib/createVM.js')(amiId, subnetIds[i], (err) => {
                  loaded();
                  if (err) {
                    log('error', 'createVM cb err: ' + err);
                  } else {
                    const vmContent = blessed.box({  
                      fg: 'white',
                      bg: 'blue',
                      content: 'starting ...'
                    });
                    content.append(vmContent);
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
    require('./lib/listVMs.js')((err, instanceIds) => {
      loaded();
      if (err) {
        log('error', 'listVMs cb err: ' + err);
      } else {
        const instanceList = blessed.list({
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
        instanceList.on('select', (ev, i) => {
          loading();
          require('./lib/terminateVM.js')(instanceIds[i], (err) => {
            loaded();
            if (err) {
              log('error', 'terminateVM cb err: ' + err);
            } else {
              const vmContent = blessed.box({  
                fg: 'white',
                bg: 'blue',
                content: 'terminating ...'
              });
              content.append(vmContent);
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
};

screen.key('left', () => {  
  content.border.type = 'none';
  content.children.slice().forEach((child) => {
    content.remove(child);
  });
  list.border.type = 'line';
  list.focus();
  screen.render(); 
});

screen.key(['escape', 'q', 'C-c'], () => { 
  return process.exit(0);
});

let loadingInterval;

const loading = () => {
  progress.reset();
  clearInterval(loadingInterval);
  loadingInterval = setInterval(() => {
    if (progress.filled < 75) {
      progress.progress(progress.filled + 5);
    }
    screen.render(); 
  }, 200);
};

const loaded = () => {
  clearInterval(loadingInterval);
  progress.progress(100);
  screen.render(); 
};

const log = (level, message) => {
  screen.log('[' + level + ']: ' + message);
};

screen.render(); 
