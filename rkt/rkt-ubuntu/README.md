# Running Containers with rkt

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) quickly and relatively easily experiment with using `rkt` (pronounced "rock-it"), the CoreOS implementation of the App Container (appc) specification. The configuration was tested using Vagrant 1.8.1, VMware Fusion 8.1.0, and the Vagrant VMware plugin.

## Contents

* **machines.yml**: This YAML file contains a list of VM definitions and associated configuration data. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs. You may need to edit this file to ensure that the correct Vagrant box is listed.

* **provision.sh**: This shell script is called by the Vagrant shell provisioner and configures `rkt` inside the VM created by Vagrant. No changes should be needed to this file.

* **README.md**: This file you're currently reading.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

## Instructions

These instructions assume you've already installed Vagrant, your virtualization provider, and any necessary Vagrant plugins. Please refer to the documentation for those products for more information on installation or configuration.

1. Use `vagrant box add` to install an Ubuntu 14.04 x64 box. For a VMware-formatted box, the "bento/ubuntu-14.04" box is one good option. For VirtualBox, you can use the "ubuntu/trusty64" box.

2. Place the files from the `rkt` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`) or just download the specific files from the the `rkt` folder.

3. Verify that the Vagrant box you added in step #1 is the box listed in `machines.yml`. Note there are separate lines for a VMware Fusion-formatted box (on the "vmw_box" line) and a VirtualBox-formatted box (on the "vb_box" line).

4. If you download the `rkt-v1.20.0.tar.gz` release from GitHub (see [https://github.com/coreos/rkt/releases](https://github.com/coreos/rkt/releases)) and place it in the same directory with the `Vagrantfile` and other files, then the Vagrant provisioning script won't download it from the Internet and will use the local copy instead.

5. Use `vagrant up` to instantiate the learning environment.

6. Use `vagrant ssh` to log into the VM created by Vagrant.

7. Use this command to launch a simple rkt container (follow the prompts as necessary):

        sudo rkt run --interactive quay.io/coreos/alpine-sh

Enjoy!

## License

This material is licensed under the MIT License.
