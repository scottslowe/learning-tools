# Docker Swarm Mode with CoreOS

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) quickly and relatively easily test Docker "Swarm Mode" using CoreOS Stable. This environment was tested using Vagrant 1.9.1 with VirtualBox 5.1.14, but should work with Vagrant's VMware provider as well.

## Contents

* **machines.yml**: This YAML file contains a list of VM definitions. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs. You will need to edit this file to provide the correct Vagrant box names. Other edits should not be necessary.

* **README.md**: This file you're currently reading.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

## Instructions

These instructions assume you've already installed your virtualization provider (VMware Fusion/Workstation or VirtualBox), Vagrant, and any necessary plugins (such as the Vagrant VMware plugin). Please refer to the documentation for those products for more information on installation or configuration.

1. Use `vagrant box add` to install the CoreOS Vagrant box. The following URLs can be used with the `vagrant box add` command to install the CoreOS Vagrant box:

	For VirtualBox: [https://stable.release.core-os.net/amd64-usr/current/coreos_production_vagrant.json](https://stable.release.core-os.net/amd64-usr/current/coreos_production_vagrant.json)
	For VMware Fusion: [https://stable.release.core-os.net/amd64-usr/current/coreos_production_vagrant_vmware_fusion.json](https://stable.release.core-os.net/amd64-usr/current/coreos_production_vagrant_vmware_fusion.json)

	This should install a Vagrant box named "coreos-stable". The `machines.yml` file in this environment assumes this is the name of the box; if you are using a different name, you'll need to edit `machines.yml` accordingly.

2. Place the files from the `docker/coreos-swarm-mode` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`) or just download the specific files from the the `docker/coreos-swarm-mode` folder.

3. As noted in step #1, if the name of your CoreOS Vagrant box is _not_ "coreos-stable", edit `machines.yml` to supply the correct box name.

4. Run `vagrant up` to instantiate 4 VMs---one manager and three workers. All the VMs will be running CoreOS.

5. Once Vagrant is finished, use `vagrant ssh manager` to log into the manager VM.

6. While logged into the manager VM, run `docker swarm init --advertise-addr 192.168.100.100` to create a new swarm. If you have edited the IP addresses supplied in `machines.yml`, then you'll need to adjust the command accordingly to use the IP address supplied for the manager VM.

	The output of that command will provide a command-line to use to join worker nodes to the swarm. Copy and paste this output; you'll need it shortly.

7. Log out of the VM and use `vagrant ssh worker-01` to log in to the first worker VM. Paste the output of the command in step #6 to have the worker join the swarm.

8. Repeat step #7 for the remaining worker nodes.

9. Use `vagrant ssh manager` to log back into the manager VM. Run `docker node ls` to show the Docker Engine instances participating in the swarm.

Enjoy!
