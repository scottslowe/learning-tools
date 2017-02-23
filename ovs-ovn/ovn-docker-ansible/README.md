# Open Virtual Network (OVN) with Docker

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) to quickly and  easily spin up an environment to test Open Virtual Network (OVN), a part of the Open vSwitch project, to provide overlay networking for Docker containers.

## Contents

* **10-exec-options.conf.j2**: This Jinja2 template is used to create a systemd drop-in file to customize the default behavior of the Docker Engine. The file created by this template is placed by Ansible at `/etc/systemd/system/docker.service.d`.

* **ansible.cfg**: This is an Ansible configuration file that instructs Ansible to use the "vagrant" remote user, and to use Vagrant's built-in Ansible inventory.

* **config.json.j2**: This Jinja2 template is used to create a Consul configuration file to turn up the Consul cluster. The configuration file created from this template is placed at `/etc/consul.d/server`.

* **consul-server.service**: This is a systemd unit file for the Consul cluster.

* **machines.yml**: This YAML file contains a list of VM definitions and associated configuration data. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs.

* **provision.yml**: This Ansible playbook installs the OVS/OVN components on each Vagrant machine. It's called automatically by Vagrant.

* **README.md**: This file you're currently reading.

* **setup.sh.j2**: This Jinja2 template is used by Ansible to build a customized OVS/OVN configuration script for each Vagrant machine. The shell script created by this template is placed by Ansible in `/home/vagrant/setup.sh`.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

## Instructions

These instructions assume you've already installed Vagrant, your back-end virtualization provider (such as VMware Fusion or VirtualBox), and any necessary plugins (such as the Vagrant VMware plugin). Please refer to the documentation for those products for more information on installation or configuration.

1. Use `vagrant box add` to add an Ubuntu 16.04 box to your system. The "bento/ubuntu-16.04" box is very good for both VirtualBox and VMware Fusion/Workstation. (Note that the "ubuntu/xenial64" box for VirtualBox is currently broken under Vagrant.)

2. Edit the `machines.yml` file to ensure the box you downloaded in step 1 is specified on the "box:" sections of this file. Specify the name of a VMware-formatted box on the "vmw:" line; place the name of a VirtualBox-formatted box on the "vb:" line.

3. Run `vagrant up` to have Vagrant instantiate the three machines configured by default in this environment. Vagrant will launch a simple Ansible playbook to populate inventory, but it will _not_ actually configure the VM. (Note you'll need Ansible installed locally on the system where you're running `vagrant up`.)

4. After Vagrant has instantiated all the VMs, run `ansible-playbook provision.yml` to run the full provisioning playbook.

5. Use `vagrant ssh ovn-01` to log into the first system. Run `sudo ./setup.sh` to perform final configuration steps for OVS/OVN.

6. Repeat step #4 with `ovn-02` and `ovn-03`. At this point, OVN is up and running, with the OVN central components running on "ovn-01".

7. To add Docker support to OVN, log into "ovn-01" and run `/usr/bin/ovn-docker-overlay-driver --detach`. This will launch the Docker driver for OVS/OVN.

8. Repeat the previous step on "ovn-02" and "ovn-03". Your OVN environment now has Docker networking support.

9. Create a Docker network using `docker network create -d openvswitch --subnet=<A.B.C.D/24> <name>`. You will note an OVN logical switch is created that corresponds to the Docker network.

10. Launch a Docker container and attach it to the new network with `docker run -d --net=<name> <image>`. You will note OVN logical ports added to the OVN logical switch.

Enjoy!
