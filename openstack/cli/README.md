# Working with the OpenStack CLI

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) to quickly and relatively easily spin up a command-line interface (CLI) client to an existing OpenStack installation. _A separate OpenStack installation is required._ The configuration was tested using Vagrant 1.7.2, VMware Fusion 6.0.5, and the Vagrant VMware plugin.

## Contents

* **adminrc**: This file contains authentication credentials for the OpenStack CLI clients.

* **config.sh**: This shell script performs the final configuration tasks on the Vagrant box to install the OpenStack CLI clients. It is called by the Vagrant file provisioner in `Vagrantfile`.

* **README.md**: This file you're currently reading.

* **servers.yml**: This YAML file contains a list of VM definitions and associated configuration data. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

## Instructions

These instructions assume you've already installed VMware Fusion, Vagrant, and the Vagrant VMware plugin. Please refer to the documentation for those products for more information on installation or configuration.

**NOTE:** A separate OpenStack installation is required; this Vagrant environment does not set up an OpenStack installation.

1. Edit the `adminrc` file to provide the specific details to authenticate against your existing OpenStack installation. Specifically, you'll need the URL for authenticating against Keystone (typically looks something like `http://192.168.1.100:5000/v2.0`), the project/tenant against which the commands should run, and the username and password for a user with permissions in that project/tenant.

2. Use `vagrant box add` to add an Ubuntu 14.04 base box to be used by this `Vagrantfile`. You can use my base box (`slowe/ubuntu-trusty-x64`), which provides support for the `vmware_desktop` provider (it should work with either VMware Workstation or VMware Fusion).

3. If you are using a box other than my Ubuntu 14.04 base box, or if you want to change the default amount of RAM or number of vCPUs assigned to the VM, edit the `servers.yml` file and make the desired changes.

4. Run `vagrant up`, and when the VM is done provisioning (note that Internet access is required) use `vagrant ssh` to enjoy CLI access to the configured OpenStack installation.

Enjoy!
