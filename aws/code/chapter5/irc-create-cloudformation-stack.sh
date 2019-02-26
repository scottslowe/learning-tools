#!/bin/bash -ex

vpc=$(aws ec2 describe-vpcs --filter "Name=isDefault, Values=true" --query "Vpcs[0].VpcId" --output text)
aws cloudformation create-stack --stack-name irc --template-url https://s3.amazonaws.com/awsinaction/chapter5/irc-cloudformation.json --parameters ParameterKey=VPC,ParameterValue=$vpc

while [[ `aws cloudformation describe-stacks --stack-name irc --query Stacks[0].StackStatus` != *"COMPLETE"* ]]
do
	sleep 10
done
