# Learning Tools

This repository contains a variety of files and tools for learning new technologies. It is aimed at existing IT professionals who need some help coming up to speed with new technologies, products, or open source projects.

## Contents

**ansible-aws**: This set of files shows how to use Ansible to both provision infrastructure on AWS as well as how to decommission (tear down) that same infrastructure.

**complex-yaml**: This folder contains files that illustrate how to use "complex" YAML files in a `Vagrantfile`.

**consul-ansible**: In this folder you'll find a `Vagrantfile` and supporting documents to run a Consul cluster under Vagrant on your local laptop. The provisioning is handled via Ansible (pre-2.0) installed on the Vagrant host.

**consul**: In this folder you'll find a `Vagrantfile` and supporting documents to run a Consul cluster under Vagrant on your local laptop. The  provisioning is handled via shell scripts.

**containerd-runc**: _This is a placeholder for future work._

**coreos-cloudinit-toolbox**: This folder contains files for using cloud-init to customize the CoreOS toolbox. This learning environment can be used with local Vagrant VMs, with OpenStack instances, or with AWS instances.

**debian-generic**: This folder has a Vagrant environment for spinning up a generic, 64-bit Debian 8.0 ("Jessie") VM. Note there's nothing special here---just a plain, generic Debian VM. No custom provisioning is provided in this environment.

**docker-ipvlan-l3**: This folder has files to create a learning environment for using ipvlan L3 interfaces with Docker containers.

**docker-ipvlan**: This folder has files to create a learning environment for using ipvlan interfaces with Docker containers.

**docker-macvlan**: This folder has files to create a learning environment for using macvlan interfaces with Docker containers.

**docker-swarm-etcd**: In this folder is a `Vagrantfile` and supporting files for turning up an etcd 2.0-backed Docker Swarm cluster. The provisioning is handled via shell scripts.

**docker-swarm-ha**: This folder holds a `Vagrantfile`, supporting files, and instructions for turning up a Consul-backed Docker Swarm cluster with multiple Swarm managers for high availability. The provisioning is handled via shell scripts.

**docker-swarm**: This folder contains a `Vagrantfile` and supporting files to run a Consul-backed Docker Swarm cluster under Vagrant on your local system. The provisioning is handled via shell scripts.

**etcd-2.0**: Use the `Vagrantfile` and other files in this directory to turn up an etcd 2.0.9 cluster running on Ubuntu 14.04. The provisioning is handled via shell scripts.

**ipvs-docker**: Use this learning environment to experiment with using IPVS/LVS for load balancing Docker containers.

**junos**: _This is a placeholder for future work._

**kvm-macvtap**: This learning environment has a `Vagrantfile` and supporting files for working with KVM and macvtap interfaces on Ubuntu 14.04. The provisioning is handled via Ansible (pre-2.0) installed on the Vagrant host.

**kvm**: The files in this folder allow you to create a learning environment for working with KVM and Libvirt on Ubuntu 14.04. The provisioning is handled via Ansible (pre-2.0) installed on the Vagrant host.

**lxc**: This folder holds a `Vagrantfile` and supporting files to create an environment for working with LXC (pronounced "lex-see"). LXC is a set of tools for working with OS containers. The provisioning is handled via Ansible (pre-2.0) installed on the Vagrant host.

**lxd-ansible**: In this folder you'll find a `Vagrantfile`, instructions, and other files to help work with LXD (pronounced "lex-dee"), a new daemon and CLI for working with LXC-based OS containers. The provisioning is handled via Ansible (pre-2.0) installed on the Vagrant host.

**lxd-ovs**: Use the `Vagrantfile` and other files in this directory to work with LXD and Open vSwitch (OVS) 2.3.1 on Ubuntu 14.04. Use the instructions from the `lxd` directory to set up containers and container images. The provisioning is handled via shell scripts.

**lxd-shell**: In this folder you'll find a `Vagrantfile`, instructions, and other files to help work with LXD (pronounced "lex-dee"), a new daemon and CLI for working with LXC-based OS containers. For this environment, the provisioning is handled via shell scripts.

**multi-provider-simple**: The `Vagrantfile` and supporting documents in this folder show how to build Vagrant environments to support multiple back-end providers.

**openstack-cli**: This folder contains the files for a Vagrant environment that spins up an Ubuntu box with all the OpenStack CLI clients pre-installed. The provisioning is handled via shell scripts.

**ovn**: _This is a placeholder for future work._

**ovn-docker-ansible**: _This is a placeholder for future work._

**ovs-cl-vtep**: _This is a placeholder for future work._

**ovs-geneve**: This folder contains a Vagrant environment and supporting files for compiling Open vSwitch (OVS) from source and using Geneve tunneling to connect network namespaces on two different VMs.

