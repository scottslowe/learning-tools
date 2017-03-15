# Docker Swarm Mode with CoreOS

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) quickly and relatively easily test Docker "Swarm Mode" using CentOS Atomic Host. This environment was tested using Vagrant 1.9.1 with VirtualBox 5.1.14, but should work with Vagrant's VMware provider as well.

## Contents

* **machines.yml**: This YAML file contains a list of VM definitions. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs. You will need to edit this file to provide the correct Vagrant box names. Other edits should not be necessary.

* **README.md**: This file you're currently reading.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

## Instructions

These instructions assume you've already installed your virtualization provider (VMware Fusion/Workstation or VirtualBox), Vagrant, and any necessary plugins (such as the Vagrant VMware plugin). Please refer to the documentation for those products for more information on installation or configuration.

1. Use `vagrant box add` to install the CentOS Atomic Host box. For VirtualBox, you can use `vagrant box add centos/atomic-host`. Users of a VMware virtualization provider will likely need to create their own Vagrant box.

2. Place the files from the `docker/atomic-swarm-mode` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`) or just download the specific files from the the `docker/atomic-swarm-mode` folder.

3. If the name of your CentOS Atomic Host Vagrant box is _not_ "centos/atomic-host", edit `machines.yml` to supply the correct box name. This file has separate lines for the VirtualBox and VMware providers; be sure to edit the correct line for your provider.

4. Run `vagrant up` to instantiate 4 VMs---one manager and three workers. All the VMs will be running CentOS Atomic Host.

5. Once Vagrant is finished, use `vagrant ssh manager` to log into the manager VM.

6. Edit the `/etc/docker/daemon.json` file to specify "live-restore" is false (change from true). Restart the Docker daemon with `sudo systemctl restart docker` in order for that change to take effect. (This setting is incompatible with Docker's Swarm mode.)

7. While logged into the manager VM, run `docker swarm init --advertise-addr 192.168.100.100` to create a new swarm. If you have edited the IP addresses supplied in `machines.yml`, then you'll need to adjust the command accordingly to use the IP address supplied for the manager VM.

	The output of that command will provide a command-line to use to join worker nodes to the swarm. Copy and paste this output; you'll need it shortly.

8. Log out of the VM and use `vagrant ssh worker-01` to log in to the first worker VM. Repeat step #6 to make the Docker daemon ready to join the Swarm.

9. Paste the output of step #7 to have the worker node join the swarm.

10. Repeat steps #8 and #9 for the remaining worker nodes.

11. Use `vagrant ssh manager` to log back into the manager VM. Run `docker node ls` to show the Docker Engine instances participating in the swarm.

Enjoy!
