# Running Multiple Docker Containers in Vagrant with YAML

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) to quickly and relatively easily launch multiple Docker ([http://www.docker.com](http://www.docker.com)) containers. The specifics of the Docker containers are specified in an external YAML file. The configuration was tested using Vagrant 1.8.5, VMware Fusion 8.1.0 (with the Vagrant VMware plugin), and VirtualBox 5.1.

## Contents

* **containers.yml**: This YAML file contains a list of the Docker containers and their properties for use by Vagrant. You will need to edit this file to specify container friendly name, image, and exposed ports.

* **hostvms.yml**: This YAML file contains the configuration data for the "host VM" that the Vagrant Docker provider will use. Edit this file to change the Vagrant box used by the "host VM," or the VM configuration.

* **README.md**: The file you're currently reading.

* **VagrantfileHost**: This file is used by Vagrant to spin up a "host VM" for use with the Vagrant Docker provider. No edits should be necessary to this file.

* **Vagrantfile**: This file is used by Vagrant to spin up the Docker containers on the host VM created by `VagrantfileHost`. Unless you change filenames, you should not need to edit this file. All container details are stored in a separate YAML file.

## Instructions

1. Use `vagrant box add` to install an Ubuntu 14.04 base box. For a VMware-formatted box, the "bento/ubuntu-14.04" box is a good option; on VirtualBox, you can use the "ubuntu/trusty64" box.

2. Edit `hostvms.yml` to specify the name of the box you added in step #1.

3. Edit the `containers.yml` file to specify the Docker containers that Vagrant should create on the host VM. Each container should contain three properties: `name` (the friendly name to be assigned to the Docker container), `image` (the name of the Docker image to be used for this container), and `ports` (a list of ports to be exposed for the container).

4. From the directory where the main `Vagrantfile` is located, simply run `vagrant up` to spin up the specified host VM and specified Docker containers. Note that Internet access **will be** required to download Docker and the Docker images.

Enjoy!
