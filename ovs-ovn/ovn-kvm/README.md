# Open Virtual Network (OVN) with KVM/Libvirt

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) to quickly and  easily spin up an environment to test Open Virtual Network (OVN), a part of the Open vSwitch project, and use it to provide connectivity to Libvirt/KVM-based virtual machines (VMs).

## Contents

* **ansible.cfg**: This is an Ansible configuration file that instructs Ansible to use the "vagrant" remote user, and to use Vagrant's built-in Ansible inventory.

* **final-setup.sh**: This shell script is used to integrate OVS with OVN. (See the "Instructions" below for how/when it is used.)

* **machines.yml**: This YAML file contains a list of VM definitions and associated configuration data. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs.

* **net-setup.sh**: This shell script configures Libvirt to integrate with OVS. (Refer to the "Instructions" section below for more information.)

* **ovn-setup.sh**: This Jinja2 template is used by Ansible to build a customized OVS/OVN configuration script for each Vagrant machine. The shell script created by this template is placed by Ansible in `/home/vagrant/ovn-setup.sh`.

* **ovs.xml**: This Libvirt network XML definition is used by `net-setup.sh` to facilitate Libvirt-OVS integration.

* **provision.yml**: This Ansible playbook installs the OVS/OVN components on each Vagrant machine. It's called automatically by Vagrant.

* **README.md**: This file you're currently reading.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

* **vm-setup.sh**: This shell script launches a simple VM for testing OVN connectivity.

## Instructions

These instructions assume you've already installed Vagrant, your back-end virtualization provider (such as VMware Fusion or VirtualBox), and any necessary plugins (such as the Vagrant VMware plugin). Please refer to the documentation for those products for more information on installation or configuration. _Note that this environment relies on nested virtualization support, and therefore will **not** work with VirtualBox._

1. Use `vagrant box add` to add an Ubuntu 16.04 box to your system. The "bento/ubuntu-16.04" box is very good for both VirtualBox and VMware Fusion/Workstation. (Note that the "ubuntu/xenial64" box for VirtualBox is currently broken under Vagrant.)

2. Edit the `machines.yml` file to ensure the box you downloaded in step 1 is specified on the "box:" sections of this file. Specify the name of a VMware-formatted box on the "vmw:" line; place the name of a VirtualBox-formatted box on the "vb:" line. (Note that the "vb:" line will be ignored, as this environment is not supported with VirtualBox.)

3. Run `vagrant up` to have Vagrant instantiate the three machines configured by default in this environment, and provision them using Ansible. (Note you'll need Ansible installed locally on the system where you're running `vagrant up`.)

4. Use `vagrant ssh hv-01` to log into the first system. Run `sudo ./setup.sh` to perform final configuration steps for OVS/OVN.

5. Repeat step #4 with `hv-02` and `hv-03`. OVN is now up and running, but not integrated with KVM/Libvirt.

6. Log into `hv-01` and run `sudo ./net-setup` to set up the Libvirt-OVS integration (it will create a Libvirt network backed by the OVS integration bridge).

7. Repeat step #6 with `hv-02` and `hv-03`.

8. On `hv-01`, run `sudo ./vm-setup.sh` to launch a VM and attach it to OVS using the Libvirt integration.

9. Repeat step #8 on `hv-02` and `hv-03`. You now have 3 VMs running across 3 nested KVM hypervisors.

10. On `hv-01`, create an OVN logical switch named "demo" using this command:

        sudo ovn-nbctl ls-add demo

11. Still on `hv-01`, run `sudo ./final-sh`. This will create an OVN logical port and link it back to OVS and Libvirt.

12. Repeat step #11 on `hv-02` and `hv-03`. You now have all three VMs linked together via an OVN logical switch.

13. Log into the console of each VM (using whatever method you prefer) and assign an IP address. Once all three VMs are assigned an IP address from the same subnet, you should have connectivity among the VMs. This connectivity is provided by OVN.

Enjoy!
