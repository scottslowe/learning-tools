# Learning Tools: Docker

Here you'll find a collection of tools and resources for learning about (or experimenting with) Docker and Docker containers.

## Contents

**containerd-runc**: _This is a placeholder for future work._

**ipvlan-l2**: This folder has files to create a learning environment for using ipvlan interfaces with Docker containers.

**ipvlan-l3**: This folder has files to create a learning environment for using ipvlan L3 interfaces with Docker containers.

**ipvs**: Use this learning environment to experiment with using IPVS/LVS for load balancing Docker containers.

**macvlan**: This folder has files to create a learning environment for using macvlan interfaces with Docker containers.

**swarm-consul**: This folder contains a `Vagrantfile` and supporting files to run a Consul-backed Docker Swarm cluster under Vagrant on your local system. The provisioning is handled via shell scripts.

**swarm-etcd**: In this folder is a `Vagrantfile` and supporting files for turning up an etcd 2.0-backed Docker Swarm cluster. The provisioning is handled via shell scripts.

**swarm-etcd2-photon:** This folder contains a Vagrant environment and associated support files to turn up an etcd-backed Docker Swarm cluster where the Docker Engine instances are running on VMware Photon. Provisioning is handled by Ansible 2.x.

**swarm-ha**: This folder holds a `Vagrantfile`, supporting files, and instructions for turning up a Consul-backed Docker Swarm cluster with multiple Swarm managers for high availability. The provisioning is handled via shell scripts.
