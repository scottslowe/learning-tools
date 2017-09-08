# Auto Scaling Group with Elastic Load Balancer

These files provide an example of a simple configuration with an Auto Scaling Group (ASG) and an Elastic Load Balancer (ELB).

## Contents

* **data.tf**: This Terraform configuration file provides information to Terraform on which AWS AMIs to use.

* **main.tf**: This Terraform configuration file creates an Elastic Load Balancer (ELB), Launch Configuration (LC), and Auto Scaling Group (ASG).

* **output.tf**: This Terraform configuration outputs information about the resources created (the ELB DNS name, specifically).

* **provider.tf**: This Terraform configuration files configures the AWS provider.

* **README.md**: The file you're currently reading.

* **variables.tf**: This Terraform configuration file specifies variables that Terraform will need in order to create the desired AWS infrastructure.

## Instructions

These instructions assume that you have an AWS account, that Terraform is installed and working correctly, and that you've appropriately supplied the necessary AWS credentials to Terraform.

1. Place the files from the `terraform/aws/asg-elb` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`) or just download the specific files from the `terraform/aws/asg-elb` folder.

2. Create a file named `terraform.tfvars` and populate it with the name of the AWS keypair you'd like to use for the instances, the AWS region to use, the ID of the security group you'd like to use, and the flavor of the instances (such as "t2.micro"). Refer to [this URL](https://www.terraform.io/intro/getting-started/variables.html) for specific details on the syntax of this file. You can refer to the contents of `variables.tf` for the names of the variables that need to be defined.

3. Run `terraform plan` to have Terraform examine the current infrastructure and determine what changes are necessary to realize the desired configuration.

4. Run `terraform apply` to have Terraform make the changes necessary to realize the desired configuration.

5. When Terraform is done running, it will output the DNS name of the Elastic Load Balancer (ELB). You can now use this DNS name to make load-balanced SSH connections to the back-end instances.

Enjoy!

## License

This content is licensed under the MIT License.