**ovs-multi-br**: In this folder is a Vagrant environment, Ansible playbook, and related data files for experimenting with multiple Open vSwitch (OVS) bridges.

**photon-ansible**: In this folder is a Vagrant environment for using Ansible with VMware Photon; specifically, for using Ansible to configure Photon's pre-installed Docker daemon to listen over a network socket. Naturally, the provisioning is handled via Ansible (pre-2.0) installed on the Vagrant host.

**photon-cloudinit**: This folder contains the files to spin up VMs, using Vagrant and customized via `cloud-init`, running the VMware Photon Technical Preview. The provisioning is handled via shell scripts.

**photon**: This folder contains the files to spin up an instance of the VMware Photon Technical Preview using Vagrant. No custom provisioning is provided in this environment.

**README.md**: This document that you're currently reading.

**rkt**: This folder contains the files to work with `rkt` (version 1.4.0), the CoreOS implementation of the App Container (appc) specification, on Ubuntu 14.04. Provisioning is handled by Vagrant's file and shell provisioners.

**ssh-bastion**: In this folder is a Vagrant environment, leveraging multiple VMs, that allow you to work with SSH multiplexing via an SSH bastion host. The provisioning is handled by Vagrant's file and shell provisioners.

**ssh-bastion-ansible**: In this folder is a Vagrant environment, leveraging multiple VMs, that allow you to work with SSH multiplexing via an SSH bastion host. The provisioning is handled by Ansible 2.x.

**swarm-consul-openstack**: This folder contains files to create a Consul-backed Docker Swarm cluster on OpenStack. _THIS ENVIRONMENT IS NOT YET FULLY FUNCTIONAL._

**swarm-etcd2-photon:** This folder contains a Vagrant environment and associated support files to turn up an etcd-backed Docker Swarm cluster where the Docker Engine instances are running on VMware Photon. Provisioning is handled by Ansible 2.x.

**terraform:** This directory provides an example Terraform configuration (in both Terraform format as well as JSON format) designed to work with OpenStack.

**terraform-aws**: This directory contains a number of different examples of using Terraform with AWS. Refer to the `README.md` in this folder for more details.

**ubuntu-generic**: This folder contains a Vagrant environment for spinning up a generic 64-bit Ubuntu 14.04 VM. Note there's nothing special going on here---just a plain, generic, Ubuntu VM. No custom provisioning is provided in this environment.

**vagrant-aws**: This folder contains a `Vagrantfile` and supporting files for using Vagrant to spin up an instance on AWS. This allows you to use Vagrant to spin up and destroy an instance on AWS.

**vagrant-aws-multi**: Like "vagrant-aws", but for spinning up multiple instances on AWS.

**vagrant-docker-json**: Like `vagrant-docker-yaml`, this folder is an extension of "vagrant-docker" in that it provides the ability to specify, in an external JSON file, a list of Docker containers to be instantiated by Vagrant. The provisioning is handled by Vagrant's built-in Docker provisioner.

**vagrant-docker-yaml**: This folder is an extension of "vagrant-docker" in that it provides the ability to specify the list of Docker containers that Vagrant will create in a separate YAML file. The provisioning is handled by Vagrant's built-in Docker provisioner.

**vagrant-docker**: In this folder you'll find `Vagrantfiles` and supporting documents for using the Docker provider in Vagrant to turn up Docker containers. The provisioning is handled by Vagrant's built-in Docker provisioner.

**vagrant-json**: The files in this folder illustrate the use of an external JSON data file to drive Vagrant.

**vagrant-multi-platform**: This Vagrant environment demonstrates one possible way to create a `Vagrantfile` that supports multiple virtualization platforms without any edits needed across platforms.

**vagrant-openstack-multi**: This folder is similar to `vagrant-openstack`, but supports Vagrant operations on multiple instances.

**vagrant-openstack**: This folder contains a `Vagrantfile` and supporting files for using Vagrant with the OpenStack provider. This allows you to use Vagrant to provision and destroy an instance in an OpenStack cloud.

## Other Useful Projects

Here are some other projects that you may also find useful:

**Kubernetes-CoreOS-Fleet-Flannel**: https://github.com/kelseyhightower/kubernetes-fleet-tutorial

**coreos-kubernetes-digitalocean**: https://github.com/bketelsen/coreos-kubernetes-digitalocean

**Docker-CoreOS-Fleet-Flannel-Etcd-Confd-Nginx**: https://github.com/willrstern/production-docker-ha-architecture

If there are additional projects you feel should be added to this list, please submit a pull request. Thanks!

## License

This content is licensed under the MIT License.
