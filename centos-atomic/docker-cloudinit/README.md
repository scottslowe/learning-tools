# Instructions

1. Capture the VPC ID of your default VPC using this command:

        VPC_ID=$(aws --output text ec2 describe-vpcs \
        --filters Name=isDefault,Values="true" \
        --query 'Vpcs[0].VpcId')

2. Capture the subnet ID of one of the subnets (the first, by default) in the default VPC using this command (step 1 must be completed first):

        SN_ID=$(aws --output text ec2 describe-subnets \
        --filters Name=vpc-id,Values="$VPC_ID" \
        --query 'sort_by(Images,&AvailabilityZone)[0].SubnetId')

3. Capture the security group ID of a security group in the default VPC with the name "default" (it is assumed that this security group exists and allows SSH access to the instance; if this is not the case, you must fix this outside of this process):

        SG_ID=$(aws --output text ec2 describe-security-groups \
        --filters Name=group-name,Values="default" \
        Name=vpc-id,Values="$VPC_ID" \
        --query 'SecurityGroups[0].GroupId')

4. Finally, capture the image ID of the latest version of the CentOS 7 Atomic Host AMI using this command:

        IMAGE_ID=$(aws --output text ec2 describe-images \
        --owners 410186602215 --filter Name=name,Values="*CentOS Atomic*" \
        --query 'sort_by(Images,&CreationDate)[-1].ImageId')

5. Make sure you have an SSH key available to use with the instance and make note of the name of the SSH key.

6. Launch an AWS instance using this command:

        aws ec2 run-instances --image-id $IMAGE_ID --instance-type t2.micro \
        --key-name keyname --user-data file://cloud-config.yml \
        --subnet-id $SN_ID --security-group-ids $SG_ID

7. Connect to the instance using SSH.

8. Verify that the Docker daemon is listening over a network socket (not the default configuration) by running `ss -lnt` and/or running `docker` commands against the network socket (via `-H tcp://127.0.0.1:2375`).
