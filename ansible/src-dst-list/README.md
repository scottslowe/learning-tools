# Using Complex Lists with Ansible Tasks

These files provide an example of how to use "complex" (multi-value) lists in an Ansible task using the "with_items" construct.

## Contents

* **docker.socket**: This is a systemd unit file defining a socket for the Docker daemon.

* **docker-socket.conf**: This is a systemd drop-in unit to modify the default Docker unit file.

* **docker-tcp.socket**: This systemd unit file defines a TCP socket for the Docker daemon.

* **machines.yml**: This YAML data file is used by Vagrant to determine which VM images to use, how many VMs to create, and what the configuration of those VMs should be.

* **provision.yml**: This Ansible playbook shows the use of the "with_items" construct when items in the list have multiple values/attributes.

* **README.md**: The file you're currently reading.

* **Vagrantfile**: This file is used by Vagrant to spin up VMs. No changes should be needed to this file; all configuration information is provided in `machines.yml`.

## Instructions

These instructions assume that you have both Vagrant and Ansible installed and functioning correctly on your system, and that the virtualization provider used by Vagrant is working as expected. These instructions also assume that you've installed any necessary Vagrant plugins to support the installed virtualization provider.

1. Place the files from the `ansible/src-dst-list` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`) or just download the specific files from the `ansible/src-dst-list` folder.

2. Install a Vagrant box for CentOS Atomic Host. The `machines.yml` file contains a suggested box for VirtualBox. _This environment does not support other virtualization providers at this time._

3. Run `vagrant up` to spin up the Vagrant VM. Vagrant will automatically invoke Ansible to copy the files into the correct location. This illustrates how Ansible's "with_items" construct can leverage multiple values in a list.

4. Run `vagrant destroy` to tear down the environment.

Enjoy!

## License

This content is licensed under the MIT License.
