# Running LXC-Based OS Containers with LXD

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) quickly and relatively easily experiment with LXD (pronounced "lex-dee"), a new daemon and CLI client for working with LXC-based OS containers. The configuration was tested using Vagrant 1.8.x, VMware Fusion 8.1.0, and the Vagrant VMware plugin.

## Contents

* **README.md**: This file you're currently reading.

* **machines.yml**: This YAML file contains a list of VM definitions and associated configuration data. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs. No changes should be necessary to use this file.

* **setup.sh**: This shell script is called by the Vagrant shell provisioner and configures `rkt` inside the VM created by Vagrant. No changes should be needed to this file.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

## Instructions

These instructions assume you've already installed VMware Fusion, Vagrant, and the Vagrant VMware plugin. Please refer to the documentation for those products for more information on installation or configuration.

1. Use `vagrant box add` to install an Ubuntu 14.04 x64 box for the "vmware_fusion" provider. The "bento/ubuntu-14.04" box is a good option for a VMware-formatted box. (In theory you should be able to use this Vagrant environment with VMware Workstation as well, but only VMware Fusion was tested.)

2. Place the files from the `lxd-shell` directory of this GitHub repository (the "lowescott/learning-tools" repository) into a directory on your system. You can clone the entire "learning-tools" repository (using `git clone`), or just download the specific files from the `lxd-shell` directory.

3. Edit `machines.yml` to ensure that the box specified in that file matches the Ubuntu 14.04 x64 base box you just installed and will be using. _I recommend that you do not change any other values in this file unless you know it is necessary._

4. From a terminal window, change into the directory where the files from this directory are stored and run `vagrant up` to bring up the VMs specified in `machines.yml` and `Vagrantfile`. (By default, it will create and power on only a single VM.)

5. Once Vagrant has finished creating, booting, and provisioning the VM (note you'll need Internet access for this step), log into the VM (named "lxd-01" by default) using `vagrant ssh`.

6. Add the public LinuxContainers.org image repository by running `lxc remote add lxc-org images.linuxcontainers.org`.

7. Copy the 32-bit Ubuntu 14.04 container image to your system with the command `lxc image copy lxc-org:/ubuntu/trusty/i386 local: --alias=trusty32`.

8. Launch a container based on this image with the command `lxc launch trusty32 lxd-test-01`. This will start a container named "lxd-test-01" based on the "trusty32" image (which is an alias for the image you copied in step 7).

9. Run `file /bin/ls` and note the output. (You'll use the output for comparison in just a moment.)

10. Open a shell in the 32-bit container you launched in step 8 with the command `lxc exec lxd-test-01 /bin/bash`.

11. Inside the container, run `file /bin/ls` and compare the output to the output of the same command you ran outside the container. You'll see that inside the container the file is reported as a 32-bit ELF executable; outside the container the same file is listed as a 64-bit ELF executable.

12. Press Ctrl-D to exit the shell in the container.

13. The container is still running, so stop the container with `lxc stop lxd-test-01`.

There you have it---a way to quickly and easily experiment with LXD. Enjoy!
