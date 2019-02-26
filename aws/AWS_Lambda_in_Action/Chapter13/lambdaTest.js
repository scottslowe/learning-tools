'use strict';

let AWS = require('aws-sdk');
let doc = require('dynamodb-doc');

let lambda = new AWS.Lambda({ apiVersion: '2015-03-31' });
let dynamo = new doc.DynamoDB();


// Asynchronously runs a given function X times
const asyncAll = (opts) => {
    let i = -1;
    const next = () => {
        i++;
        if (i === opts.times) {
            opts.done();
            return;
        }
        opts.fn(next, i);
    };
    next();
};


const unit = (event, callback) => {
    const lambdaParams = {
        FunctionName: event.function,
        Payload: JSON.stringify(event.event)
    };
    lambda.invoke(lambdaParams, (err, data) => {
        if (err) {
            return callback(err);
        }
        // Write result to Dynamo
        const dynamoParams = {
            TableName: event.resultsTable,
            Item: {
                testId: event.testId,
                iteration: event.iteration || 0,
                result: data.Payload,
                passed: !JSON.parse(data.Payload).hasOwnProperty('errorMessage')
            }
        };
        dynamo.putItem(dynamoParams, callback);
    });
};

const load = (event, callback) => {
    const payload = event.event;
    asyncAll({
        times: event.iterations,
        fn: (next, i) => {
            payload.iteration = i;
            const lambdaParams = {
                FunctionName: event.function,
                InvocationType: 'Event',
                Payload: JSON.stringify(payload)
            };
            lambda.invoke(lambdaParams, (err, data) => next());
        },
        done: () => callback(null, 'Load test complete')
    });
};

const ops = {
    unit: unit,
    load: load
};

exports.handler = (event, context, callback) => {
    if (ops.hasOwnProperty(event.operation)) {
        ops[event.operation](event, callback);
    } else {
        callback(`Unrecognized operation "${event.operation}"`);
    }
};
