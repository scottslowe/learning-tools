# A Pulumi "Sandbox" Environment

This set of files was created to help users establish a "sandbox" environment for playing around with [Pulumi](https://pulumi.io).

**NOTE:** At some point in the future the Ansible playbooks here will be reconciled with the Pulumi role found in the `ansible/pulumi-env` directory of this repository.

## Contents

* **ansible.cfg**: This Ansible configuration file configures Ansible to work with local Vagrant-powered VMs. Modifications are needed to this file if you wish to use Ansible with a different inventory source (like EC2 instances, for example).

* **configure.yml**: This Ansible playbook is used to configure the baseline Pulumi environment. It is written for Ubuntu 18.04, and can be used with either local Vagrant VMs or remote cloud instances (such as EC2 instances). If you wish to use it with the latter, you will need to modify the Ansible configuration file to specify an appropriate inventory source.

* **js-simple-ec2.yml**: This Ansible playbook installs the files necessary to use Pulumi with JavaScript to instantiate an EC2 instance.

* **machines.yml**: This YAML file contains a list of VM definitions and associated configuration data. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs.

* **packer.json**: This Packer build file allows you to create an AWS AMI that is pre-configured with Pulumi.

* **README.md**: This file you're currently reading.

* **roles**: This directory contains all the various Ansible roles for the particular Pulumi example files. Each role directory corresponds to the name of an Ansible playbook, like `js-simple-ec2.yml`.

* **vagrant.py**: This dynamic inventory source for Ansible pulls inventory data from Vagrant. If you wish to use the Ansible playbooks with a different target, you'll need to find an equivalent dynamic inventory source (or create a static inventory) and modify the Ansible configuration file accordingly.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

## Instructions

These instructions assume you've already installed Ansible, your virtualization provider (VMware Fusion/Workstation or VirtualBox), Vagrant, and any necessary plugins (such as the Vagrant VMware plugin). Please refer to the documentation for those products for more information on installation or configuration. Similarly, if you wish to use these Ansible playbooks with a cloud provider such as AWS, these instructions assume all necessary tools and configuration have already been handled.

### Using Vagrant

1. Use `vagrant box add` to add an Ubuntu 18.04 base box for your particular virtualization provider. Some sample boxes are provided in `machines.yml`.

2. If you decided to use a box _other_ than one of the boxes listed here, you'll need to edit `machines.yml` to specify the correct box. In `machines.yml`, the "vmw" line is for a VMware-formatted box, the "vb" line is for the name of a VirtualBox-formatted box, and the "lv" line is for the name of a box that supports the Libvirt provider. Edit the appropriate line based on your virtualization provider and the name of the box you added in step 1.

3. Run `vagrant up` to instantiate the VM.

### Using AWS

1. Download a dynamic inventory script for EC2 (see [here](https://github.com/ansible/ansible/blob/devel/contrib/inventory/ec2.py) for one such script). Perform any necessary configuration to make sure the inventory script is returning the desired results.

2. Modify the `ansible.cfg` to specify the dynamic inventory script downloaded and configured in step 1.

3. Create an EC2 instance using the tooling of your choice (AWS CLI, AWS console, Terraform, CloudFormation, etc.).

Optionally, you can create a preconfigured Pulumi AMI using `packer`:

    packer build packer.json

You'll need to have `packer` installed and configured appropriately.

### For All Platforms

Once you've established a VM or instance using one of the previous two sections, then you can continue here.

1. Run `ansible-playbook configure.yml` to configure the Pulumi base components on the target VM or instance. If you are using a preconfigured AMI built using `packer`, this step isn't needed as it has already been done.

2. Run `ansible-playbook <scenario>.yml` to copy files needed for the particular Pulumi scenario you'd like to use. For example, `js-simple-ec2.yml` is one example scenario that shows using JavaScript with Pulumi to create an EC2 instance.

3. Log into the VM or instance using SSH (for Vagrant use `vagrant ssh`, for example) and switch to the `pulumi` directory in the home directory. The files for the scenario you selected in step 2 will be found there.

4. Play around with Pulumi. Enjoy!
