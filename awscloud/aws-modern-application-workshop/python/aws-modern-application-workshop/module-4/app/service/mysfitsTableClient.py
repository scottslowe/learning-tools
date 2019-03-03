import boto3
import json
import logging
from collections import defaultdict

# create a DynamoDB client using boto3. The boto3 library will automatically
# use the credentials associated with our ECS task role to communicate with
# DynamoDB, so no credentials need to be stored/managed at all by our code!
client = boto3.client('dynamodb')

def getAllMysfits():

    # Retrieve all Mysfits from DynamoDB using the DynamoDB scan operation.
    # Note: The scan API can be expensive in terms of latency when a DynamoDB
    # table contains a high number of records and filters are applied to the
    # operation that require a large amount of data to be scanned in the table
    # before a response is returned by DynamoDB. For high-volume tables that
    # receive many requests, it is common to store the result of frequent/common
    # scan operations in an in-memory cache. DynamoDB Accelerator (DAX) or
    # use of ElastiCache can provide these benefits. But, because out Mythical
    # Mysfits API is low traffic and the table is very small, the scan operation
    # will suit our needs for this workshop.
    response = client.scan(
        TableName='MysfitsTable'
    )

    logging.info(response["Items"])

    # loop through the returned mysfits and add their attributes to a new dict
    # that matches the JSON response structure expected by the frontend.
    mysfitList = defaultdict(list)
    for item in response["Items"]:
        mysfit = {}
        mysfit["mysfitId"] = item["MysfitId"]["S"]
        mysfit["name"] = item["Name"]["S"]
        mysfit["goodevil"] = item["GoodEvil"]["S"]
        mysfit["lawchaos"] = item["LawChaos"]["S"]
        mysfit["species"] = item["Species"]["S"]
        mysfit["thumbImageUri"] = item["ThumbImageUri"]["S"]
        mysfitList["mysfits"].append(mysfit)

    # convert the create list of dicts in to JSON
    return json.dumps(mysfitList)

def queryMysfits(queryParam):

    logging.info(json.dumps(queryParam))

    # Use the DynamoDB API Query to retrieve mysfits from the table that are
    # equal to the selected filter values.
    response = client.query(
        TableName='MysfitsTable',
        IndexName=queryParam['filter']+'Index',
        KeyConditions={
            queryParam['filter']: {
                'AttributeValueList': [
                    {
                        'S': queryParam['value']
                    }
                ],
                'ComparisonOperator': "EQ"
            }
        }
    )

    mysfitList = defaultdict(list)
    for item in response["Items"]:
        mysfit = {}
        mysfit["mysfitId"] = item["MysfitId"]["S"]
        mysfit["name"] = item["Name"]["S"]
        mysfit["goodevil"] = item["GoodEvil"]["S"]
        mysfit["lawchaos"] = item["LawChaos"]["S"]
        mysfit["species"] = item["Species"]["S"]
        mysfit["thumbImageUri"] = item["ThumbImageUri"]["S"]
        mysfitList["mysfits"].append(mysfit)

    return json.dumps(mysfitList)

# Retrive a single mysfit from DynamoDB using their unique mysfitId
def getMysfit(mysfitId):

    # use the DynamoDB API GetItem, which gives you the ability to retrieve
    # a single item from a DynamoDB table using its unique key with super
    # low latency.
    response = client.get_item(
        TableName='MysfitsTable',
        Key={
            'MysfitId': {
                'S': mysfitId
            }
        }
    )

    item = response["Item"]

    mysfit = {}
    mysfit["mysfitId"] = item["MysfitId"]["S"]
    mysfit["name"] = item["Name"]["S"]
    mysfit["age"] = int(item["Age"]["N"])
    mysfit["goodevil"] = item["GoodEvil"]["S"]
    mysfit["lawchaos"] = item["LawChaos"]["S"]   
    mysfit["species"] = item["Species"]["S"]
    mysfit["description"] = item["Description"]["S"]
    mysfit["thumbImageUri"] = item["ThumbImageUri"]["S"]
    mysfit["profileImageUri"] = item["ProfileImageUri"]["S"]
    mysfit["likes"] = item["Likes"]["N"]
    mysfit["adopted"] = item["Adopted"]["BOOL"]

    return json.dumps(mysfit)

# increment the number of likes for a mysfit by 1
def likeMysfit(mysfitId):

    # Use the DynamoDB API UpdateItem to increment the number of Likes
    # the mysfit has by 1 using an UpdateExpression.
    response = client.update_item(
        TableName='MysfitsTable',
        Key={
            'MysfitId': {
                'S': mysfitId
            }
        },
        UpdateExpression="SET Likes = Likes + :n",
        ExpressionAttributeValues={':n': {'N': '1'}}
    )

    response = {}
    response["Update"] = "Success";

    return json.dumps(response)

# mark a mysfit as adopted
def adoptMysfit(mysfitId):

    # Use the DynamoDB API UpdateItem to set the value of the mysfit's
    # Adopted attribute to True using an UpdateExpression.
    response = client.update_item(
        TableName='MysfitsTable',
        Key={
            'MysfitId': {
                'S': mysfitId
            }
        },
        UpdateExpression="SET Adopted = :b",
        ExpressionAttributeValues={':b': {'BOOL': True}}
    )

    response = {}
    response["Update"] = "Success";

    return json.dumps(response)
