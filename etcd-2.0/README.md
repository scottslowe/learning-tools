# Running an etcd 2.0 Cluster

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) to quickly and relatively easily spin up a cluster running etcd 2.0 (etcd 2.0.9 was the version specifically tested). The configuration was tested using Vagrant 1.7.2, VMware Fusion 6.0.5, and the Vagrant VMware plugin.

## Contents

* **README.md**: This file you're currently reading.

* **servers.yml**: This YAML file contains a list of VM definitions. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs. You will need to edit this file to provide appropriate IP addresses and other VM configuration data (see "Instructions" below).

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

## Instructions

These instructions assume you've already installed VMware Fusion, Vagrant, and the Vagrant VMware plugin. Please refer to the documentation for those products for more information on installation or configuration.

**[NOT YET COMPLETE]**
