# The code to be used as an AWS Lambda function for processing real-time
# user click records from Kinesis Firehose and adding additional attributes
# to them before they are stored in Amazon S3.
from __future__ import print_function

import base64

import json

import requests

# Send a request to the Mysfits Service API that we have created in previous
# modules to retrieve all of the attributes for the included MysfitId.
def retrieveMysfit(mysfitId):
    apiEndpoint = 'REPLACE_ME_API_ENDPOINT' + '/mysfits/' + str(mysfitId) # eg: 'https://ljqomqjzbf.execute-api.us-east-1.amazonaws.com/prod/'
    mysfit = requests.get(apiEndpoint).json()
    return mysfit

# The below method will serve as the "handler" for the Lambda function. The
# handler is the method that AWS Lambda will invoke with events, which in this
# case will include records from the Kinesis Firehose Delivery Stream.
def processRecord(event, context):
    output = []

    # retrieve the list of records included with the event and loop through
    # them to retrieve the full list of mysfit attributes and add the additional
    # attributes that a hypothetical BI/Analyitcs team would like to analyze.
    for record in event['records']:
        print('Processing record: ' + record['recordId'])
        # kinesis firehose expects record payloads to be sent as encoded strings,
        # so we must decode the data first to retrieve the click record.
        click = json.loads(base64.b64decode(record['data']))

        mysfitId = click['mysfitId']
        mysfit = retrieveMysfit(mysfitId)

        enrichedClick = {
                'userId': click['userId'],
                'mysfitId': mysfitId,
                'goodevil': mysfit['goodevil'],
                'lawchaos': mysfit['lawchaos'],
                'species': mysfit['species']
            }

       # create the output record that Kinesis Firehose will store in S3.
        output_record = {
            'recordId': record['recordId'],
            'result': 'Ok',
            'data': base64.b64encode(json.dumps(enrichedClick).encode('utf-8') + b'\n').decode('utf-8')
        }
        output.append(output_record)

    print('Successfully processed {} records.'.format(len(event['records'])))

    # return the enriched records to Kiesis Firehose.
    return {'records': output}
