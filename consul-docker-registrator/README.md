# Running a Consul Cluster in Vagrant

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) quickly and relatively easily spin up a Consul ([http://www.consul.io](http://www.consul.io)) cluster. The configuration was tested using Vagrant 1.7.2, VMware Fusion 6.0.5, and the Vagrant VMware plugin.

## Contents

* **boostrap.json**: This Consul configuration file contains configuration directives to bootstrap the Consul cluster. This file is copied to `/etc/consul.d/bootstrap/config.json` by the Vagrant file provisioner. You should not need to edit this file.

* **consul.sh**: This shell script is executed by the Vagrant shell provisioner to provision the Ubuntu base box with the Consul binary. This shell script was written for an Ubuntu system; edits will likely be necessary for use with a different Linux distribution.

* **README.md**: This file you're currently reading.

* **server.json**: This Consul configuration file contains configuration directives to run the Consul agent as a server. This file is copied to `/etc/consul.d/server/config.json` by the Vagrant file provisioner. The IP addresses specified in this file _must_ match the IP address specified in `servers.yml`.

* **servers.yml**: This YAML file contains a list of VM definitions. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs. You will need to edit this file to provide appropriate IP addresses and other VM configuration data (see "Instructions" below). If you edit the IP addresses in this file, you _must_ also edit `server.json` to supply matching IP addresses there as well.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

## Instructions

These instructions assume you've already installed VMware Fusion, Vagrant, and the Vagrant VMware plugin. Please refer to the documentation for those products for more information on installation or configuration.

1. Use `vagrant box add` to install an Ubuntu 14.04 x64 box for the vmware_fusion provider. I have a base box you can use for this purpose; to use my Ubuntu 14.04 x64 base box, add the box with `vagrant box add slowe/ubuntu-trusty-x64`.

2. Place the files from the `consul` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`) or just download the specific files from the the `consul` folder.

3. Edit the `servers.yml` file to provide the specific details on the VMs that Vagrant should create. The `Vagrantfile` expects five values for each VM: `name` (the user-friendly name of the VM, which will also be used as the hostname for the guest OS inside the VM); `box` (the name of an Ubuntu 14.04 base box); `ram` (the amount of memory to be assigned to the VM); `vcpu` (the number of vCPUs that should be assigned to the VM); and `priv_ip` (an IP address to be statically assigned to the VM and is used for Consul cluster communications).

4. Once you have edited `servers.yml` (and `server.json`, if you changed the IP addresses in `servers.yml`), use `vagrant up` to bring up the 3 systems that will serve as your Consul cluster.

5. Once Vagrant has finished bringing up the VMs, simply use `vagrant ssh consul-01` (where `consul-01` is the value assigned to the first VM from `servers.yml`) to connect to the VM. No password should be required; it should use the default (insecure) SSH key. Once you are logged into the first VM, bootstrap the Consul cluster with this command:

		consul agent -config-dir /etc/consul.d/bootstrap -advertise 192.168.1.101 -client 0.0.0.0

	If you changed the IP address assigned to the first VM in `servers.yml`, then substitute the appropriate address for the `-advertise` parameter in the above command.

6. In a separate terminal window, connect to the second VM using `vagrant ssh consul-02`. (If you changed the name in `servers.yml`, specify the changed name here.) Launch the second member of the Consul cluster with this command:

		consul agent -config-dir /etc/consul.d/server -advertise 192.168.1.102 -client 0.0.0.0

	Change `192.168.1.102` in the command above if you changed the IP address specified for the second VM in `servers.yml`.

7. In yet another terminal window, connect to the third VM using `vagrant ssh consul-03` (or the name provided in `servers.yml` for the third VM). Launch the third member of the Consul cluster with this command (change the IP address to match what is provided in `servers.yml`):

		consul agent -config-dir /etc/consul.d/server -advertise 192.168.1.103 -client 0.0.0.0

8. At this point, return to the first terminal window (where you are connected to the initial Consul instance launched to bootstrap the cluster) and press Ctrl+C to kill that instance. Launch it again as a "normal" Consul instance with this command:

		consul agent -config-dir /etc/consul.d/server -advertise 192.168.1.101 -client 0.0.0.0 -rejoin

	Consul should re-join the other two nodes in the cluster.

At this point, you have a functional Consul cluster running under Vagrant. If you are using VMware Fusion, you should have IP connectivity to the VMs, and can use the OS X `consul` binary to connect to the cluster and test it. For example, this command would work to demonstrate that Consul is working (you would need to change the IP address provided after `-rpc-addr`):

	consul members -rpc-addr=192.168.1.101:8400

Enjoy!