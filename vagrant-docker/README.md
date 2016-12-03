# Running Docker in Vagrant

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) to quickly and relatively easily launch one or more Docker ([http://www.docker.com](http://www.docker.com)) containers. The configuration was tested using Vagrant 1.7.2, VMware Fusion 6.0.5, and the Vagrant VMware plugin.

## Contents

* **README.md**: The file you're currently reading.

* **host/Vagrantfile**: This file is used by Vagrant to spin up a "host VM" for use with the Vagrant Docker provider. Edit this file to change the Vagrant box you'd like to use; by default, this Vagrantfile uses the "bento/ubuntu-14.04" box (includes support for the "vmware_desktop" provider).

* **Vagrantfile**: This file is used by Vagrant to spin up the Docker containers on the host VM created by `host/Vagrantfile`. Edit this file to change any of the properties of the Docker container you want created by Vagrant.

## Instructions

1. If you wish to use a box _other_ than the "bento/ubuntu-14.04" box (running Ubuntu 14.04), edit the `Vagrantfile` in the host subdirectory.

2. If you wish to run a Docker container with an image _other_ than the standard Nginx image, edit the main `Vagrantfile` and specify a new image.

3. From the directory where the main `Vagrantfile` is located, simply run `vagrant up` to spin up the specified host VM and specified Docker containers. Note that Internet access **will be** required to download Docker and the Docker images.

Enjoy!
