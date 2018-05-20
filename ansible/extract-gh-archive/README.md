# Extracting and "Installing" Something from GitHub

These files provide an example of how to download, extract, and "install" a binary release of an open source project from GitHub. As an example, the playbook shows how to do this for `ksonnet`, a Kubernetes-related open source project led by Heptio.

## Contents

* **machines.yml**: This YAML data file is used by Vagrant to determine which VM images to use, how many VMs to create, and what the configuration of those VMs should be.

* **provision.yml**: This Ansible playbook shows creating a temporary directory, registering that temporary directory for later use, then downloading, extracting, and "installing" a binary release.

* **README.md**: The file you're currently reading.

* **Vagrantfile**: This file is used by Vagrant to spin up VMs. No changes should be needed to this file; all configuration information is provided in `machines.yml`.

## Instructions

These instructions assume that you have both Vagrant and Ansible installed and functioning correctly on your system, and that the virtualization provider used by Vagrant is working as expected. These instructions also assume that you've installed any necessary Vagrant plugins to support the installed virtualization provider.

1. Place the files from the `ansible/extract-gh-archive` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`) or just download the specific files from the `ansible/extract-gh-archive` folder.

2. Install a Vagrant box for CentOS 7. The `machines.yml` file contains suggested boxes for VirtualBox, VMware, and Libvirt.

3. Run `vagrant up` to spin up the Vagrant VM. Vagrant will automatically invoke Ansible. Ansible will create a temporary directory, download the latest (as of this writing) `ksonnet` release archive into that directory, extract it, and copy out the `ks` binary to a versioned copy.

4. Use `vagrant ssh` to log into the Vagrant VM and see that `ks` is installed and in the path. Press Ctrl-D to log out when you're finished.

5. Run `vagrant destroy` to tear down the environment.

Enjoy!

## License

This content is licensed under the MIT License.
