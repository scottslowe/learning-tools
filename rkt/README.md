# Running Containers with rkt

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) quickly and relatively easily experiment with using `rkt` (pronounced "rock-it"), the CoreOS implementation of the App Container (appc) specification. The configuration was tested using Vagrant 1.7.2, VMware Fusion 6.0.5, and the Vagrant VMware plugin.

**WARNING:** These files are not yet fully functional!

## Contents

* **README.md**: This file you're currently reading.

* **servers.yml**: This YAML file contains a list of VM definitions and associated configuration data. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs. No changes should be necessary to use this file.

* **setup.sh**: This shell script is called by the Vagrant shell provisioner and configures `rkt` inside the VM created by Vagrant. No changes should be needed to this file.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

## Instructions

[NOT YET FUNCTIONAL]

## License

This material is licensed under the MIT License.
