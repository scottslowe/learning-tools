# Virtualization with KVM

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) and Ansible ([http://www.ansible.com](http://www.ansible.com)) to quickly and relatively easily experiment with KVM on Ubuntu 14.04 LTS. This configuration was tested using Vagrant 1.8.1, VMware Fusion 8.1.0, the Vagrant VMware plugin, and Ansible 1.9.1. Other versions of these products are likely to work, but haven't been tested.

Note that this environment does **not** work with VirtualBox, as it relies on support for nested virtualization (which VirtualBox does not support).

## Contents

* **ansible.cfg**: This Ansible configuration file instructs Ansible to use an inventory file named `hosts` in the current directory, to use the default vagrant user (`vagrant`) and default insecure Vagrant SSH private key, and to use the Vagrant-generated Ansible inventory file (found, by default, in `./.vagrant/provisioners/inventory/vagrant_ansible_inventory`).

* **machines.yml**: This YAML file contains a list of VM definitions and associated configuration data. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs. Generally the only change this file needs is to ensure the "box" value correctly references the Vagrant box you're using. The "ip_addr" value is also required in order to build the Ansible inventory file; you may want to edit this to a value appropriate for your environment.

* **provision.yml**: This YAML file is an Ansible playbook that configures the VMs created by Vagrant when they are first provisioned. Ansible is called automatically by Vagrant; you do not need to invoke Ansible separately. No changes are needed to this file.

* **README.md**: The file you're currently reading.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored in `machines.yml`.

## Instructions

These instructions assume you've already installed VMware Fusion, Vagrant, the Vagrant VMware plugin, and Ansible. Please refer to the documentation for those products for more information on installation or configuration.

1. Use `vagrant box add` to install an Ubuntu 14.04 x64 box for the "vmware_fusion" provider. The "bento/ubuntu-14.04" box is a good option here. (In theory you should be able to use this Vagrant environment with VMware Workstation as well, but only VMware Fusion was tested.)

2. Copy the files from the `kvm` directory of this repository (the "learning-tools" repository) to a directory on your system. You can clone the entire "learning-tools" repository (using `git clone`), or just download the specific files from the `kvm` directory.

3. Edit `machines.yml` to ensure that the box specified in that file matches the Ubuntu 14.04 x64 base box you just installed and will be using. _I recommend that you do not change any other values in this file unless you know it is necessary._

4. From a terminal window, change into the directory where the files from this directory are stored and run `vagrant up` to bring up the VMs according to the instructions in `machines.yml` and `Vagrantfile`. (By default, it will create and power on only a single VM.)

5. Once Vagrant has finished creating, booting, and provisioning the VM (note you'll need Internet access for this step), log into the VM (named "kvm-01" by default) using `vagrant ssh`.

You can now use this VM to do any testing or experimentation with KVM and Libvirt.
