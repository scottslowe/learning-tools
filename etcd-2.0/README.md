# Running an etcd 2.0 Cluster on Ubuntu 14.04

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) to quickly and relatively easily spin up a cluster running etcd 2.0 (etcd 2.0.9 was the version specifically tested). The configuration was tested using Vagrant 1.7.2, VMware Fusion 6.0.5, and the Vagrant VMware plugin.

## Contents

* **etcd.conf**: This is an Upstart script (written for Ubuntu) to start etcd. No modifications to this file should be necessary. This file is installed by the provisioning script called in `Vagrantfile`.

* **etcd-01.override, etcd-02.override, etcd-03.override**: These are machine-specific Upstart override files for each node in the etcd cluster. The appropriate node-specific file is installed as `etcd.override` in the `/etc/init` directory on each node by the provisioning script called in `Vagrantfile`. If you make any changes to hostnames or IP addresses in `servers.yml`, these files must also be modified so that the hostnames and IP addresses match.

* **provision.sh**: This provisioning script is called by the shell provisioner in `Vagrantfile`. It downloads the etcd 2.0.9 release from GitHub, expands it, creates necessary directories, and places files in the appropriate locations.

* **README.md**: This file you're currently reading.

* **servers.yml**: This YAML file contains a list of VM definitions. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs. You may need to edit this file to provide appropriate IP addresses and other VM configuration data (see "Instructions" below). _If you change the hostnames or IP addresses provided in this file, you **must** also edit the Upstart override files._

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

## Instructions

These instructions assume you've already installed VMware Fusion, Vagrant, and the Vagrant VMware plugin. Please refer to the documentation for those products for more information on installation or configuration.

1. Use `vagrant box add` to install an Ubuntu 14.04 x64 box for the "vmware_fusion" provider. (In theory you should be able to use this Vagrant environment with VMware Workstation as well, but only VMware Fusion was tested.) I have a base box you can use for this purpose; to use my Ubuntu 14.04 x64 base box, add the box with `vagrant box add slowe/ubuntu-trusty-x64`.

2. Place the files from the `etcd-2.0` directory of this GitHub repository (the "lowescott/learning-tools" repository) into a directory on your system. You can clone the entire "learning-tools" repository (using `git clone`), or just download the specific files from the `etcd-2.0` directory.

3. Edit `servers.yml` to ensure that the box specified in that file matches the Ubuntu 14.04 x64 base box you just installed and will be using. _I recommend that you do not change any other values in this file unless you know it is necessary._

4. If you changed the hostnames or IP addresses provided in `servers.yml`, you **must** edit the Upstart override files (`etcd-01.override`, `etcd-02.override`, and `etcd-03.override`). The hostnames and IP addresses specified in these files are based on the values provided in `servers.yml`. If you changed the values in `servers.yml`, change the values in these files to match.

5. From a terminal window, change into the directory where the files from this directory are stored and run `vagrant up` to bring up the VMs specified in `servers.yml` and `Vagrantfile`.

6. Once Vagrant has finished creating, booting, and provisioning each of the VMs, log into the first system ("etcd-01" by default) using `vagrant ssh etcd-01`. Once you are logged into this VM, start etcd with this command:

		sudo initctl start etcd

7. Repeat step 6 with the remaining 2 VMs ("etcd-02" and "etcd-03", by default).

8. Once etcd is running on all 3 VMs, you can test etcd with this command:

		etcdctl member list
	
	This should return a list of three nodes as members of the etcd cluster. If you receive an error or don't see all three VMs listed, tear down the Vagrant environment with `vagrant destroy` and recreate the environment from scratch. If you continue to experience problems, open an issue here on GitHub (or file a pull request fixing the problem).

Enjoy!
