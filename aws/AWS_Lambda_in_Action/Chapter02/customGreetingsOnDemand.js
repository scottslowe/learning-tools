console.log('Loading function');

exports.handler = (event, context, callback) => {
    console.log('Received event:',
        JSON.stringify(event, null, 2));
    console.log('greet =', event.greet);
    console.log('name =', event.name);
    var greet = '';
    if ('greet' in event) {
        greet = event.greet;
    } else {
        greet = "Hello";
    }
    var name = '';
    if ('name' in event) {
        name = event.name;
    } else {
        name = "World";
    }
    var greetings = greet + ' ' + name + '!';
    console.log(greetings);
    callback(null, greetings);
};
