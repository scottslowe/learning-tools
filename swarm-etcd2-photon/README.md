# Running an etcd-Backed Swarm Cluster on Photon

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) to quickly and relatively easily spin up a Docker Swarm cluster on VMware Photon, with discovery services provided by etcd 2.0 on Ubuntu. The configuration was tested using Vagrant 1.7.4 and Oracle VirtualBox 5.0.0.

## Contents

* **ansible.cfg**: This is an Ansible configuration file to fine-tune how Ansible behaves when called by Vagrant. No edits to this file should be necessary.

* **cfg-docker.yml**: This Ansible playbook configures the Docker Engine in the learning environment. It's called automatically by Vagrant. No edits to this file should be needed.

* **cfg-etcd.yml**: This Ansible playbook installs and configures the etcd cluster. No edits to this file should be needed.

* **docker-tcp.socket**: This systemd unit enables the Docker Engine to listen on a network socket. You should not need to edit this file.

* **docker.service**: This systemd unit replaces the default systemd unit for Docker Engine on Photon OS hosts. No edits to this file should be needed.

* **docker.socket**: This systemd unit configures the local UNIX socket for Docker Engine. No changes to this file should be needed.

* **etcd.conf**: This is an Upstart script (written for Ubuntu) to start etcd. No modifications to this file should be necessary. This file is installed by the provisioning script called in `Vagrantfile`.

* **etcd.defaults.erb**: A template used by Vagrant to create machine-specific service defaults files for each node in the etcd cluster. The appropriate node-specific file is installed as `etcd` in the `/etc/default` directory on each node by the provisioning script called in `Vagrantfile`.

* **machines.yml**: This YAML file contains a list of VM definitions. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs. You may need to edit this file to provide appropriate IP addresses and other VM configuration data (see "Instructions" below).

* **README.md**: This file you're currently reading.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file in `machines.yml`.

## Instructions

These instructions assume you've already installed your back-end virtualization provider (VirtualBox or VMware Fusion/Workstation), Vagrant, and any necessary Vagrant plugins (such as the VMware plugin). Please refer to the documentation for those products for more information on installation or configuration.

1. Use `vagrant box add` to install an Ubuntu 14.04 x64 box. The "bento/ubuntu-14.04" box is one good option with support for both VMware and VirtualBox. If you don't already have a Vagrant box for Photon, use `vagrant box add` to download a Photon box as well.

2. Place the files from the `swarm-etcd2-photon` directory of this GitHub repository (the "lowescott/learning-tools" repository) into a directory on your system. You can clone the entire "learning-tools" repository (using `git clone`), or just download the specific files from the `swarm-etcd2-photon` directory.

3. Edit `machines.yml` to ensure that the box specified in that file matches the Ubuntu 14.04 x64 base box you just installed and will be using. _I recommend that you do not change any other values in this file unless you know it is necessary._

4. From a terminal window, change into the directory where the files from this directory are stored and run `vagrant up` to bring up the VMs specified in `machines.yml` and `Vagrantfile`.

5. Once Vagrant has finished creating, booting, and provisioning each of the VMs and starting etcd, log into the first system ("etcd-01" by default) using `vagrant ssh etcd-01`.

6. You can test etcd with this command:

		etcdctl member list
	
	This should return a list of three nodes as members of the etcd cluster. If you receive an error or don't see all three VMs listed, tear down the Vagrant environment with `vagrant destroy` and recreate the environment from scratch. If you continue to experience problems, open an issue here on GitHub (or file a pull request fixing the problem).

7. Log into the first Photon OS instance using `vagrant ssh photon-01`. Launch the first Swarm container:

        docker run -d swarm join --addr=192.168.100.111:2375 etcd://192.168.100.101:2379/swarm

    Make sure the IP address referenced by the `--addr=` parameter matches the IP address assigned to the Photon OS VM (which is pulled from the "ip_addr" key in `machines.yml`). Further, make sure the IP address in the "etcd://" URL matches the IP address assigned to one of the Ubuntu-based etcd cluster nodes.

8. Repeat step 7 with `photon-02` and `photon-03`, making sure to change the value of the `--addr=` parameter appropriately on each system. The default values from `machines.yml` are 192.168.100.111 (photon-01), 192.168.100.112 (photon-02), and 192.168.100.113 (photon-03).

9. On `photon-04`, launch the Swarm manager with this command:

        docker run -d -p 8333:2375 swarm manage etcd://192.168.100.101:2379/swarm

    The IP address specified in the "etcd://" URL should correspond to the IP address assigned to one of the Ubuntu-based etcd cluster nodes. Make note of the port exposed by the `-p` command and the IP address of the node on which you're running the Swarm manager. _This will be the endpoint against which you will run Docker commands against the cluster._

10. Verify the operation of the Swarm cluster by running this command (from any system that has connectivity to the Photon OS system running the Swarm manager container launched in the previous step):

        docker -H tcp://192.168.100.114:8333 info

    Docker should return information indicating that there are 3 containers running across 3 nodes, and then provide more information about each node and the containers running on that node.

11. Launch an Nginx container somewhere on the cluster with this command:

        docker -H tcp://192.168.100.114:8333 run -d --name www -p 80:80 nginx

    If everything is working as expected, Docker will launch an Nginx container on one of the Photon OS nodes in the Swarm cluster. Re-running the command from step 10 can help track it down, as well as using this command:

        docker -H tcp://192.168.100.114:8333 ps

Enjoy!
