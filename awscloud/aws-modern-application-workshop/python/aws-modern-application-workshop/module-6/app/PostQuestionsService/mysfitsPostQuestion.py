import boto3
import json
import logging
import uuid
import random
import time
# UNCOMMENT_BEFORE_2ND_DEPLOYMENT - below 2 lines
#from aws_xray_sdk.core import xray_recorder
#from aws_xray_sdk.core import patch_all

# UNCOMMENT_BEFORE_2ND_DEPLOYMENT - below 1 line
#patch_all()

ddb = boto3.client('dynamodb')
logger = logging.getLogger()
logger.setLevel(logging.INFO)

def postQuestion(event, context):

    logger.info(event)

    payload = json.loads(event["body"])

    question = {}
    question["QuestionId"] = {
        "S": str(uuid.uuid4())
        }
    question["QuestionText"] = {
        "S": payload["questionText"]
        }
    question["UserEmailAddress"] = {
        "S": payload["email"]
        }

    # UNCOMENT_BEFORE_3RD_DEPLOYMENT - below 4 lines
    #throwError = random.randint(0,5)
    #if throwError:
    #   errorMsg = 'FATAL: This question did not get entered into the database. Email Address: ' + payload["email"]  + " Question Text: " + payload["questionText"]
    #   hangingException(errorMsg)

    ddb.put_item(
        TableName="MysfitsQuestionsTable",
        Item=question
        )

    response = {}
    response["headers"] = {"Access-Control-Allow-Origin": "*", "Access-Control-Allow-Headers": "*", "Access-Control-Allow-Methods": "*"}
    response["statusCode"] = 200
    responseBody = {}
    responseBody["status"] = "success"
    response["body"] =  json.dumps(responseBody)
    logger.info(response)
    return response

# UNCOMENT_BEFORE_3RD_DEPLOYMENT - below 4 lines
#@xray_recorder.capture('hangingException')
#def hangingException(msg):
#    time.sleep(5)
#    raise Exception(msg)
