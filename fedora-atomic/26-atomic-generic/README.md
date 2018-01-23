# Generic Fedora 26 Atomic Host VM

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) to quickly and easily spin up an environment running Fedora 26 Atomic Host. This configuration was tested with VirtualBox and Libvirt (no VMware-compatible Fedora Atomic Host box was available).

**NOTE:** This is just a generic VM environment. I'm including it here for the sake of completeness and because it can/will serve as a basis for other environments.

## Contents

* **machines.yml**: This YAML file contains a list of VM definitions and associated configuration data. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs.

* **README.md**: This file you're currently reading.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

## Instructions

These instructions assume you've already installed your virtualization provider (VMware Fusion/Workstation or VirtualBox), Vagrant, and any necessary plugins (such as the Vagrant VMware plugin). Please refer to the documentation for those products for more information on installation or configuration.

1. Use `vagrant box add` to add a Fedora Atomic Host base box to be used by this `Vagrantfile`. (This environment was tested using Fedora 26 Atomic Host.) You'll need to specify a box that provides support for the virtualization provider you're planning to use. The existing `machines.yml` file provides some suggested boxes.

2. If you decide to use a box _other_ than one listed in `machines.yml`, edit `machines.yml` to specify the name of the box you added in step 1. In `machines.yml`, the "vmw" line is for a VMware-formatted box, the "vb" line is for the name of a VirtualBox-formatted box, and the "lv" line is for the name of a box that supports the Libvirt provider. Edit the appropriate line based on your virtualization provider and the name of the box you added in step 1.

3. Run `vagrant up`, and when the VM is up use `vagrant ssh` to access the VM.

Enjoy!
