# Multi-Platform Vagrant Environment

These files were created to test a method of providing a single Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) environment that could support multiple local virtualization platforms without any changes to the `Vagrantfile` or supporting configuration files. This configuration was tested with the following components:

* Vagrant 1.7.4 with VirtualBox 4.3.36 on Debian "Jessie" using the official "debian/jessie64" Vagrant box
* Vagrant 1.8.1 with VMware Fusion 8.1.0 on OS X 10.11.3 using my self-built "slowe/debian-81-x64" Vagrant box

## Contents

* **machines.yml**: This YAML file contains a list of VM definitions and associated configuration data. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs. Separate box definitions are included: "vb_box" for VirtualBox, "vmw_box" for VMware Fusion.

* **README.md**: This file you're currently reading.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

## Instructions

These instructions assume you've already installed Vagrant, the necessary back-end virtualization provider(s) (only VirtualBox and VMware Fusion are supported by this testing environment), and any necessary plugins. Please refer to the documentation for those products for more information on installation or configuration.

1. Use `vagrant box add` to add a VirtualBox box and/or a VMware Fusion box (any box marked with "vmware_desktop" provider will work with Fusion). If you have both VMware Fusion and VirtualBox installed on the same system, install a box for both platforms.

2. Edit the `machines.yml` file to ensure the box(es) you downloaded in step 1 is/are specified in this file. Place the name of the VirtualBox box as the value for "vb_box"; supply the name of the VMware Fusion box as the value for "vmw_box".

3. Run `vagrant up`. Vagrant will create the VM using the box specified in `machines.yml`, selecting the appropriate box based on the provider in use.

5. For ideal results, test this environment on different systems with different providers (i.e., a Linux system running VirtualBox and an OS X system running VMware Fusion). No changes should be required to either `Vagrantfile` or `machines.yml` as you switch back and forth between systems.

Enjoy!
