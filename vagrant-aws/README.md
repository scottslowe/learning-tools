# Using Vagrant with AWS (Single Instance)

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) with AWS, where the VMs managed by Vagrant are actually instances running on AWS. Using these files, Vagrant can only operate against a single instance at a time. This configuration was tested using Vagrant 1.8.1, version 0.7.0 of [the vagrant-aws plugin](https://github.com/mitchellh/vagrant-aws), and AWS.

## Contents

* **instances.yml**: This YAML file contains the instance-specific configuration information. Six values are expected in this file: `instance_type` (the instance type, such as "m3.medium"), `region`, `ami`, `user`, `security_groups`, and `keypair_name`.

* **README.md**: The file you're currently reading.

* **Vagrantfile**: This file is used by Vagrant to spin up the AWS instance. There are two changes that **must** be made to this file in order for it to function properly: you must specify the correct path to your SSH private key (the private key for the keypair specified in `instances.yml`), and you must supply the correct name to the dummy box installed for use with AWS.

## Instructions

These instructions assume that you have an AWS account, that you know your AWS access key ID and secret access key, and that you have a valid SSH keypair configured in your AWS account.

1. Vagrant requires that a "dummy box" be installed for use with AWS. Run this command to install the dummy box:

        vagrant box add <box-name> https://github.com/mitchellh/vagrant-aws/raw/master/dummy.box

2. Install the Vagrant AWS provider by running `vagrant plugin install vagrant-aws`.

3. Place the files from the `vagrant-aws` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`), download a ZIP file of the entire "learning-tools" repository, or just download the specific files from the the `vagrant-aws` folder.

4. Edit `instances.yml` to supply the correct information to be used by Vagrant when launching an instance on AWS. You'll need to specify the instance type, region, AMI ID, default SSH user for the AMI, a list of security groups (by name, _not_ by security group ID), and the keypair name.

5. In a terminal window, set the `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` environment variables to your AWS access key ID and AWS secret access key, respectively.

7. In the directory where you placed the files from this GitHub repository, run `vagrant up` to have Vagrant authenticate to AWS and launch the desired instance for you. Once the instance is created, you can use `vagrant ssh` to connect to the instance, and `vagrant destroy` will terminate (destroy) the AWS instance for you. (You can follow all these actions in the AWS Management Console, if you so desire.)

Enjoy!

## Additional Notes

This environment will only create a single instance on AWS using Vagrant. An environment for spinning up multiple instances is planned for the near future.
