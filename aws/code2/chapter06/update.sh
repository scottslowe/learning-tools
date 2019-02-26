#!/bin/bash -ex

PUBLICIPADDRESSESS="$(aws ec2 describe-instances --filters "Name=instance-state-name,Values=running" --query "Reservations[].Instances[].PublicIpAddress" --output text)"

for PUBLICIPADDRESS in $PUBLICIPADDRESSESS; do
  ssh -t "ec2-user@$PUBLICIPADDRESS" "sudo yum -y --security update"
done
