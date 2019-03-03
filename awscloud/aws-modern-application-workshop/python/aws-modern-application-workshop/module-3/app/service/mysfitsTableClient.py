import boto3
import json
import logging
from collections import defaultdict
import argparse

# create a DynamoDB client using boto3. The boto3 library will automatically
# use the credentials associated with our ECS task role to communicate with
# DynamoDB, so no credentials need to be stored/managed at all by our code!
client = boto3.client('dynamodb')

def getMysfitsJson(items):
    # loop through the returned mysfits and add their attributes to a new dict
    # that matches the JSON response structure expected by the frontend.
    mysfitList = defaultdict(list)

    for item in items:
        mysfit = {}

        mysfit["mysfitId"] = item["MysfitId"]["S"]
        mysfit["name"] = item["Name"]["S"]
        mysfit["species"] = item["Species"]["S"]
        mysfit["description"] = item["Description"]["S"]
        mysfit["age"] = int(item["Age"]["N"])
        mysfit["goodevil"] = item["GoodEvil"]["S"]
        mysfit["lawchaos"] = item["LawChaos"]["S"]
        mysfit["thumbImageUri"] = item["ThumbImageUri"]["S"]
        mysfit["profileImageUri"] = item["ProfileImageUri"]["S"]
        mysfit["likes"] = item["Likes"]["N"]
        mysfit["adopted"] = item["Adopted"]["BOOL"]

        mysfitList["mysfits"].append(mysfit)

    return mysfitList

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
    mysfitList = getMysfitsJson(response["Items"])

    return json.dumps(mysfitList)

def queryMysfitItems(filter, value):
    # Use the DynamoDB API Query to retrieve mysfits from the table that are
    # equal to the selected filter values.
    response = client.query(
        TableName='MysfitsTable',
        IndexName=filter+'Index',
        KeyConditions={
            filter: {
                'AttributeValueList': [
                    {
                        'S': value
                    }
                ],
                'ComparisonOperator': "EQ"
            }
        }
    )

    # loop through the returned mysfits and add their attributes to a new dict
    # that matches the JSON response structure expected by the frontend.
    mysfitList = getMysfitsJson(response["Items"])

    # convert the create list of dicts in to JSON
    return json.dumps(mysfitList)

def queryMysfits(queryParam):

    logging.info(json.dumps(queryParam))

    filter = queryParam['filter']
    value = queryParam['value']

    return queryMysfitItems(filter, value)

# So we can test from the command line
if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('-f', '--filter')
    parser.add_argument('-v', '--value')
    args = parser.parse_args()

    filter = args.filter
    value = args.value

    if args.filter and args.value:
        print 'filter is '+args.filter
        print 'value is '+args.value

        print "Getting filtered values"
        items = queryMysfitItems(args.filter, args.value)
    else:
        print "Getting all values"
        items = getAllMysfits()

    print items
