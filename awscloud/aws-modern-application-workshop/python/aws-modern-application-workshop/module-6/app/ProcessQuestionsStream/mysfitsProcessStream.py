import boto3
import json
import os
# UNCOMMENT_BEFORE_2ND_DEPLOYMENT
#from aws_xray_sdk.core import xray_recorder

# UNCOMMENT_BEFORE_2ND_DEPLOYMENT
#from aws_xray_sdk.core import patch_all

# UNCOMMENT_BEFORE_2ND_DEPLOYMENT
#patch_all()

snsTopic = os.environ['SNS_TOPIC_ARN']
sns = boto3.client('sns')

def processStream(event, context):

    emailSubject = "New Customer Question: "
    emailMessage = "USER EMAIL: "

    print("Received event: " + json.dumps(event))

    for record in event['Records']:

        item = record.get('dynamodb').get('NewImage')
        questionText = item.get('QuestionText').get('S')
        userEmail = item.get('UserEmailAddress').get('S')

        emailSubject = emailSubject + userEmail
        emailMessage = emailMessage + userEmail + ". QUESTION TEXT: " + questionText

        sns.publish(
            TopicArn=snsTopic,
            Message=emailMessage,
            Subject=emailSubject
            )




    return 'Successfully processed {} records.'.format(len(event['Records']))
