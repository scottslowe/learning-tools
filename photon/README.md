# Simple VMware Photon Environment

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) to quickly and relatively easily spin up an instance of VMware Photon (container-optimized Linux distribution). The configuration was tested using Vagrant 1.7.2, VMware Fusion 6.0.5, and the Vagrant VMware plugin.

## Contents

* **README.md**: This file you're currently reading.

* **servers.yml**: This YAML file contains a list of VM definitions and associated configuration data. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

## Instructions

These instructions assume you've already installed VMware Fusion, Vagrant, and the Vagrant VMware plugin. Please refer to the documentation for those products for more information on installation or configuration.

1. At the time of writing, Vagrant's guest OS support did not work as expected with Photon. To fix this issue, install a Photon-specific Vagrant plugin using this command:

		vagrant plugin install vagrant-guests-photon

2. Optionally, go ahead and download the Photon box for Vagrant using the command `vagrant box add vmware/photon`. You can skip this step, if you'd like; Vagrant will then automatically download the box the first time you run `vagrant up`.

3. Place the files from the `photon` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`), download a ZIP file of the entire "learning-tools" repository, or just download the specific files from the the `photon` folder.

4. From a terminal window, change into the directory where you stored the files from step 3 and run `vagrant up`.

5. Once Vagrant has finished bringing up the VM, simply use `vagrant ssh` to log into the system. Both `rkt` (version 0.5.1) and `docker` (version 1.5.0) are pre-installed and ready for use.

Enjoy!
