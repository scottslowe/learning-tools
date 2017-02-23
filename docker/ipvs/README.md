# Load Balancing Docker Containers with IPVS

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) to build an environment to test using IPVS for load balancing Docker containers. This configuration was tested using Vagrant 1.8.1, VMware Fusion 8.1.0, and version 4.0.5 of the Vagrant VMware plugin.


## Contents

* **machines.yml**: This is a YAML file containing the configuration data used by Vagrant when creating and provisioning VMs. This particular Vagrant environment requires six (6) values in this file for each VM: `name` (name to be assigned to the box as well as used for hostname), `box` (the name of the Vagrant box), `ram` (desired RAM), `vcpu` (number of virtual CPUs), `ip_addr` (the private IP address to be assigned to the second network interface; also controls whether a second interface is provisioned), and `docker` (set to "true" or "false"; controls whether Vagrant provisions Docker onto the VM).

* **README.md**: The file you're currently reading.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines for this environment. No changes need to be made to this document, as all the configuration data is found in other files (like `machines.yml`). However, if you are using a virtualization solution _other_ than VMware Fusion, you might need to make changes to this file.

## Instructions

These instructions assume you've already installed your back-end virtualization provider, Vagrant, and any necessary plugins (such as the Vagrant VMware plugin). Please refer to the documentation for those products for more information on installation or configuration. Note that Internet access is required when using `vagrant up` to create this environment.

1. Use `vagrant box add` to install an Ubuntu 14.04 x86_64 Vagrant box. The "bento/ubuntu-14.04" box is a good option for a VMware-formatted box. The "ubuntu/trusty64" box is a good option for a VirtualBox-formatted box.

2. Place the files from the `ipvs-docker` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`) or just download the specific files from the the `ipvs-docker` folder.

3. If necessary, edit `machines.yml` to specify the name of the Vagrant box downloaded in step 1. You may also make changes to the private IP addresses or the RAM/CPU settings, although no changes are typically required.

4. Run `vagrant up` to instantiate the learning environment. This will spin up three (3) VMs based on the Ubuntu 14.04 base box you downloaded in step 1 and specified in `machines.yml` in step 3. Vagrant will also appropriately configure each VM and start the necessary services. Depending on the speed of your system and your Internet connection, this may take a few minutes.

5. Configure the virtual IP (VIP) you'll use to connect to the Docker containers. Run `vagrant ssh lvs-01` to connect to the VM named `lvs-01`, then run this command:

        ip addr add 192.168.100.10/24 dev eth1

6. Log into the first Docker VM using `vagrant ssh docker-01` and launch an Nginx container with this command line:

        docker run -d -p 80:80 nginx:latest

    Repeat this command on the other Docker VM (the VM named `docker-02`).

7. Use `vagrant ssh lvs-01` to log back into the IPVS load balancer and run these commands to set up the virtual server:

        sudo ipvsadm -A -t 192.168.100.10:80 -s rr
        sudo ipvsadm -a -t 192.168.100.10:80 -r 192.168.100.151:80 -g
        sudo ipvsadm -a -t 192.168.100.10:80 -r 192.168.100.152:80 -g

8. Connect to "http://192.168.100.10" to see the load balanced VIP in front of the Docker containers.

Have fun!
