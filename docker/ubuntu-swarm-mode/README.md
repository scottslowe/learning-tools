# Running a Docker Swarm Mode Cluster Locally

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) and Ansible ([http://www.ansible.com](http://www.ansible.com)) to quickly and relatively easily spin up a Docker Swarm mode cluster.

## Contents

* **ansible.cfg**: This Ansible configuration file supplies configuration to Ansible to streamline integration with the Vagrant Ansible provisioner.

* **create-swarm.yml**: This Ansible playbook configures a group of VMs (created by Vagrant) to be a Docker Swarm cluster.

* **destroy-swarm.yml**: This Ansible playbook forcefully destroys the Docker Swarm mode cluster.

* **machines.yml**: This YAML file contains a list of VM definitions. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs. Generally, the only change needed to this file is to specify the correct Vagrant box you will be used (see "Instructions" below). If necessary, you may need to edit the IP addresses supplied in this file to avoid IP addressing conflicts with other networks.

* **provision.yml**: This is the Ansible playbook that will perform a very light edit to the VMs, primarily for the purpose of creating the appropriate Ansible inventory file by the . No edits to this file should be necessary.

* **README.md**: This file you're currently reading.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

## Instructions

These instructions assume you've already installed your virtualization provider (typically VirtualBox, VMware Fusion, or VMware Workstation), Vagrant, any necessary Vagrant plugins, and Ansible. Please refer to the documentation for those products for more information on installation or configuration.

1. Use `vagrant box add` to install an Ubuntu 14.04 x64 box for the vmware_fusion provider. The "bento/ubuntu-14.04" box is a good option here.

2. Place the files from the `docker/ubuntu-swarm-mode` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`) or just download the specific files from the the `docker/ubuntu-swarm-mode` folder.

3. Edit the `machines.yml` file to specify the box you will use (as specified in step 1). If necessary to avoid IP address conflicts with existing networks, you may also need to edit the IP addresses specified in this file. Generally, no other changes are needed, although (if you are comfortable with the settings) you can adjust the number of virtual CPUs and/or the amount of RAM assigned to each Vagrant VM in this file as well. Note that this environment _assumes_ the presence of an `eth1` in each Vagrant VM; therefore, do not remove the "ip_addr" value from `machines.yml`.

4. Once you have edited `machines.yml`, use `vagrant up` to bring up the systems that will serve as your Swarm mode cluster. As part of the process of running `vagrant up`, you'll see Ansible perform a quick task on each VM.

5. Once Vagrant has finished bringing up the VMs, run `ansible-playbook create-swarm.yml`. This will configure the VMs and get a Docker Swarm mode cluster up and running.

At this point, you have a functional Docker Swarm mode cluster. You should have IP connectivity to the hosts, and you can log into the manager and run `docker node ls` to see the nodes in the Swarm mode cluster.

Enjoy!

## License

This content is licensed under the MIT License.
