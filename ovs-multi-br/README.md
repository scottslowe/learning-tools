# Open vSwitch Multi-Bridge Configuration

These files were created to test a method of providing a single Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) environment that enables easier experimentation with Open vSwitch (OVS) configurations leveraging multiple OVS bridges. This environment was tested using Vagrant 1.8.1 with VMware Fusion 8.1.0 on OS X 10.11.3 using my self-built "slowe/ubuntu-trusty-x64" Vagrant box.

## Contents

* **ansible.cfg**: This Ansible configuration file instructs Ansible to use the Vagrant-generated inventory file (by default in `./.vagrant/provisioners/inventory/vagrant_ansible_inventory`), to use the default vagrant user (`vagrant`) and default insecure Vagrant SSH private key.

* **br0.xml** and **br1.xml**: These XML files are used to define a couple OVS-based Libvirt networks. No edits are necessary to this file.

* **machines.yml**: This YAML file contains a list of VM definitions and associated configuration data. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs. Separate box definitions are included: "vb_box" for VirtualBox, "vmw_box" for VMware Fusion.

* **provision.yml**: This YAML file is an Ansible playbook that configures the VMs created by Vagrant when they are first provisioned. Ansible is called automatically by Vagrant; you do not need to invoke Ansible separately. No changes are needed to this file.

* **README.md**: This file you're currently reading.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

## Instructions

These instructions assume you've already installed Vagrant, the necessary back-end virtualization provider(s) (only VirtualBox and VMware Fusion are supported by this testing environment), and any necessary plugins. Please refer to the documentation for those products for more information on installation or configuration.

**INSTRUCTIONS NOT YET COMPLETE**
