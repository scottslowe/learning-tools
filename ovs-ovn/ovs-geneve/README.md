# Using Geneve Tunnels with OVS

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) and Ansible ([http://www.ansible.com](http://www.ansible.com)) to quickly and relatively easily experiment with KVM on Ubuntu 14.04 LTS. This configuration was tested using Vagrant 1.8.1, VMware Fusion 8.1.0, the Vagrant VMware plugin, and Ansible 1.9.1. Other versions of these products are likely to work, but haven't been tested.

## Contents

* **ansible.cfg**: This Ansible configuration file instructs Ansible to use an inventory file named `hosts` in the current directory, to use the default vagrant user (`vagrant`) and default insecure Vagrant SSH private key, and to use the Vagrant-generated Ansible inventory file (found, by default, in `./.vagrant/provisioners/inventory/vagrant_ansible_inventory`).

* **geneve-net.xml**: This is a snippet of Libvirt network XML that is used by Ansible when provisioning the VMs. No changes are needed to this file.

* **machines.yml**: This YAML file contains a list of VM definitions and associated configuration data. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs. Generally the only change this file needs is to ensure the "box" value correctly references the Vagrant box you're using. The "ip_addr" value is also required in order to build the Ansible inventory file; you may want to edit this to a value appropriate for your environment.

* **provision.yml**: This YAML file is an Ansible playbook that configures the VMs created by Vagrant when they are first provisioned. Ansible is called automatically by Vagrant; you do not need to invoke Ansible separately. No changes are needed to this file.

* **setup.sh**: This Bash shell script prepares the VMs to test connectivity between a network namespace on each host over the Geneve tunnel. No changes should be necessary to this file; Ansible copies the file into the VMs during provisioning.

* **README.md**: The file you're currently reading.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored in `machines.yml`.

## Instructions

These instructions assume you've already installed VMware Fusion, Vagrant, the Vagrant VMware plugin, and Ansible. Please refer to the documentation for those products for more information on installation or configuration.

1. Use `vagrant box add` to install an Ubuntu 14.04 x64 box for the "vmware_fusion" provider. I have a base box you can use for this purpose; to use my Ubuntu 14.04 x64 base box, add the box with `vagrant box add slowe/ubuntu-trusty-x64`. (In theory you should be able to use this Vagrant environment with VMware Workstation as well, but only VMware Fusion was tested.)

2. Copy the files from the `ovs-geneve` directory of this repository (the "learning-tools" repository) to a directory on your system. You can clone the entire "learning-tools" repository (using `git clone`), or just download the specific files from the `ovs-geneve` directory.

3. Edit `machines.yml` to ensure that the box specified in that file matches the Ubuntu 14.04 x64 base box you just installed and will be using. _I recommend that you do not change any other values in this file unless you know it is necessary._

4. From a terminal window, change into the directory where the files from this directory are stored and run `vagrant up` to bring up the VMs according to the instructions in `machines.yml` and `Vagrantfile`. (By default, it will create and power on two VMs.) Internet access is required for this step, as Ansible will download updates to the VMs as part of the provisioning process.

5. Once Vagrant has finished creating, booting, and provisioning the VM (note you'll need Internet access for this step), run `vagrant reload` to reboot the VMs, thus ensuring that the latest kernel (required for support of Geneve tunnels) is loaded.

6. Log into the first VM (named "kvm-01" by default) using `vagrant ssh kvm-01`. In a separate terminal window, change to the directory where you placed the files for this environment and log into the second VM using `vagrant ssh kvm-02`. From here on, repeat all remaining steps on both VMs.

7. Switch into the `ovs` directory. Compile OVS using the commands found in [the OVS INSTALL.md documentation](https://github.com/openvswitch/ovs/blob/master/INSTALL.md). For convenience, here are the steps required (without additional explanation; note that Ansible has already installed the necessary packages and prerequisites when Vagrant provisioned the VMs):

        $ ./boot.sh
        $ ./configure --with-linux=/lib/modules/`uname -r`/build
        $ make
        $ sudo make install
        $ sudo make modules_install
        $ sudo modprobe openvswitch
        $ sudo modprobe vport_geneve
        $ sudo ovsdb-tool create /usr/local/etc/openvswitch/conf.db vswitchd/vswitch.ovsschema
        $ sudo ovsdb-server --remote=punix:/usr/local/var/run/openvswitch/db.sock --remote=db:Open_vSwitch,Open_vSwitch,manager_options --private-key=db:Open_vSwitch,SSL,private_key --certificate=db:Open_vSwitch,SSL,certificate --bootstrap-ca-cert=db:Open_vSwitch,SSL,ca_cert --pidfile --detach
        $ sudo ovs-vsctl --no-wait init
        $ sudo ovs-vswitchd --pidfile --detach

    When this process is complete, you should be able to run `sudo ovs-vsctl show` without receiving any errors (no configuration will be shown because there is no configuration to show).

8. Run `setup.sh` on each VM. This will create a network namespace, create a pair of veth interfaces, and configure the veth interfaces for testing.

9. Configure OVS for Geneve tunneling using the following commands (note that you may need to preface each command with `sudo`):

        $ ovs-vsctl add-br br-int
        $ ovs-vsctl add-port br-int geneve0 -- set interface geneve0 type=geneve options:remote_ip=<remote IP address>
        $ ovs-vsctl add-port veth0

    For `<remote IP address>` in the command above, substitute the IP address for the "eth1" interface of the _other_ VM. So, on `kvm-01`, you'd supply the IP address of "eth1" on `kvm-02`, and vice versa.

10. From `kvm-01`, type `sudo ip netns exec testns ping -c 4 10.1.1.101` to verify connectivity between the test namespace on `kvm-01` with the test namespace on `kvm-02`.

11. From `kvm-02`, type `sudo ip netns exec testns ping -c 4 10.1.1.100` to verify connectivity between the test namespace on `kvm-02` with the test namespace on `kvm-01`.

Congratulations, you've just configured and tested Geneve tunneling with OVS!
