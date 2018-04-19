# Using Ansible to Bootstrap Ansible

These files provide an example of how to use Ansible to bootstrap systems that don't have the necessary Python dependencies installed for full Ansible support. One example is Ubuntu 16.04, which lacks Python 2 support.

## Contents

* **ansible.cfg**: This file tells Ansible where to find the default inventory information (it leverages a dynamic inventory script for AWS).

* **bootstrap.yml**: This Ansible playbook uses the `raw` module to install Python 2 support, which in turn will enable full Ansible support.

* **compute.tf**: This is a Terraform configuration that launches an Ubuntu 16.04 instance on AWS.

* **datasources.tf**: This Terraform configuration looks up the AMI ID for Ubuntu 16.04 and passes it to `compute.tf`.

* **ec2.ini**: This file is the configuration file for the dynamic inventory script. Edit the `regions=` line in this file to specify the AWS regions where instances may be running.

* **ec2.py**: This is a dynamic inventory script to query AWS APIs and generate an inventory that Ansible can use. No edits are needed to this file.

* **provider.tf**: This file configures the AWS provider for Terraform.

* **README.md**: The file you're currently reading.

* **variables.tf**: This file defines the variables that Terraform is expecting to have provided (either via a `terraform.tfvars` file, or via the `terraform` command line).

## Instructions

These instructions assume that you have an AWS account, that you know your AWS access key ID and secret access key, and that both Ansible and Terraform are installed and working on your system. The instructions also assume that the AWS CLI is working correctly.

1. Place the files from the `ansible/boostrap` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`) or just download the specific files from the `ansible/bootstrap` folder.

2. Edit `ec2.ini` and make sure the "regions=" line specifies the AWS regions where you will launch instances.

3. Create a `terraform.tfvars` file to contain the values assigned to the variables listed in `variables.tf`.

4. Run `terraform init`, followed by `terraform plan` and `terraform apply` to launch an AWS instance in the region you specified.

5. Run `ansible-playbook bootstrap.yml` to run the Ansible playbook against the AWS instance launched by Terraform.

    The AWS instance should now have the necessary Python dependencies installed to support all Ansible modules and functionality. Enjoy!

6. Run `terraform destroy` to tear down the AWS instance you launched in step 4.

## License

This content is licensed under the MIT License.
