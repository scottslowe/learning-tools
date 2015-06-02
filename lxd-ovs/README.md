# Running LXC-Based OS Containers with LXD and Open vSwitch

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) quickly and relatively easily experiment with LXD (pronounced "lex-dee"), a new daemon and CLI client for working with LXC-based OS containers, along with Open vSwitch, aka OVS ([http://openvswitch.org](http://openvswitch.org)). The configuration was tested using Vagrant 1.7.2, VMware Fusion 6.0.5, and the Vagrant VMware plugin.

## Contents

* **README.md**: This file you're currently reading.

* **servers.yml**: This YAML file contains a list of VM definitions and associated configuration data. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs. No changes should be necessary to use this file.

* **setup.sh**: This shell script is called by the Vagrant shell provisioner and configures LXD inside the VM created by Vagrant. No changes should be needed to this file.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

## Instructions

This learning environment is exactly the same as the LXD learning environment (found in the `lxd` directory of my "learning-tools" GitHub repository), but adds OVS to the base box. This will enable you to leverage LXD/LXC's built-in support for OVS by specifying the name of an OVS bridge on the "parent:" line of a bridged network interface in the container's profile.

I have an Ubuntu 14.04 x64 with OVS base box that can be used; just be sure that "slowe/ubuntu-1404-x64-ovs" is specified in the `servers.yml` file as the name of the box Vagrant should use.

Enjoy working with LXD and OVS!
