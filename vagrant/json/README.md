# Generic Debian 8.0 ("Jessie") VM

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) to quickly and  easily spin up a generic Debian 8.0 ("Jessie") 64-bit VM. The configuration was tested using Vagrant 1.7.2, VMware Fusion 6.0.5, and the Vagrant VMware plugin.

**NOTE:** There's really nothing special here; I created these files because I often had a need to quickly and easily spin up a generic Debian VM for some purpose (building a package or testing a command). I'm including them here just for the sake of completeness.

## Contents

* **README.md**: This file you're currently reading.

* **servers.yml**: This YAML file contains a list of VM definitions and associated configuration data. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

## Instructions

These instructions assume you've already installed VMware Fusion, Vagrant, and the Vagrant VMware plugin. Please refer to the documentation for those products for more information on installation or configuration.

1. Use `vagrant box add` to add a 64-bit Debian 8.0 ("Jessie") base box to be used by this `Vagrantfile`. You'll need to specify a box that provides support for the `vmware_desktop` provider (it should work with either VMware Workstation or VMware Fusion).

2. Edit the `servers.yml` file to ensure the box you downloaded in step 1 is specified on the "box:" line of this file.

3. Run `vagrant up`, and when the VM is up use `vagrant ssh` to access the generic Debian VM.

Enjoy!
