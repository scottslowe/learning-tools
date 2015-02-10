# Running Docker in Vagrant

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) to quickly and relatively easily launch one or more Docker ([http://www.docker.com](http://www.docker.com)) containers. The configuration was tested using Vagrant 1.7.2, VMware Fusion 6.0.5, and the Vagrant VMware plugin.

## Contents

* **README.md**: The file you're currently reading.

* **host/Vagrantfile**: This file is used by Vagrant to spin up a "host VM" for use with the Vagrant Docker provider. Edit this file to change the Vagrant box you'd like to use; by default, this Vagrantfile uses the "slowe/ubuntu-trusty-x64" box (built for the "vmware_desktop" provider).

* **Vagrantfile**: This file is used by Vagrant to spin up the Docker containers on the host VM created by `host/Vagrantfile`. Edit this file to change any of the properties of the Docker container you want created by Vagrant.

## Instructions

