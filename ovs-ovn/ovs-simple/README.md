# Simple Open vSwitch (OVS) Environment

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) to quickly and  easily spin up an environment to test Open vSwitch (OVS).

## Contents

* **ansible.cfg**: This is an Ansible configuration file that instructs Ansible to use the "vagrant" remote user, and to use Vagrant's built-in Ansible inventory.

* **machines.yml**: This YAML file contains a list of VM definitions and associated configuration data. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs.

* **provision.yml**: This Ansible playbook installs the OVS/OVN components on each Vagrant machine. It's called automatically by Vagrant.

* **README.md**: This file you're currently reading.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

## Instructions

These instructions assume you've already installed Vagrant, your back-end virtualization provider (such as VMware Fusion or VirtualBox), and any necessary plugins (such as the Vagrant VMware plugin). Please refer to the documentation for those products for more information on installation or configuration.

1. Use `vagrant box add` to add an Ubuntu 16.04 box to your system. The "bento/ubuntu-16.04" box is very good for both VirtualBox and VMware Fusion/Workstation. (Note that the "ubuntu/xenial64" box for VirtualBox is currently broken under Vagrant.)

2. Edit the `machines.yml` file to ensure the box you downloaded in step 1 is specified on the "box:" sections of this file. Specify the name of a VMware-formatted box on the "vmw:" line; place the name of a VirtualBox-formatted box on the "vb:" line.

3. Run `vagrant up` to have Vagrant instantiate the three machines configured by default in this environment, and provision them using Ansible. (Note you'll need Ansible installed locally on the system where you're running `vagrant up`.)

4. Use `vagrant ssh ovs-01` to log into the first system. Verify that OVS is installed and working with `ovs-vsctl show`.

5. Repeat step #4 with `ovs-02`.

You now have a two-node system setup where both nodes are running OVS with a single bridge.

Enjoy!
