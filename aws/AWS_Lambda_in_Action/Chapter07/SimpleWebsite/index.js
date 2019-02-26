console.log('Loading function');

const fs = require('fs');
const ejs = require('ejs');

exports.handler = (event, context, callback) => {
  console.log('Received event:', JSON.stringify(event, null, 2));
  var fileName = './content' + event.path + 'index.ejs';
  console.log(fileName)
  fs.readFile(fileName, function(err, data) {
    if (err) {
      callback("Error 404");
    } else {
      var html = ejs.render(data.toString());
      callback(null, { data: html });
    }
  });
};
