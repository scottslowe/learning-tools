# Building a Simple Terraform Module

These files provide an example of how to use Terraform _modules_ for repeatable infrastructure. In this case, the module automates the creation of a new VPC and associated infrastructure (subnet, route table, and Internet gateway).

## Contents

* **main.tf**: This Terraform configuration calls the `VPC` module to create two new VPCs.

* **modules**: This directory contains a Terraform module (which is itself a collection of Terraform configurations) to create a new VPC.

* **provider.tf**: This Terraform configuration files configures the AWS provider.

* **README.md**: The file you're currently reading.

* **variables.tf**: This Terraform configuration file specifies variables that Terraform will need in order to create the desired AWS infrastructure.

## Instructions

These instructions assume that you have an AWS account, that Terraform is installed and working correctly, and that you've appropriately supplied the necessary AWS credentials to Terraform.

1. Place the files from the `terraform/aws/simple-module` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`) or just download the specific files from the `terraform/aws/simple-module` folder.

2. Create a file named `terraform.tfvars` and populate it with the name of the AWS region to use. Refer to [this URL](https://www.terraform.io/intro/getting-started/variables.html) for specific details on the syntax of this file. You can refer to the contents of `variables.tf` for the names of the variables that need to be defined.

3. Run `terraform plan` to have Terraform examine the current infrastructure and determine what changes are necessary to realize the desired configuration.

4. Run `terraform apply` to have Terraform make the changes necessary to realize the desired configuration.

Enjoy!

## License

This content is licensed under the MIT License.
