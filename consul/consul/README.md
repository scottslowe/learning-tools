# Running a Consul Cluster in Vagrant

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) quickly and relatively easily spin up a Consul ([http://www.consul.io](http://www.consul.io)) cluster. The configuration was tested using Vagrant, VMware Fusion, VirtualBox, and Libvirt.

## Contents

* **consul.service**: This systemd unit file enables Consul to run as a daemon. No edits should be necessary to this file (the `consul.sh` makes a few in-place edits during provisioning).

* **consul.sh**: This shell script is executed by the Vagrant shell provisioner to provision the Ubuntu base box with the Consul binary. This shell script was written for an Ubuntu system; edits will likely be necessary for use with a different Linux distribution.

* **machines.yml**: This YAML file contains a list of VM definitions. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs. You will need to edit this file to provide appropriate IP addresses and other VM configuration data (see "Instructions" below). If you edit the IP addresses in this file, you _must_ also edit `server.json` to supply matching IP addresses there as well.

* **README.md**: The file you're currently reading.

* **server.json**: This Consul configuration file contains configuration directives to run the Consul agent as a server. This file is copied to `/etc/consul.d/server/config.json` by the Vagrant file provisioner. The IP addresses specified in this file _must_ match the IP address specified in `machines.yml`.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

## Instructions

These instructions assume you've already installed the virtualization provider (VMware Fusion or VirtualBox), Vagrant, and any necessary plugins (such as the Vagrant VMware plugin). Please refer to the documentation for those products for more information on installation or configuration.

1. Use `vagrant box add` to install an Ubuntu 16.04 x64 box for your virtualization provider. The `machines.yml` file contains some suggested boxes for VirtualBox, VMware, and Libvirt.

2. If you decide to use boxes _other_ than those already listed in `machines.yml`, edit this file to specify the box being used. It may be necessary to edit the IP addresses being assigned, but otherwise no other edits should be needed. If you do edit the IP addresses, you _must_ edit `config.json` as well.

3. Place the files from the `consul` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`) or just download the specific files from the the `consul` folder.

4. Once you have edited `machines.yml` (and `server.json`, if you changed the IP addresses in `machines.yml`), use `vagrant up` to bring up the 3 systems that will serve as your Consul cluster.

5. Once Vagrant has finished bringing up the VMs, simply use `vagrant ssh consul-01` (where `consul-01` is the value assigned to the first VM from `servers.yml`) to connect to the VM. No password should be required; it should use the default (insecure) SSH key. Once you are logged into the first VM, start Consul using `sudo service consul start`.

6. Repeat step #5 for the remaining two VMs (`consul-02` and `consul-03`).

At this point, you have a functional Consul cluster running under Vagrant. Note that the Consul cluster is not accessible from outside the VMs, as Consul is only listening on the loopback address on each VM. Enjoy!
