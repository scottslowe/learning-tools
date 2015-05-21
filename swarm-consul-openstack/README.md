# Running a Docker Swarm Cluster on OpenStack

This repository contains files intended to help users run their own Consul-backed Docker Swarm cluster on OpenStack. This solution leverages Docker, Docker Swarm, Consul, CoreOS, and OpenStack (including OpenStack Heat and the Docker plugin for Heat). _A functional OpenStack installation is required to use these files._

**WARNING:** These files are not yet fully functional!

## Contents

* **consul-cluster.yml**: An OpenStack Heat template to spin up a cluster of Ubuntu 14.04 instances that will run Consul. You'll need to edit this file to provide correct values for an Ubuntu 14.04 image in Glance, the Neutron network to which the instances should be attached, and the Neutron security group that should be applied to these instances. All these values can be obtained using the `glance` and `neutron` CLI clients.

* **coreos-cluster.yml**: An OpenStack Heat template to spin up a cluster of CoreOS instances running etcd. You'll need to edit this file to provide the values for the correct CoreOS image in Glance, the Neutron security group to be applied to the Neutron ports, and the Neutron network to which to connect these ports (and therefore the instances). All these values are easily obtained using the `neutron` and `glance` CLI clients.

* **docker-swarm.yml**: An OpenStack Heat template that deploys the necessary Docker Swarm containers onto already-deployed CoreOS instances that are running etcd. You'll need to edit this file to supply the IP addresses of the CoreOS instances.

* **README.md**: The file you're reading right now.

## Instructions

[NOT YET FUNCTIONAL]

## License

This material is licensed under the MIT License.
