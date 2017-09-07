# Launching an Instance with Terraform

These files provide an example of a very simple Terraform configuration that will launch an instance into an existing VPC/subnet (the user's default VPC for the specified region).

## Contents

* **data.tf**: This Terraform configuration file provides information to Terraform on which AWS AMIs to use.

* **main.tf**: This Terraform configuration file launches an instance into the user's default VPC for the specified region.

* **output.tf**: This Terraform configuration file supplies information about the instance launched by Terraform (public IP address, specifically).

* **provider.tf**: This Terraform configuration files configures the AWS provider.

* **README.md**: The file you're currently reading.

* **variables.tf**: This Terraform configuration file specifies variables that Terraform will need in order to create the desired AWS infrastructure.

## Instructions

These instructions assume that you have an AWS account, that Terraform is installed and working correctly, and that you've appropriately supplied the necessary AWS credentials to Terraform.

1. Place the files from the `terraform/aws/simple-ec2` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`) or just download the specific files from the `terraform/aws/simple-ec2` folder.

2. Create a file named `terraform.tfvars` and populate it with the name of the AWS keypair you'd like to use for the instance, the AWS region you'd like Terraform to use, the security group with which the instance should be associated, and the type of the instance (such as "t2.micro"). Refer to [this URL](https://www.terraform.io/intro/getting-started/variables.html) for specific details on the syntax of this file. You can refer to the contents of `variables.tf` for the names of the variables that need to be defined.

3. Run `terraform plan` to have Terraform examine the current infrastructure and determine what changes are necessary to realize the desired configuration.

4. Run `terraform apply` to have Terraform make the changes necessary to realize the desired configuration.

5. When Terraform is done running, it will output the public IP address of the instance that was launched. Use this value, as appropriate, to connect to the instance via SSH.

Enjoy!

## License

This content is licensed under the MIT License.
