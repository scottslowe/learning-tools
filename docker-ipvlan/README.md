# Using ipvlan Interfaces with Docker Containers

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) to build an environment for working with Docker containers using ipvlan interfaces. This configuration was tested using Vagrant 1.8.1, VMware Fusion 8.1.0, and version 4.0.5 of the Vagrant VMware plugin.

## Contents

* **docker-provision.sh**: This shell script configures the Docker host VM with an experimental build of the Docker binary. No changes to this file are necessary.

* **machines.yml**: This is a YAML file containing the configuration data used by Vagrant when creating and provisioning VMs. This particular Vagrant environment requires six (6) values in this file for each VM: `name` (name to be assigned to the box as well as used for hostname), `box` (the name of the Vagrant box), `ram` (desired RAM), `vcpu` (number of virtual CPUs), `ip_addr` (the private IP address to be assigned to the second network interface; also controls whether a second interface is provisioned), and `docker` (set to "true" or "false"; controls whether Vagrant provisions Docker onto the VM).

* **README.md**: The file you're currently reading.

* **remote-provision.sh**: This shell script configures the remote VM used for testing connectivity. No changes to this file are necessary.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines for this environment. No changes need to be made to this document, as all the configuration data is found in other files (like `machines.yml`). However, if you are using a virtualization solution _other_ than VMware Fusion, you might need to make changes to this file.

## Instructions

These instructions assume you've already installed VMware Fusion, Vagrant, and the Vagrant VMware plugin. Please refer to the documentation for those products for more information on installation or configuration. Note that Internet access is required when using `vagrant up` to create this environment.

1. Use `vagrant box add` to install a 64-bit Ubuntu 15.10 box. I have an Ubuntu 15.10 base box you can use; to use my base box, add the box with `vagrant box add slowe/ubuntu-15.10-server-amd64`. You'll also want to install a 64-bit Ubuntu 14.04 box; you can use `vagrant box add slowe/ubuntu-trusty-x64` to use my base box.

2. Place the files from the `docker-ipvlan` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`) or just download the specific files from the the `docker-ipvlan` folder.

3. If necessary, edit `machines.yml` to specify the name of the Vagrant boxes downloaded in step 1. Editing this file is only necessary if you are _not_ using my base boxes. The VM definition for "docker-01" **must** use Ubuntu 15.10; the other VM may use any version of Ubuntu you prefer. I do not recommend making any other changes to this file.

4. Run `vagrant up` to instantiate the learning environment. This will spin up two (2) VMs based on the Vagrant boxes you downloaded in step 1 and specified in `machines.yml` in step 3. Vagrant will also appropriately configure each VM and start the necessary services. Depending on the speed of your system and your Internet connection, this may take a few minutes.

5. Use `vagrant ssh docker-01` to connect to the Docker host VM. Run `sudo docker pull alpine:latest` to pull down the latest Alpine image for Docker.

6. While still logged into the Docker host VM, run this command to create a Docker network backed by the IPVLAN driver:

        sudo docker network create -d ipvlan \
        --subnet=192.168.100.0/24 --gateway=192.168.100.1 \
        -o ipvlan_mode=l2 -o parent=ens33 ipvlan100

7. Launch a "target" container on the Docker host VM to use in verifying connectivity:

        sudo docker run --net=ipvlan100 --ip=192.168.100.10 -itd alpine /bin/sh

8. In a separate terminal window, use `vagrant ssh remote-01` to connect to the remote system. Test connectivity to the "target" container with `ping -c 4 192.168.100.10`.

9. In the terminal window for the Docker host VM, launch another container with this command:

        sudo docker run --net=ipvlan100 --ip=192.168.100.11 -it --rm alpine /bin/sh

    This will drop you at a container root prompt. Test connectivity to the other container:

        ping -c 4 192.168.100.10

    Now test connectivity to the remote client system:

        ping -c 4 192.168.100.101

You've just deployed IPVLAN L2 interfaces with Docker. Enjoy!
