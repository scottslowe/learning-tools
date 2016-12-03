# Running an etcd-Backed Docker Swarm Cluster in Vagrant

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) quickly and relatively easily spin up a Docker Swarm ([http://docs.docker.com/swarm/](http://docs.docker.com/swarm/)) cluster backed by etcd ([https://github.com/coreos/etcd/](https://github.com/coreos/etcd/)). The configuration was tested using Vagrant 1.8.6, VMware Fusion 8.1.0 with the Vagrant VMware plugin, and VirtualBox 5.1.

## Contents

* **etcd.conf**: This is an Upstart script (written for Ubuntu) to start etcd. No modifications to this file should be necessary. This file is installed by the provisioning script called in `Vagrantfile`.

* **etcd.defaults.erb**: A template used by Vagrant to create machine-specific service defaults files for each node in the etcd cluster. The appropriate node-specific file is installed as `etcd` in the `/etc/default` directory on each node by the provisioning script called in `Vagrantfile`. These files are generated for each VM whose "etcd" value in `machines.yml` is set to "true".

* **provision.sh**: This provisioning script is called by the shell provisioner in `Vagrantfile`. It downloads the etcd 2.3.7 release from GitHub, expands it, creates necessary directories, and places files in the appropriate locations. Finally it starts etcd (or restarts it if already running).

* **README.md**: This file you're currently reading.

* **machines.yml**: This YAML file contains a list of VM definitions and associated configuration data. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs. You _might_ need to edit this file to change the IP addresses (the default IP addresses may not work in all environments).

* **user_data**: This file is used by the CoreOS cloud-init process to customize the CoreOS VMs upon instantiation. This file disables etcd and fleet, and configures Docker to listen on TCP port 2375.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

## Instructions

These instructions assume you've already installed your virtualization provider (VMware Fusion/Workstation or VirtualBox), Vagrant, and any applicable Vagrant plugins (like the Vagrant VMware plugin). Please refer to the documentation for those products for more information on installation or configuration.

1. Use `vagrant box add` to install an Ubuntu 14.04 x64 box for your virtualization provider. If you are using VMware Fusion/Workstation, the "bento/ubuntu-14.04" box is a good option. For VirtualBox, the "ubuntu/trusty64" box will work fine.

2. Use `vagrant box add` to install a CoreOS base box. The `Vagrantfile` assumes you are using the Stable release of CoreOS (the box named `coreos-stable`). If the box name is different, you'll need to edit both `machines.yml` and the `Vagrantfile` to specify the correct box name. You'll need to be sure to use a version of CoreOS that comes with Docker 1.4.0 or later, as Swarm requires Docker => 1.4.0. Note that CoreOS Stable 557.2.0 comes with Docker 1.4.1.

3. Place the files from the `docker-swarm-etcd` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`) or just download the specific files from the the `docker-swarm-etcd` folder.

4. Edit the `machines.yml` file to make sure the box names specified in this file match the names of the boxes you added to your system in steps 1 and 2. _Generally speaking, no other changes to this file should be necessary._ The `Vagrantfile` expects six values for each VM: `name` (the user-friendly name of the VM, which will also be used as the hostname for the guest OS inside the VM); `box` (two subvalues which provide the name of the Vagrant box to use for the VMware and VirtualBox providers); `ram` (the amount of memory to be assigned to the VM); `vcpu` (the number of vCPUs that should be assigned to the VM); `ip_addr` (an IP address to be statically assigned to the VM and is used for Consul cluster communications); and `etcd` (set to "true" for a node to be included in the etcd cluster configuration; any other value will exclude it from the etcd cluster configuration).

5. Edit `Vagrantfile` to make sure the box names referenced in this file (on line 90) match the names of the boxes you added to your system in steps 1 and 2.

6. Once you have edited `machines.yml` (and `Vagrantfile`, if necessary), use `vagrant up` to bring up the 6 systems. Three VMs will run the etcd cluster; the other 3 VMs will be running CoreOS and will make up the Docker Swarm cluster.

7. Once Vagrant has finished bringing up the VMs, simply use `vagrant ssh etcd-01` (where `etcd-01` is the value assigned to the first VM from `machines.yml`) to connect to the VM. No password should be required; it should use the default (insecure) SSH key. Once you are logged into the first VM, verify the operation of etcd with this command:

		etcdctl member list

	This command should return a list of three members, which are the first three VMs (based on an Ubuntu 14.04 box) specified in `machines.yml`. If the command does not return a list of three members, then you'll need to troubleshoot etcd.

8. Use `vagrant ssh coreos-01` to log into the first CoreOS system (`coreos-01` is the default name supplied in `machines.yml`; if you've changed the default name, modify your command appropriately). Add it as the first node to a new Docker Swarm cluster using this command:

		docker run -d swarm join --addr=192.168.100.111:2375 etcd://192.168.100.101:2379/swarm
	
	Make sure the IP address referenced by the `--addr=` parameter matches the IP address assigned to the CoreOS VM (which is pulled from the "ip_addr" key in `machines.yml`). Further, make sure the IP address in the "etcd://" URL matches the IP address assigned to one of the Ubuntu-based etcd cluster nodes.

9. Log out of the first CoreOS system and use `vagrant ssh` to log into the second and third CoreOS systems, repeating step 8 on each system. Be sure to change values for the `--addr=` parameter on each system. The default values from `machines.yml` use the addresses 192.168.100.111 (coreos-01), 192.168.100.112 (coreos-02), and 192.168.100.113 (coreos-03).

9. On the first CoreOS system (the one named `coreos-01` by default), run a Docker container that will serve as the manager of the Swarm cluster. Launch the Swarm manager with this command:

		docker run -d -p 8333:2375 swarm manage etcd://192.168.100.101:2379/swarm

	The IP address specified in the "etcd://" URL should correspond to the IP address assigned to one of the Ubuntu-based etcd cluster nodes. Make note of the port exposed by the `-p` command and the IP address of the node on which you're running the Swarm manager. _This will be the endpoint against which you will run Docker commands against the cluster._

10. Verify the operation of the Swarm cluster by running this command (from any system that has connectivity to the CoreOS system running the Swarm manager container launched in the previous step):

		docker -H tcp://192.168.100.111:8333 info

	Docker should return information indicating that there are 4 containers running across 3 nodes, and then provide more information about each node and the containers running on that node.

11. Launch an Nginx container somewhere on the cluster with this command:

		docker -H tcp://192.168.100.111:8333 run -d --name www -p 80:80 nginx

	If everything is working as expected, Docker will launch an Nginx container on one of the CoreOS nodes in the Swarm cluster. Re-running the command from step 10 can help track it down, as well as using this command:

		docker -H tcp://192.168.100.111:8333 ps

Enjoy!
