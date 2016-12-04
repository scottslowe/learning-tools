# Running LXC-Based OS Containers with LXD and Open vSwitch

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) quickly and relatively easily experiment with LXD (pronounced "lex-dee"), a new daemon and CLI client for working with LXC-based OS containers, along with Open vSwitch, aka OVS ([http://openvswitch.org](http://openvswitch.org)). The configuration was tested using Vagrant 1.8.7, VMware Fusion 8.1.0, and the Vagrant VMware plugin.

## Contents

* **machines.yml**: This YAML file contains a list of VM definitions and associated configuration data. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs. No changes should be necessary to use this file.

* **README.md**: This file you're currently reading.

* **setup.sh**: This shell script is called by the Vagrant shell provisioner and configures LXD inside the VM created by Vagrant. No changes should be needed to this file.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

## Instructions

This learning environment is exactly the same as the LXD learning environment (found in the `lxd` directory of my "learning-tools" GitHub repository), but adds OVS to the base box. This will enable you to leverage LXD/LXC's built-in support for OVS by specifying the name of an OVS bridge on the "parent:" line of a bridged network interface in the container's profile.

Use `vagrant box add` to add an Ubuntu 14.04 base box. For a VMware-formatted box, the "bento/ubuntu-14.04" box is one good option. For a VirtualBox-formatted box, use the "ubuntu/trusty64" box. Just be sure the appropriate box name(s) are put into the `machines.yml` file before running `vagrant up`.

Enjoy working with LXD and OVS!
