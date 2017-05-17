# Docker CE Edge on Ubuntu 16.04

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) quickly and relatively easily turn up a VM running the "Edge" channel of Docker CE (for testing the newest features of Docker). This was tested using Vagrant 1.9.1 with VirtualBox 5.1.20 on Fedora Linux, but it should work with any recent version of Vagrant with other virtualization providers as well. The `Vagrantfile` is already extended to support the Vagrant VMware provider.

## Contents

* **ansible.cfg**: This is an Ansible configuration file to support Vagrant. No edits to this file should be needed.

* **machines.yml**: This YAML file contains a list of VM definitions. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs. You will need to edit this file to provide the correct Vagrant box names. Other edits should not be necessary.

* **provision.yml**: This is an Ansible playbook to automatically install Docker CE from the "Edge" channel. No edits to this file should be needed.

* **README.md**: This file you're currently reading.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

## Instructions

These instructions assume you've already installed your virtualization provider (VMware Fusion/Workstation or VirtualBox), Vagrant, and any necessary plugins (such as the Vagrant VMware plugin). Please refer to the documentation for those products for more information on installation or configuration.

1. Use `vagrant box add` to install an Ubuntu 16.04 Vagrant box. The "bento/ubuntu-16.04" box works well with both VirtualBox and VMware Fusion/Workstation.

2. Place the files from the `docker/docker-ce-edge` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`) or just download the specific files from the the `docker/docker-ce-edge` folder.

3. If you're _not_ using the "bento/ubuntu-16.04" Vagrant box, edit `machines.yml` to supply the correct box name. This file has separate lines for the VirtualBox and VMware providers; be sure to edit the correct line for your provider.

4. Run `vagrant up` to instantiate the VM. Vagrant will create the VM, power it on, and automatically run an Ansible playbook against the VM.

5. Once Vagrant is finished, use `vagrant ssh` to log into the VM.

Enjoy!
