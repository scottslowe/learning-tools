# "Instance Cluster" Module

These files provide an example of a pair of modules that can be used to create "instance clusters" (groups of clusters serving a common function/role).

## Contents

* **data.tf**: This Terraform configuration file provides information to Terraform on which AWS AMIs to use.

* **main.tf**: This Terraform configuration calls separate modules to create some VPCs and launch groups of instances ("instance clusters") in the VPCs.

* **modules**: This directory contains two Terraform modules ("vpc" and "instance-cluster"); these are called by `main.tf`.

* **provider.tf**: This Terraform configuration files configures the AWS provider.

* **README.md**: The file you're currently reading.

* **variables.tf**: This Terraform configuration file specifies variables that Terraform will need in order to create the desired AWS infrastructure.

## Instructions

These instructions assume that you have an AWS account, that Terraform is installed and working correctly, and that you've appropriately supplied the necessary AWS credentials to Terraform. (If the AWS CLI works, then Terraform will be able to use the same credentials.)

1. Place the files from the `terraform/aws/ic-module` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`) or just download the specific files from the `terraform/aws/ic-module` folder.

2. Create a file named `terraform.tfvars` and populate it with the name of the AWS keypair you'd like to use for the instances, the AWS region to use, and the type of the instances (such as "t2.micro"). Refer to [this URL](https://www.terraform.io/intro/getting-started/variables.html) for specific details on the syntax of this file. You can refer to the contents of `variables.tf` for the names of the variables that need to be defined.

3. Run `terraform init` to pull in the modules.

4. Run `terraform plan` to have Terraform examine the current infrastructure and determine what changes are necessary to realize the desired configuration.

5. Run `terraform apply` to have Terraform make the changes necessary to realize the desired configuration (by creating/modifying/deleting infrastructure).

Enjoy!

## License

This content is licensed under the MIT License.
