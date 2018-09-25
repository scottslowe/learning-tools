# Virtualization with KVM

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) and Ansible ([http://www.ansible.com](http://www.ansible.com)) to quickly and relatively easily experiment with KVM on Ubuntu 16.04 LTS. This configuration was tested using Vagrant, VMware Fusion with the Vagrant VMware plugin, KVM/Libvirt with the Libvirt provider, and Ansible.

Note that this environment does **not** work with VirtualBox, as it relies on support for nested virtualization (which VirtualBox does not support).

## Contents

* **ansible.cfg**: This Ansible configuration file instructs Ansible to use the Vagrant-provided inventory, to use the default vagrant user (`vagrant`) and default insecure Vagrant SSH private key, and to use the Vagrant-generated Ansible inventory file (found, by default, in `./.vagrant/provisioners/inventory/vagrant_ansible_inventory`).

* **machines.yml**: This YAML file contains a list of VM definitions and associated configuration data. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs.

* **provision.yml**: This YAML file is an Ansible playbook that configures the VMs created by Vagrant when they are first provisioned. Ansible is called automatically by Vagrant; you do not need to invoke Ansible separately. No changes are needed to this file.

* **README.md**: The file you're currently reading.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored in `machines.yml`.

## Instructions

These instructions assume you've already installed Vagrant, the virtualizatoin provider (VMware Fusion or Libvirt), the necessary Vagrant plugin, and Ansible. Please refer to the documentation for those products for more information on installation or configuration.

1. Use `vagrant box add` to install an Ubuntu 16.04 x64 box for VMware Fusion or Libvirt. The `machines.yml` file contains some suggested boxes. If you download a box other than one of the ones listed in `machines.yml`, be sure to edit `machines.yml` appropriately.

2. Copy the files from the `kvm-generic` directory of this repository (the "learning-tools" repository) to a directory on your system. You can clone the entire "learning-tools" repository (using `git clone`), or just download the specific files from the `kvm-generic` directory.

3. From a terminal window, change into the directory where the files from this directory are stored and run `vagrant up` to bring up the VMs according to the instructions in `machines.yml` and `Vagrantfile`. (By default, it will create and power on only a single VM.)

5. Once Vagrant has finished creating, booting, and provisioning the VM (note you'll need Internet access for this step), log into the VM (named "kvm-01" by default) using `vagrant ssh`.

You can now use this VM to do any testing or experimentation with KVM and Libvirt.
