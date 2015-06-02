# Learning Tools

This repository contains a variety of files and tools for learning new technologies. It is aimed at existing IT professionals who need some help coming up to speed with new technologies, products, or open source projects.

## Contents

**consul**: In this folder you'll find a `Vagrantfile` and supporting documents to run a Consul cluster under Vagrant on your local laptop.

**docker-swarm**: This folder contains a `Vagrantfile` and supporting files to run a Consul-backed Docker Swarm cluster under Vagrant on your local system.

**docker-swarm-etcd**: In this folder is a `Vagrantfile` and supporting files for turning up an etcd 2.0-backed Docker Swarm cluster.

**etcd-2.0**: Use the `Vagrantfile` and other files in this directory to turn up an etcd 2.0.9 cluster running on Ubuntu 14.04.

**lxd-ovs**: Use the `Vagrantfile` and other files in this directory to work with LXD and Open vSwitch (OVS) 2.3.1 on Ubuntu 14.04. Use the instructions from the `lxd` directory to set up containers and container images.

**lxd**: In this folder you'll find a `Vagrantfile`, instructions, and other files to help work with LXD (pronounced "lex-dee"), a new daemon and CLI for working with LXC-based OS containers.

**openstack-cli**: This folder contains the files for a Vagrant environment that spins up an Ubuntu box with all the OpenStack CLI clients pre-installed.

**photon**: This folder contains the files to spin up an instance of the VMware Photon Technical Preview using Vagrant.

**rkt**: This folder contains the files to work with `rkt`, the CoreOS implementation of the App Container (appc) specification, on Ubuntu 14.04. _THIS ENVIRONMENT IS NOT YET FULLY FUNCTIONAL._

**swarm-consul-openstack**: This folder contains files to create a Consul-backed Docker Swarm cluster on OpenStack. _THIS ENVIRONMENT IS NOT YET FULLY FUNCTIONAL._

**ubuntu-generic**: This folder contains a Vagrant environment for spinning up a generic 64-bit Ubuntu 14.04 VM. _Note there's nothing special going on here---just a plain, generic, Ubuntu VM._

**vagrant-docker**: In this folder you'll find `Vagrantfiles` and supporting documents for using the Docker provider in Vagrant to turn up Docker containers.

**vagrant-docker-yaml**: This folder is an extension of "vagrant-docker" in that it provides the ability to specify the list of Docker containers that Vagrant will create in a separate YAML file.

## License

This content is licensed under the MIT License.
