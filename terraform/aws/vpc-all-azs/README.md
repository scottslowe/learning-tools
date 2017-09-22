# Creating Subnets and Instances on All Availability Zones

These files provide an example of how to use Terraform data sources and functions to create subnets on all availability zones (AZs) within a user-specified region, and then launch instances spread across the subnets.

## Contents

* **compute.tf**: This Terraform configuration file launches the instances across the subnets within the VPC.

* **data.tf**: This Terraform configuration provides the data sources used by other configuration files (the AZs within a region and the AMI to use to launch an instance).

* **networking.tf**: This Terraform configuration creates a new VPC and then creates a subnet in each AZ within the region. It also creates an Internet gateway and programs a route in the main route table for the VPC.

* **provider.tf**: This Terraform configuration files configures the AWS provider.

* **README.md**: The file you're currently reading.

* **variables.tf**: This Terraform configuration file specifies variables that Terraform will need in order to create the desired AWS infrastructure.

## Instructions

These instructions assume that you have an AWS account, that Terraform is installed and working correctly, and that you've appropriately supplied the necessary AWS credentials to Terraform. If the AWS CLI works properly, then Terraform can use those credentials to connect to AWS.

1. Place the files from the `terraform/aws/vpc-all-azs` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`) or just download the specific files from the `terraform/aws/vpc-all-azs` folder.

2. Create a file named `terraform.tfvars` and populate it with the name of the AWS region to use, a CIDR block to use (a /16 block is recommended), the instance type to use, the SSH keypair to inject into the instances, and the number of instances you'd like to launch. Refer to [this URL](https://www.terraform.io/intro/getting-started/variables.html) for specific details on the syntax of this file. You can refer to the contents of `variables.tf` for the names of the variables that need to be defined.

3. Run `terraform plan` to have Terraform examine the current infrastructure and determine what changes are necessary to realize the desired configuration.

4. Run `terraform apply` to have Terraform make the changes necessary to realize the desired configuration. Once Terraform has finished executing, you can use SSH (with the specified key pair) to log into one or more of the instances.

Enjoy!

## License

This content is licensed under the MIT License.
