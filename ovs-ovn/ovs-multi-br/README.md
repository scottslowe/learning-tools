# Open vSwitch Multi-Bridge Configuration

These files were created to test a method of providing a single Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) environment that enables easier experimentation with Open vSwitch (OVS) configurations leveraging multiple OVS bridges. This environment was tested using Vagrant 1.8.7 with VMware Fusion 8.1.0 on OS X 10.11.6.

Note that VirtualBox is _not_ supported for this environment, as this environment uses a nested hypervisor (not supported by VirtualBox).

## Contents

* **ansible.cfg**: This Ansible configuration file instructs Ansible to use the Vagrant-generated inventory file (by default in `./.vagrant/provisioners/inventory/vagrant_ansible_inventory`), to use the default vagrant user (`vagrant`) and default insecure Vagrant SSH private key. _Please note that Ansible 2.1.1.0 (or later) is required._

* **br0.xml** and **br1.xml**: These XML files are used to define a couple OVS-based Libvirt networks. No edits are necessary to this file.

* **machines.yml**: This YAML file contains a list of VM definitions and associated configuration data. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs. Use the "vmw_box" line to specify a box formatted for the VMware provider. VirtualBox is not supported here because the environment uses nested virtualization.

* **provision.yml**: This YAML file is an Ansible playbook that configures the VMs created by Vagrant when they are first provisioned. Ansible is called automatically by Vagrant; you do not need to invoke Ansible separately. No changes are needed to this file.

* **README.md**: This file you're currently reading.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

## Instructions

These instructions assume you've already installed Vagrant, the necessary back-end virtualization provider(s) (only VMware providers are supported by this testing environment), and any necessary plugins. These instructions also assume that Ansible (version 2.1.1.0 or later) is installed and working properly. Please refer to the documentation for those products for more information on installation or configuration.

1. Use `vagrant box add` to install a 64-bit Ubuntu 14.04 Vagrant box. For a VMware-formatted box, one good option is the "bento/ubuntu-14.04" box. (In theory you should be able to use this Vagrant environment with VMware Workstation as well, but only VMware Fusion was tested.)

2. Copy the files from the `ovs-multi-br` directory of this repository (the "learning-tools" repository) to a directory on your system. You can clone the entire "learning-tools" repository (using `git clone`), or just download the specific files from the `ovs-multi-br` directory.

3. Edit `machines.yml` to ensure that the box specified in that file matches the Ubuntu 14.04 x64 base box you just installed and will be using. If you are using VMware Fusion (or VMware Workstation), specify the value on the "vmw_box" line. _I recommend that you do not change any other values in this file unless you know it is necessary._

4. From a terminal window, change into the directory where the files from this directory are stored and run `vagrant up` to bring up the VMs according to the instructions in `machines.yml` and `Vagrantfile`. (By default, it will create and power on a single VMs that will act as a KVM hypervisor.)

5. Run `vagrant ssh` to connect to the VM.

6. In the VM, run `sudo virsh net-create br0.xml` to create a Libvirt-based network that will connect VMs to the OVS bridge named "br0". After that completes, run `sudo virsh net-create br1.xml` to do the same for the OVS "br1" bridge.

7. Make three copies of the `cirros-0.3.2-x86_64-disk.img` file (this file was already downloaded by Ansible), and make note of the names. For these instructions, I'll assume the file names are `cirros1.img`, `cirros2.img`, and `cirros3.img`.

8. Now launch three KVM VMs using this command line:

        sudo virt-install --name=cirros1 --ram=128 --vcpus=1 \
        --disk path=./cirros1.img,format=qcow2 \
        --import --network network:br0 --vnc

    Repeat for the second VM:

        sudo virt-install --name=cirros2 --ram=128 --vcpus=1 \
        --disk path=./cirros2.img,format=qcow2 \
        --import --network network:br1 --vnc

    And again for the third VM:

        sudo virt-install --name=cirros3 --ram=128 --vcpus=1 \
        --disk path=./cirros3.img,format=qcow2 \
        --import --network network:br1 --vnc

9. Determine the VNC display allocated to the new VM with this command (it should be `127.0.0.1:0`):

        sudo virsh vncdisplay cirros1

10. While still logged into the Vagrant guest, enable SSH forwarding to the VM's VNC display by first pressing `~C`, then entering this command (this assumes the VNC display was 0; adjust the command if the output of the previous command was different):

        -L 5910:127.0.0.1:5900

11. On your host system, point your VNC viewer to `127.0.0.1:5910` (or tell it to use display 10 on `127.0.0.1`). This will connect you to the console of the first CirrOS VM.

12. Log into the CirrOS VM using the credentials provided (username `cirros`, password `cubswin:)`, as noted on the screen).

13. Verify that you have received an IP address (typically assigned via DHCP) and that you have connectivity to hosts on the same subnet.

14. Repeat steps 9 through 13 for `cirros2` and `cirros3`, respectively. You should have network connectivity to the respective local subnets from each VM, but no connectivity between subnets.

Enjoy!
