# Running an etcd-Backed Swarm Cluster on Photon

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) to quickly and relatively easily spin up a Docker Swarm cluster on VMware Photon, with discovery services provided by etcd 2.0 on Ubuntu. The configuration was tested using Vagrant 1.7.4 and Oracle VirtualBox 5.0.0.

## Contents

* **etcd.conf**: This is an Upstart script (written for Ubuntu) to start etcd. No modifications to this file should be necessary. This file is installed by the provisioning script called in `Vagrantfile`.

* **etcd.defaults.erb**: A template used by Vagrant to create machine-specific service defaults files for each node in the etcd cluster. The appropriate node-specific file is installed as `etcd` in the `/etc/default` directory on each node by the provisioning script called in `Vagrantfile`.

* **provision.sh**: This provisioning script is called by the shell provisioner in `Vagrantfile`. It downloads the etcd 2.0.9 release from GitHub, expands it, creates necessary directories, and places files in the appropriate locations.  Finally it starts etcd ( or restarts it if already running ).

* **README.md**: This file you're currently reading.

* **servers.yml**: This YAML file contains a list of VM definitions. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs. You may need to edit this file to provide appropriate IP addresses and other VM configuration data (see "Instructions" below). _If you change the hostnames or IP addresses provided in this file, you **must** also edit the Upstart override files._

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

## Instructions

These instructions assume you've already installed VirtualBox and Vagrant. Please refer to the documentation for those products for more information on installation or configuration.

1. Use `vagrant box add` to install an Ubuntu 14.04 x64 box for the "virtualbox" provider. (In theory you should be able to use this Vagrant environment with VMware Workstation and VMware Fusion as well, with minor changes to the `Vagrantfile`.) If you don't already have a Vagrant box for Photon, use `vagrant box add` to download a Photon box as well.

2. Place the files from the `swarm-etcd2-photon` directory of this GitHub repository (the "lowescott/learning-tools" repository) into a directory on your system. You can clone the entire "learning-tools" repository (using `git clone`), or just download the specific files from the `swarm-etcd2-photon` directory.

3. Edit `servers.yml` to ensure that the box specified in that file matches the Ubuntu 14.04 x64 base box you just installed and will be using. _I recommend that you do not change any other values in this file unless you know it is necessary._

4. From a terminal window, change into the directory where the files from this directory are stored and run `vagrant up` to bring up the VMs specified in `servers.yml` and `Vagrantfile`.

5. Once Vagrant has finished creating, booting, and provisioning each of the VMs and starting etcd, log into the first system ("etcd-01" by default) using `vagrant ssh etcd-01`.

6. You can test etcd with this command:

		etcdctl member list
	
	This should return a list of three nodes as members of the etcd cluster. If you receive an error or don't see all three VMs listed, tear down the Vagrant environment with `vagrant destroy` and recreate the environment from scratch. If you continue to experience problems, open an issue here on GitHub (or file a pull request fixing the problem).

Enjoy!
