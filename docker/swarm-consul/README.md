# Running a Consul-Backed Docker Swarm Cluster in Vagrant

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) quickly and relatively easily spin up a Docker Swarm (URL) cluster backed by Consul ([http://www.consul.io](http://www.consul.io)). The configuration was tested using Vagrant 1.8.1 and 1.8.5, VMware Fusion 8.1.0 (with the Vagrant VMware plugin), and VirtualBox 5.1.

## Contents

* **config.json.erb**: This Consul configuration file template contains configuration directives to run the Consul agent as a server. This template is used to generate machine-specific configuration files that are copied to `/home/vagrant/config.json` by Vagrant's file provisioner, then moved to `/etc/consul.d/server/config.json` by `consul.sh` when called by Vagrant's shell provisioner. The IP addresses specified on the `retry_join` line in this file are generated based on the IP addresses specified in `machines.yml`.

* **consul.conf**: This Upstart script is used to start the Consul agent and establish the Consul cluster. This file is copied to `/home/vagrant/consul.conf` by Vagrant's file provisioner, then moved to `/etc/init/consul.conf` by the `consul.sh` shell script called by Vagrant's shell provisioner.

* **consul.sh**: This shell script is executed by the Vagrant shell provisioner to create a Consul user, create directories needed by Consul, and provision the Ubuntu base box with the Consul binary. This shell script was written for an Ubuntu system; edits will likely be necessary for use with a different Linux distribution.

* **machines.yml**: This YAML file contains a list of VM definitions. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs. You will need to edit this file to provide appropriate IP addresses and other VM configuration data (see "Instructions" below).

* **README.md**: This file you're currently reading.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

* **user_data**: This file is used by the CoreOS cloud-init process to customize the CoreOS VMs upon instantiation. This file disables etcd and fleet, and configures Docker to listen on TCP port 2375.

## Instructions

These instructions assume you've already installed your virtualization provider (VMware Fusion/Workstation or VirtualBox), Vagrant, and any necessary plugins (such as the Vagrant VMware plugin). Please refer to the documentation for those products for more information on installation or configuration.

1. Use `vagrant box add` to install an Ubuntu 14.04 x64 box. The "bento/ubuntu-14.04" box is a good option here.

2. Use `vagrant box add` to install a CoreOS base box. The `Vagrantfile` assumes you are using the Stable release of CoreOS (the box named `coreos-stable`). If the box name is different, you'll need to edit the `Vagrantfile` accordingly. You'll need to be sure to use a version of CoreOS that comes with Docker 1.4.0 or later, as Swarm requires Docker => 1.4.0. Note that CoreOS Stable 557.2.0 comes with Docker 1.4.1.

3. Place the files from the `docker-swarm` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`) or just download the specific files from the the `docker-swarm` folder.

4. Optionally, edit the `machines.yml` file to provide the specific details on the VMs that Vagrant should create. The `Vagrantfile` expects five values for each VM: `name` (the user-friendly name of the VM, which will also be used as the hostname for the guest OS inside the VM); `vmw_box` (the name of a VMware-formatted Ubuntu 14.04 base box); `vb_box` (the name of a VirtualBox-formatted Ubuntu 14.04 base box); `ram` (the amount of memory to be assigned to the VM); `vcpu` (the number of vCPUs that should be assigned to the VM); and `ip_addr` (an IP address to be statically assigned to the VM and is used for Consul cluster communications). _It is not required to edit this file. You can use it without any modifications if desired._

5. Once you have edited `machines.yml`, use `vagrant up` to bring up the 6 systems. Three VMs will run the Consul cluster; the other 3 VMs will be running CoreOS and will make up the Docker Swarm cluster.

6. Once Vagrant has finished bringing up the VMs, simply use `vagrant ssh consul-01` (where `consul-01` is the value assigned to the first VM from `machines.yml`) to connect to the VM. No password should be required; it should use the default (insecure) SSH key. Once you are logged into the first VM, verify Consul is running correctly by running this command:

		consul members list

	Consul should report three members, using the IP addresses specified in `machines.yml`. If Consul does not report three members (a minimum to bootstrap the cluster) or if it reports an error, you'll need to resolve this before continuing.

7. Use `vagrant ssh coreos-01` to log into the first CoreOS system (`coreos-01` is the default name supplied in `machines.yml`; if you've changed the default name, modify your command appropriately). On this system, launch a Dockerized Consul client using the following command:

		docker run -d -p 8300:8300 -p 8301:8301 -p 8301:8301/udp -p 8302:8302 -p 8302:8302/udp -p 8400:8400 -p 8500:8500 -p 8600:8600/udp --name consul-coreos-01 -h coreos-01 progrium/consul -rejoin -advertise 192.168.1.104 -join 192.168.1.101

	If you've changed the IP addresses in `machines.yml`, be sure to modify the command above with the correct IP addresses (`-advertise` needs to specify the IP address assigned to the first CoreOS node, and `-join` needs to provide the IP address of one of the Consul nodes).

8. While still logged into the first CoreOS system, launch an instance of Registrator:

		docker run -d --name reg-coreos-01 -h coreos-01 -v /var/run/docker.sock:/tmp/docker.sock gliderlabs/registrator consul://192.168.1.104:8500

	The IP address in this command must correspond to the IP address assigned to the CoreOS node as specified in `machines.yml`.

9. While still logged into the first CoreOS system, add it as the first node to a new Docker Swarm cluster using this command:

		docker run -d swarm join --addr=192.168.1.104:2375 consul://192.168.1.104:8500/swarm

10. Log out of the first CoreOS system and use `vagrant ssh` to log into the second and third CoreOS systems, repeating steps 7, 8, and 9 on each system. Be sure to change values for the `--name` and `-h` parameters on each system. Also be sure that the `-advertise` parameter for the Consul container maps to the IP address assigned to the host CoreOS VM (as provided in `machines.yml`). Make sure the `consul://` parameter for the Registrator container points to the CoreOS VM's IP address as provided in `machines.yml` as well.

11. On the first CoreOS system (the one named `coreos-01` by default), run a Docker container that will serve as the manager of the Swarm cluster. Launch the Swarm manager with this command:

		docker run -d -p 8333:2375 swarm manage consul://192.168.1.104:8500/swarm

	The IP address specified in the `consul://` URL should correspond to the IP address assigned from `machines.yml` to this node. Make note of the port exposed by the `-p` command and the IP address of the node on which you're running the Swarm manager. _This will be the endpoint against which you will run Docker commands against the cluster._

12. Verify the operation of the Swarm cluster by running this command (from any system that has connectivity to the CoreOS system running the Swarm manager container launched in the previous step):

		docker -H tcp://192.168.1.104:8333 info

	Docker should return information indicating that there are 10 containers running across 3 nodes, and then provide more information about each node and the containers running on that node.

13. Launch an Nginx container somewhere on the cluster with this command:

		docker -H tcp://192.168.1.104:8333 run -d --name www -p 80:80 nginx

If everything is working as expected, Docker will launch an Nginx container on one of the CoreOS nodes in the Swarm cluster, and Registrator will register the presence of the container in Consul for service discovery.

Enjoy!
