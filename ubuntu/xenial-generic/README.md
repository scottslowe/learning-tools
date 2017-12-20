# Generic Ubuntu 16.04 VM

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) to quickly and  easily spin up a generic Ubuntu 16.04 ("Xenial Xerus") VM. The configuration was tested with VMware Fusion, VirtualBox, and Libvirt.

**NOTE:** There's really nothing special here; I created these files because I often had a need to quickly and easily spin up a generic Ubuntu VM for some purpose (building a package or testing a command). I'm including them here just for the sake of completeness.

## Contents

* **machines.yml**: This YAML file contains a list of VM definitions and associated configuration data. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs.

* **README.md**: This file you're currently reading.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

## Instructions

These instructions assume you've already installed your virtualization provider (VMware Fusion/Workstation or VirtualBox), Vagrant, and any necessary plugins (such as the Vagrant VMware plugin). Please refer to the documentation for those products for more information on installation or configuration.

1. Use `vagrant box add` to add an Ubuntu 16.04 base box to be used by this `Vagrantfile`. The `machines.yml` file contains some suggested/recommended boxes.

2. If you are not using a box from `machines.yml`, edit `machines.yml` file to specify the Vagrant box you added in step 1 (the "vmw", "vb", and "lv" lines are for boxes formatted for VMware Fusion, VirtualBox, and Libvirt, respectively). You can also edit this file to change the amount of RAM allocated or the number of virtual CPUs assigned.

3. Run `vagrant up`, and when the VM is up use `vagrant ssh` to access the VM.

Enjoy!
