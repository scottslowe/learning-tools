# Instructions for Using this Demo Environment

## Prerequisites

This demo environment assumes that you have the following software already installed on your system:

* Terraform 0.9.x (tested with 0.9.2, should work with any 0.9.x version)
* Ansible 2.x (tested with Ansible 2.3.0)
* Git 2.x (should work with any recent version of Git)

This demo also assumes that you've properly configured your AWS credentials. (If the `aws` command-line tools work, then your credentials are properly configured.)

Before trying to use this demo environment, please ensure that you've installed the necessary prerequisites.

This demo environment was tested on Fedora Linux, but it should work without any issues on any Linux distribution or on recent versions of OS X. I don't know what would be required to make it work on Windows, sorry.

## Setup for the Demo Environment

1. Open a terminal and navigate to the directory where you'd like to store the files for this demo environment.

2. Run `git clone https://github.com/lowescott/2017-itx-container-workshop` to clone the repository to the current directory.

3. Switch into the directory for this demo environment (`cd 2017-itx-container-workshop/ec2-swarm`).

4. Edit `terraform.tf` and specify a valid name for the S3 bucket and the name/path of the file where Terraform should store its state. If you want to use a region other than "us-west-2", you also need to specify the AWS region.

5. Run `terraform init` to configure the S3 storage for Terraform state. Address any errors that may be reported.

6. Edit `ec2.ini` and specify the AWS region(s) you'll be using. (If you are using the "us-west-2" region, you may leave this file unmodified.)

7. Edit `vars.tf` to specify the name of an AWS keypair to use. If you want to use a region other than "us-west-2", you'll also need to specify the name of the AWS region to use.

The demo environment is now ready to be created.

## Creating the Demo Environment

1. In the `ec2-swarm` directory where the repository was cloned, run `terraform plan` to evaluate the requested Terraform configuration against your current AWS infrastructure.

2. Run `terraform apply` to create all the necessary AWS infrastructure.

3. Run `./ec2.py --refresh-cache` to refresh the Ansible inventory. This will query AWS and return information about the AWS infrastructure that was just created by Terraform.

4. Run `ansible-playbook create-swarm.yml` to create the Swarm cluster.

At this point you now have a working demo environment.

## Deploying the Demo Application

1. Connect to the manager node using SSH with the command `ssh -i <Path to SSH Key file> ubuntu@<public IP of manager>`. You can determine the public IP of the manager node using the `ec2.py` inventory script, if necessary.

2. While logged into the manager node, run `docker stack deploy --compose-file docker-stack.yml demo` to deploy the sample application.

3. Use a local browser to connect to port 8888 of the manager node's public IP address to show the visualizer.

4. Use a local browser to connect to port 8080 of the manager node's public IP address to show WordPress.

5. You can scale up the number of containers using the `docker service scale` command. The visualizer will reflect the additional containers that are deployed. Use `docker service ls` to list the services.

You now have a working demo application.

## Destroying Only the Swarm Cluster

If you'd like to tear down the Swarm cluster but leave the AWS infrastructure intact, follow these steps:

1. Open a terminal and switch into the `ec2-swarm` directory of the cloned repository.

2. Run `ansible-playbook destroy-swarm.yml` to destroy the Swarm cluster.

The Swarm cluster will be destroyed, but the AWS infrastructure will remain active. You can use `ansible-playbook create-swarm.yml` to create a new Swarm using the same infrastructure.

## Destroying the Entire Demo Environment

To destroy the entire demo environment, simply run `terraform destroy` from the `ec2-swarm` directory.
