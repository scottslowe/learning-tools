#!/usr/bin/env bash
#
# This script assumes AWSCLI is installed and configured correctly

# Set some variables to be used later
TYPE="t2.micro"
KEYNAME="aws_rsa"

# First, capture the ID of the user's default VPC
VPC_ID=$(aws --output text ec2 describe-vpcs \
         --filters Name=isDefault,Values=true \
         --query 'Vpcs[0].VpcId')

# Use the captured VPC_ID to get the subnet ID of the first subnet
# in the first (sorted alphabetically) availability zone in the user's
# configured region
SN_ID=$(aws --output text ec2 describe-subnets \
        --filters Name=vpc-id,Values="$VPC_ID" \
        --query 'sort_by(Subnets,&AvailabilityZone)[0].SubnetId')

# Capture the ID of the security group named "default" in the
# user's default VPC.
SG_ID=$(aws --output text ec2 describe-security-groups \
        --filters Name=group-name,Values="default" \
        Name=vpc-id,Values="$VPC_ID" \
        --query 'SecurityGroups[0].GroupId')

# Capture the AMI ID for the latest version of CentOS 7 Atomic Host
IMG_ID=$(aws --output text ec2 describe-images \
         --owners 410186602215 \
         --filters Name=name,Values="*CentOS Atomic*" \
         --query 'sort_by(Images,&CreationDate)[-1].ImageId')

# Launch an instance using the captured information from above (no cloud-init)
#aws ec2 run-instances --image-id $IMG_ID --instance-type $TYPE \
#--key-name $KEYNAME --subnet-id $SN_ID --security-group-ids $SG_ID

# Launch an instance using the captured information from above (with cloud-init)
aws ec2 run-instances --image-id $IMG_ID --instance-type $TYPE \
--key-name $KEYNAME --user-data file://cloud-config.yml \
--subnet-id $SN_ID --security-group-ids $SG_ID
