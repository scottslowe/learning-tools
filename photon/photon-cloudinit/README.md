# Customizing Photon with cloud-init

These files provide an example of how to customize VMware Photon ([https://github.com/vmware/photon](https://github.com/vmware/photon)) using cloud-init ([https://launchpad.net/cloud-init](https://launchpad.net/cloud-init)) and the NoCloud datasource when used in conjunction with Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)). This configuration was tested using Vagrant 1.8.1 and 1.8.5, VMware Fusion 8.1.0 (with the Vagrant VMware plugin), and VirtualBox 5.1.

## Contents

* **machines.yml**: This YAML file contains configuration data for Vagrant to use to instantiate VMs. It is referenced by `Vagrantfile` when the user runs `vagrant up`. If you use a Vagrant box _other_ than "vmware/photon", or if you want more memory, more vCPUs, or more than 1 Photon VM created, then you'll need to edit this file. Otherwise, you should be able to use this file unchanged.

* **meta-data**: This file is used by `cloud-init` and should not be modified. Vagrant will put this file in the correct place in the Photon VM(s) via the setup script that is called by `Vagrantfile`.

* **README.md**: This file you're reading right now.

* **setup.sh**: This bash shell script prepares the Photon VM(s) to use the NoCloud datasource with `cloud-init`. The script is designed to be as idempotent as possible, but isn't guaranteed to be completely idempotent. You should not need to edit this file.

* **user-data**: This file is used by `cloud-init` to perform the desired customization to the Photon VM(s). You should not need to edit this file unless you wish to change the customization that is occurring. Otherwise, you can use this file unchanged.

* **Vagrantfile**: This file is used by Vagrant to spin up the VMs. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file in `machines.yml`.

## Instructions

These instructions assume that you've already installed your virtualization provider (VMware Fusion/Workstation or VirtualBox), Vagrant, any necessary plugins (such as the Vagrant VMware plugin), and that they are all working properly. Please refer to the documentation for those products for more information on installation, configuration, or troubleshooting.

1. If you haven't already downloaded the VMware Photon Vagrant box, use `vagrant box add vmware/photon` to download the VMware Photon Vagrant box. You can verify that the box is downloaded and ready for use with the command `vagrant box list`. The same Vagrant box is available for multiple virtualization providers.

2. If you decided to use a different Vagrant box (something different than "vmware/photon"), you'll need to edit `machines.yml` and specify the correct box.

3. Put the files from the `photon-cloudinit` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository via `git clone`, or just download the specific files from the `photon-cloudinit` folder.

4. From a terminal window, change into the directory where you placed the files from step 3 and run `vagrant up`. Vagrant will instantiate one or more Photon VMs according to the information in `machines.yml`.

5. Currently, Photon VMs created by Vagrant won't perform the `cloud-init` customizations until they are rebooted after first being brought up by Vagrant. Use `vagrant ssh` to log into the Photon VM (if you created more than one by adding entries to `machines.yml`, you'll need to add the VM name to the `vagrant ssh` command). Reboot the VM using `sudo reboot -h now`.

6. When the Photon VM comes back up, the Docker daemon should be listening on the official Docker TCP port (2375). If you are running VMware Fusion/Workstation and have a Docker client installed on the host system, you can run `docker -H tcp://W.X.Y.Z:2375 ps`, where "W.X.Y.Z" is the IP address assigned to the Photon VM. The command will return a list of running containers (which is most likely empty). Note that this will _not_ work with VirtualBox.

At this point, you can use the Docker client (either inside the Photon VM or from a system with connectivity to the Photon VM) to pull down images, launch images, etc.

Enjoy!
