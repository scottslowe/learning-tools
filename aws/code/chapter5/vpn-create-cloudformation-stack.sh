#!/bin/bash -ex

vpc=$(aws ec2 describe-vpcs --filter "Name=isDefault, Values=true" --query "Vpcs[0].VpcId" --output text)
subnet=$(aws ec2 describe-subnets --filters Name=vpc-id,Values=$vpc --query Subnets[0].SubnetId --output text)
sharedsecret=$(openssl rand -base64 30)
user=vpn
password=$(openssl rand -base64 30)

aws cloudformation create-stack --stack-name vpn --template-url https://s3.amazonaws.com/awsinaction/chapter5/vpn-cloudformation.json --parameters ParameterKey=KeyName,ParameterValue=mykey ParameterKey=VPC,ParameterValue=$vpc ParameterKey=Subnet,ParameterValue=$subnet ParameterKey=IPSecSharedSecret,ParameterValue=$sharedsecret ParameterKey=VPNUser,ParameterValue=$user ParameterKey=VPNPassword,ParameterValue=$password

while [[ `aws cloudformation describe-stacks --stack-name vpn --query Stacks[0].StackStatus` != *"COMPLETE"* ]]
do
	sleep 10
done
aws cloudformation describe-stacks --stack-name vpn --query Stacks[0].Outputs
