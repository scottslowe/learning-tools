
import boto3
ec2 = boto3.client('ec2')

def lambda_handler(event, context):

  userName = event['detail']['userIdentity']['arn'].split('/')[1]
  instanceId = event['detail']['responseElements']['instancesSet']['items'][0]['instanceId']    
  print("Adding owner tag " + userName + " to instance " + instanceId + ".")
  ec2.create_tags(Resources=[instanceId,],Tags=[{'Key': 'Owner', 'Value': userName},])
  return
