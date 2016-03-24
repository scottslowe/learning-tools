# Using IPVLAN L3 Interfaces with Docker Containers

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) to build an environment for working with Docker containers using IPVLAN L3 interfaces (IPVLAN interfaces running in L3 mode instead of L2 mode). This configuration was tested using Vagrant 1.8.1, VMware Fusion 8.1.0, and version 4.0.5 of the Vagrant VMware plugin.

## Contents

* **docker-01-setup.sh**: This shell script configures the Docker host VM named "docker-01" with an experimental build of the Docker binary. No changes to this file are necessary.

* **docker-02-setup.sh**: This shell script configures the Docker host VM named "docker-02" with an experimental build of the Docker binary. No changes to this file are necessary.

* **machines.yml**: This is a YAML file containing the configuration data used by Vagrant when creating and provisioning VMs. This particular Vagrant environment requires four (4) values in this file for each VM: `name` (name to be assigned to the box as well as used for hostname), `box` (the name of the Vagrant box), `ram` (desired RAM), and `vcpu` (number of virtual CPUs).

* **README.md**: The file you're currently reading.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines for this environment. No changes need to be made to this document, as all the configuration data is found in other files (like `machines.yml`). However, if you are using a virtualization solution _other_ than VMware Fusion, you might need to make changes to this file.

## Instructions

These instructions assume you've already installed VMware Fusion, Vagrant, and the Vagrant VMware plugin. Please refer to the documentation for those products for more information on installation or configuration. Note that Internet access is required when using `vagrant up` to create this environment.

1. Use `vagrant box add` to install a 64-bit Ubuntu 15.10 box. I have an Ubuntu 15.10 base box you can use; to use my base box, add the box with `vagrant box add slowe/ubuntu-15.10-server-amd64`.

2. Place the files from the `docker-ipvlan-l3` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`) or just download the specific files from the the `docker-ipvlan-l3` folder.

3. If necessary, edit `machines.yml` to specify the name of the Vagrant boxes downloaded in step 1. Editing this file is only necessary if you are _not_ using my base boxes. Please note that a box with a minimum of version 4.2 of the Linux kernel is needed.

4. Run `vagrant up` to instantiate the learning environment. This will spin up two (2) VMs based on the Vagrant boxes you downloaded in step 1 and specified in `machines.yml` in step 3. Vagrant will also appropriately configure each VM and start the necessary services. Depending on the speed of your system and your Internet connection, this may take a few minutes.

5. Use `vagrant ssh docker-01` to connect to the Docker host VM. Run `sudo docker pull alpine:latest` to pull down the latest Alpine image for Docker.

6. While still logged into "docker-01", run this command to create a Docker network backed by the IPVLAN driver:

        sudo docker network create -d ipvlan \
        --subnet=10.1.100.0/24 --gateway=10.1.100.1 \
        -o ipvlan_mode=l3 -o parent=ens33 ipvlan100

7. In a separate terminal window, change to the directory where the files from the `docker-ipvlan-l3` folder are stored and run `vagrant ssh docker-02` to log into the second Docker host VM.

8. Create the IPVLAN-backed Docker network:

        sudo docker network create -d ipvlan \
        --subnet=10.1.101.0/24 --gateway=10.1.101.1 \
        -o ipvlan_mode=l3 -o parent=ens33 ipvlan101

9. On `docker-01`, launch a "target" container to use in verifying connectivity:

        sudo docker run --net=ipvlan100 --ip=10.1.100.10 -itd alpine /bin/sh

10. From `docker-02`, use `ping` to try to connect to the "target" container on `docker-01`. The ping should fail.

11. Still on `docker-02`, add a route to the "target" container:

        ip route add 10.1.100.10/32 via 192.168.100.100 dev ens33

12. Repeat step #10. The ping should succeed this time.

13. On `docker-02`, launch a "target" container to use in verifying connectivity:

        sudo docker run --net=ipvlan101 --ip=10.1.101.10 -itd alpine /bin/sh

14. From `docker-01`, try to ping the "target" container you just launched in the previous step. The ping should fail.

15. Add a route on `docker-01` to the "target" container you launched in step #13:

        ip route add 10.1.101.10/32 via 192.168.100.101 dev ens33

16. Repeat step #14. The connectivity test should succeed.

You've just deployed IPVLAN L3 interfaces with Docker. Enjoy!
