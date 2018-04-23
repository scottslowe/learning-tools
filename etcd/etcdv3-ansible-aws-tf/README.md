# Running an etcd v3 Cluster on Ubuntu 16.04 on AWS

These files were created to allow users to use Terraform and Ansible to set up an etcd v3 cluster on AWS.

## Contents

* **README.md**: This file you're currently reading.

## Instructions

These instructions assume you've already installed your back-end virtualization provider (VMware Fusion or VirtualBox), Vagrant, and any necessary plugins (such as the Vagrant VMware plugin). Please refer to the documentation for those products for more information on installation or configuration.

1. Place the files from the `etcdv3-ansible-aws-tf` directory of this GitHub repository (the "lowescott/learning-tools" repository) into a directory on your system. You can clone the entire "learning-tools" repository (using `git clone`), or just download the specific files from the `etcdv3-ansible-aws-tf` directory.

6. You can test etcd with this command:

		etcdctl member list
	
	This should return a list of three nodes as members of the etcd cluster. If you receive an error or don't see all three VMs listed, tear down the Vagrant environment with `vagrant destroy` and recreate the environment from scratch. If you continue to experience problems, open an issue here on GitHub (or file a pull request fixing the problem).

Enjoy!
