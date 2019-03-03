import os
import json
import boto3

client = boto3.client('sagemaker-runtime')
endpoint_name = "REPLACE_ME_SAGEMAKER_ENDPOINT_NAME" # eg. knn-ml-m4-xlarge-123456789-123456
content_type = "application/jsonlines"
accept = "application/json"

def recommend(event, context):

    print(event)

    payload = {
        "features": json.loads(event['body'])['entry']
    }

    smResponse = client.invoke_endpoint(
        EndpointName=endpoint_name,
        ContentType=content_type,
        Accept=accept,
        Body=json.dumps(payload)
    )

    smResponseBody = json.loads(smResponse['Body'].read())
    encodedMysfit = smResponseBody['predictions'][0]['predicted_label']

    # NOTE: In a real-world scenario if you used a machine learning algorithm
    # that requires numeric values like kNN does, you would not hard-code the
    # encoded values as we've done here below.  The encoding value for each
    # mysfit would likely be stored as metadata wtihin a microservice relevant
    # to the machine learning data curation process. For the purposes of this
    # overview/introductory tutorial, we've decided to instead hardcode the
    # encoding values here below so that this module can remain a short introduction
    # to using SageMaker, rather than incorperate full production-readiness
    # into the architecture.

    decodedMysfitId = ''

    if encodedMysfit == 0:
        decodedMysfitId = '4e53920c-505a-4a90-a694-b9300791f0ae'
    elif encodedMysfit == 1:
        decodedMysfitId = '2b473002-36f8-4b87-954e-9a377e0ccbec'
    elif encodedMysfit == 2:
        decodedMysfitId = '0e37d916-f960-4772-a25a-01b762b5c1bd'
    elif encodedMysfit == 3:
        decodedMysfitId = 'da5303ae-5aba-495c-b5d6-eb5c4a66b941'
    elif encodedMysfit == 4:
        decodedMysfitId = 'a901bb08-1985-42f5-bb77-27439ac14300'
    elif encodedMysfit == 5:
        decodedMysfitId = 'b41ff031-141e-4a8d-bb56-158a22bea0b3'
    elif encodedMysfit == 6:
        decodedMysfitId = '3f0f196c-4a7b-43af-9e29-6522a715342d'
    elif encodedMysfit == 7:
        decodedMysfitId = 'a68db521-c031-44c7-b5ef-bfa4c0850e2a'
    elif encodedMysfit == 8:
        decodedMysfitId = 'c0684344-1eb7-40e7-b334-06d25ac9268c'
    elif encodedMysfit == 9:
        decodedMysfitId = 'ac3e95f3-eb40-4e4e-a605-9fdd0224877c'
    elif encodedMysfit == 10:
        decodedMysfitId = '33e1fbd4-2fd8-45fb-a42f-f92551694506'
    elif encodedMysfit == 11:
        decodedMysfitId = 'b6d16e02-6aeb-413c-b457-321151bb403d'

    responseBody = {
        "recommendedMysfit": decodedMysfitId
    }


    response = {}
    response["headers"] = {"Access-Control-Allow-Origin": "*", "Access-Control-Allow-Headers": "*", "Access-Control-Allow-Methods": "*"}
    response["statusCode"] = 200
    response['body'] = json.dumps(responseBody)

    return response
