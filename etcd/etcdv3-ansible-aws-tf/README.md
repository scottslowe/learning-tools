# Running an etcd v3 Cluster on Ubuntu 16.04 on AWS

These files were created to allow users to use Terraform and Ansible to set up an etcd v3 cluster on AWS.

## Contents

* **ansible.cfg**: This configuration file supplies parameters for Ansible. No changes to this file should be necessary.

* **data.tf**: This Terraform file supplies some information needed by the overall Terraform configuration (AMI IDs, primarily). No changes to this file should be necessary.

* **ec2.ini**: This configuration file controls the EC2 dynamic inventory module for Ansible. The only change to this file would be specifying the AWS region you are using (if it is not "us-west-2").

* **ec2.py**: This is the EC2 dynamic inventory module used by Ansible.

* **etcd.conf.j2**: This Jinja2 template is used to create an environment file for configuring etcd. No changes to this file should be necessary.

* **etcd.service:** This is a systemd unit file for etcd. No changes to this file should be necessary.

* **etcd.yml**: This is the Ansible playbook that will configure the EC2 instances created by Terraform to run etcd. No changes to this file should be necessary.

* **main.tf**: This Terraform file calls two other Terraform modules (found in `modules/vpc` and `modules/instance-cluster`) to launch EC2 instances into a new VPC and configure them with security groups to allow etcd traffic. No changes to this file should be necessary.

* **provider.tf**: This Terraform file configures the AWS provider. No changes to this file should be necessary.

* **README.md**: This file you're currently reading.

* **variables.tf**: This Terraform file defines the variables that Terraform expects the user to provide (either via the command line or via a `*.tfvars` file). No changes to this file should be necessary.

## Instructions

These instructions assume that Terraform, Ansible, and the AWS CLI are already installed and working correctly.

1. Place the files from the `etcdv3-ansible-aws-tf` directory of this GitHub repository (the "lowescott/learning-tools" repository) into a directory on your system. You can clone the entire "learning-tools" repository (using `git clone`), or just download the specific files from the `etcdv3-ansible-aws-tf` directory.

2. Create a `terraform.tfvars` file that defines the "user_region", "node_type", and "key_pair" variables. If you skip this step, you must provide these variables via the command-line when running other `terraform` commands.

3. Run `terraform init` to load the modules and ensure that the AWS provider has been downloaded and is available.

4. Run `terraform plan` to plan what Terraform is going to do.

5. Run `terraform apply` to have Terraform create the specified infrastructure.

6. Run `ansible-playbook etcd.yml` to have Ansible configure the EC2 instances created by Terraform to run etcd.

7. Once step 6 has completed, You can test etcd by logging into one of the instances via SSH and running this command:

		etcdctl member list
	
	This should return a list of three nodes as members of the etcd cluster. If you receive an error or don't see all three VMs listed, tear down the  environment with `terraform destroy` and recreate the environment from scratch. If you continue to experience problems, open an issue in the "learning-tools" repository on GitHub (or file a pull request fixing the problem).

8. When you're finished with the environment, run `terraform destroy` to tear everything down.

Enjoy!
