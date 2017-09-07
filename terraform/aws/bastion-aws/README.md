# SSH Bastion Hosts on AWS with Terraform

These files provide an example of how to use Terraform to create a "private" EC2 subnet where instances can only be reached via use of an SSH bastion host. Refer to [this blog post](http://blog.scottlowe.org/2015/11/21/using-ssh-bastion-host/) for more information on SSH bastion hosts.

## Contents

* **data.tf**: This Terraform configuration file provides information to Terraform on which AWS AMIs to use.

* **instances.tf**: This Terraform configuration file specifies the AWS instances to create.

* **networking.tf**: This Terraform configuration creates all the networking components: the VPC (Virtual Private Cloud), subnets, Internet gateway, and the route table.

* **output.tf**: This Terraform configuration file supplies information about the instances created by Terraform (public and private IP addresses, as appropriate).

* **provider.tf**: This Terraform configuration files configures the AWS provider.

* **README.md**: The file you're currently reading.

* **security.tf**: Security groups and security group rules are defined in this Terraform configuration file.

* **ssh.cfg.example**: This is an example of the configuration stanzas that may be needed to support the use of an SSH bastion host.

* **variables.tf**: This Terraform configuration file specifies variables that Terraform will need in order to create the desired AWS infrastructure.

## Instructions

These instructions assume that you have an AWS account, that Terraform is installed and working correctly, and that you've appropriately supplied the necessary AWS credentials to Terraform.

1. Place the files from the `terraform/aws/bastion-aws` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`) or just download the specific files from the `terraform/aws/bastion-aws` folder.

2. Create a file named `terraform.tfvars` and populate it with the name of the AWS keypair you'd like to use for the instances, the AWS region to use, and the type (like "t2.micro") of the instances. Refer to [this URL](https://www.terraform.io/intro/getting-started/variables.html) for specific details on the syntax of this file. You can refer to the contents of `variables.tf` for the names of the variables that need to be defined.

3. Run `terraform plan` to have Terraform examine the current infrastructure and determine what changes are necessary to realize the desired configuration.

4. Run `terraform apply` to have Terraform make the changes necessary to realize the desired configuration.

5. When Terraform is done running, it will output the public and private IP addresses of the bastion along with the private IP address of the remote host. Use these values, as appropriate, to configure your SSH client. You can refer to the file `ssh.cfg.example` for an example of how your SSH configuration _may_ need to be modified.

6. When your SSH client is properly configured, attempt to connect via SSH to the bastion host. It should work as expected.

7. Now attempt to connect via SSH to the private host's private (non-routable) IP address. This should also work, as the connection is operating via the bastion host.

Enjoy!

## License

This content is licensed under the MIT License.
