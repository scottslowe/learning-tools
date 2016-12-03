# KVM Networking with macvtap Interfaces

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) and Ansible ([http://www.ansible.com](http://www.ansible.com)) to quickly and relatively easily experiment with KVM networking using macvtap interfaces on Ubuntu 14.04 LTS. This configuration was tested using Vagrant 1.8.1, VMware Fusion 8.1.0, the Vagrant VMware plugin, and Ansible 1.9.1. Other versions of these products are likely to work, but haven't been tested.

Note that this environment does **not** work with VirtualBox, as it relies on support for nested virtualization (which VirtualBox does not support).

## Contents

* **ansible.cfg**: This Ansible configuration file instructs Ansible to use the Vagrant-generated inventory file (by default in `./.vagrant/provisioners/inventory/vagrant_ansible_inventory`), to use the default vagrant user (`vagrant`) and default insecure Vagrant SSH private key.

* **machines.yml**: This YAML file contains a list of VM definitions and associated configuration data. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs. Generally the only change this file needs is to ensure the "box" value correctly references the Vagrant box you're using. The "ip_addr" value is also required in order for Vagrant to provision the second network interface; it's not recommended to remove this value, although you may need to adjust the IP addresses to suit your particular environment.

* **macvtap.xml**: This XML file is used to define a Libvirt network using macvtap interfaces. No edits are necessary to this file.

* **provision.yml**: This YAML file is an Ansible playbook that configures the VMs created by Vagrant when they are first provisioned. Ansible is called automatically by Vagrant; you do not need to invoke Ansible separately. No changes are needed to this file.

* **README.md**: The file you're currently reading.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored in `machines.yml`.

## Instructions

These instructions assume you've already installed VMware Fusion, Vagrant, the Vagrant VMware plugin, and Ansible. Please refer to the documentation for those products for more information on installation or configuration.

Note that you'll also need a VNC client to connect the console of the nested KVM VM; these instructions assume you've already installed, configured, and tested a VNC client.

1. Use `vagrant box add` to install an Ubuntu 14.04 x64 box for the "vmware_fusion" provider. The "bento/ubuntu-14.04" box is a good option here. (In theory you should be able to use this Vagrant environment with VMware Workstation as well, but only VMware Fusion was tested.)

2. Copy the files from the `kvm-macvtap` directory of this repository (the "learning-tools" repository) to a directory on your system. You can clone the entire "learning-tools" repository (using `git clone`), or just download the specific files from the `kvm-macvtap` directory.

3. Edit `machines.yml` to ensure that the box specified in that file matches the Ubuntu 14.04 x64 base box you just installed and will be using. _I recommend that you do not change any other values in this file unless you know it is necessary._

4. From a terminal window, change into the directory where the files from this directory are stored and run `vagrant up` to bring up the VMs according to the instructions in `machines.yml` and `Vagrantfile`. (By default, it will create and power on two VMs---one will act as a KVM hypervisor, the other as a remote system.)

5. Once Vagrant has finished creating, booting, and provisioning the VMs (note you'll need Internet access for this step), log into the VM named "kvm-01"  using `vagrant ssh kvm-01`.

6. Run the following command to create the Libvirt network that will leverage macvtap interfaces for VMs (the `macvtap.xml` file was copied into the VM already by Ansible):

        sudo virsh net-define macvtap.xml

    Then start the Libvirt network with this command:

        sudo virsh net-start macvtap-net

7. **This step is very important!** Set the interface that will be used by this Libvirt network to promiscuous mode:

        sudo ip link set eth1 promisc on

    (Note: this may cause a prompt for authentication to allow the virtual NIC to enter promiscuous mode, depending on your host OS and host OS configuration.)

8. Now launch a KVM VM using this command line (the CirrOS disk image was downloaded already by Ansible):

        sudo virt-install --name=cirros --ram=256 --vcpus=1 \
        --disk path=./cirros-0.3.2-x86_64-disk.img,format=qcow2 \
        --import --network network:macvtap-net --vnc

9. Determine the VNC display allocated to the new VM with this command (it should be `127.0.0.1:0`):

        sudo virsh vncdisplay cirros

10. While still logged into the VM named `kvm-01`, enable SSH forwarding to the VM's VNC display by first pressing `~C`, then entering this command (this assumes the VNC display was 0; adjust the command if the output of the previous command was different):

        -L 5910:127.0.0.1:5900

11. On your host system, point your VNC viewer to `127.0.0.1:5910` (or tell it to use display 10 on `127.0.0.1`). This will connect you to the console of the CirrOS VM.

12. Log into the CirrOS VM using the credentials provided (username `cirros`, password `cubswin:)`, as noted on the screen).

13. Type in `ip addr list` to determine the IP address that was obtained by the CirrOS VM. If the VM did _not_ obtain an IP address, then something went wrong. Destroy the entire Vagrant environment (using `vagrant destroy -f`) and start again.

14. In a separate terminal window, change to the directory where the files for this environment are stored and connect to the second VM using `vagrant ssh remote-01`. From this VM, you should be able to ping the IP address of the CirrOS VM obtained in step 13.

There you have it---a working KVM environment, with a nested VM, using a macvtap interface for networking. A few things to note:

* From the `kvm-01` VM, you'll be able to see the macvtap interface (use `ip link list`) but you will _not_ be able to see the IP address of the interface.
* There will be no connectivity between the KVM VM and the nested CirrOS VM. This is a byproduct of using macvtap interfaces instead of a bridge.
* If you forget step #7, then the CirrOS VM won't get an IP address and connectivity to/from the VM will not work. Fortunately, you can run step #7 later and manually assign an IP address to the CirrOS VM if that happens.
* The `Vagrantfile` does not contain any support for VirtualBox because this environment requires nested virtualization support. VirtualBox does not offer support for nested virtualization.

Enjoy!
