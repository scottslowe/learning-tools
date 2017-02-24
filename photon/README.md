# Learning Tools: VMware Photon OS

This folder contains tools, resources, and environments designed around VMware's Photon OS Linux distribution.

## Contents

**photon-ansible**: In this folder is a Vagrant environment for using Ansible with VMware Photon; specifically, for using Ansible to configure Photon's pre-installed Docker daemon to listen over a network socket. Naturally, the provisioning is handled via Ansible (pre-2.0) installed on the Vagrant host.

**photon-cloudinit**: This folder contains the files to spin up VMs, using Vagrant and customized via `cloud-init`, running the VMware Photon Technical Preview. The provisioning is handled via shell scripts.

**photon-generic**: This folder contains the files to spin up an instance of the VMware Photon Technical Preview using Vagrant. No custom provisioning is provided in this environment.
