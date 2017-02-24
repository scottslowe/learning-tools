# Learning Tools: LXC/LXD

Here are found Vagrant environments and other tools/resources for working with and learning more about LXC and LXD.

## Contents

**lxc**: This folder holds a `Vagrantfile` and supporting files to create an environment for working with LXC (pronounced "lex-see"). LXC is a set of tools for working with OS containers. The provisioning is handled via Ansible (pre-2.0) installed on the Vagrant host.

**lxd-ansible**: In this folder you'll find a `Vagrantfile`, instructions, and other files to help work with LXD (pronounced "lex-dee"), a new daemon and CLI for working with LXC-based OS containers. The provisioning is handled via Ansible (pre-2.0) installed on the Vagrant host.

**lxd-ovs**: Use the `Vagrantfile` and other files in this directory to work with LXD and Open vSwitch (OVS) 2.3.1 on Ubuntu 14.04. Use the instructions from the `lxd` directory to set up containers and container images. The provisioning is handled via shell scripts.

**lxd-shell**: In this folder you'll find a `Vagrantfile`, instructions, and other files to help work with LXD (pronounced "lex-dee"), a new daemon and CLI for working with LXC-based OS containers. For this environment, the provisioning is handled via shell scripts.
