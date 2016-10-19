# Configuring Docker on Photon OS Using Ansible

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) to quickly and relatively easily spin up an instance of VMware Photon (container-optimized Linux distribution) and configure that instance of Photon using Ansible. The configuration was tested using Vagrant 1.8.1 and 1.8.5, VMware Fusion 8.1.0 (with the Vagrant VMware plugin), VirtualBox 5.1, and Ansible 2.1.

## Contents

* **ansible.cfg**: This Ansible configuration file tells Ansible to use the automatically-generated inventory created by Vagrant and to use Vagrant's default username and insecure SSH key. This facilitates the use of Ansible to modify the Vagrant environment outside the use of `vagrant provision`. No edits to this file should be necessary.

* **docker-tcp.socket**: This systemd unit configures a TCP socket for the Docker daemon. This unit file is provisioned into the Photon OS instance by Ansible.

* **docker.service**: This systemd unit configures the Docker daemon to listen on a TCP socket as well as a local UNIX socket. It is provisioned by Ansible.

* **docker.socket**: This systemd unit configures a local UNIX socket for use by the Docker daemon. Ansible provisions this file into the Photon OS instance.

* **machines.yml**: This YAML file contains a list of VM definitions and associated configuration data. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs.

* **README.md**: This file you're currently reading.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

## Instructions

These instructions assume you've already installed your virtualization provider (VMware Fusion/Workstation or VirtualBox), Vagrant and any necessary plugins (such as the Vagrant VMware plugin), and Ansible. Please refer to the documentation for those products for more information on installation or configuration.

1. Depending on your version of Vagrant, you may need to install a Photon-specific Vagrant plugin. If this is necessary, you can do so with the following command:

		vagrant plugin install vagrant-guests-photon

    As of this writing, it was unclear which version(s) of Vagrant required this plugin.

2. Optionally, go ahead and download the Photon box for Vagrant using the command `vagrant box add vmware/photon`. You can skip this step, if you'd like; Vagrant will then automatically download the box the first time you run `vagrant up`. The same Vagrant box is formatted for multiple virtualization providers; if you're downloading it manually and are prompted for the provider, select the appropriate provider.

3. Place the files from the `photon-ansible` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`), download a ZIP file of the entire "learning-tools" repository, or just download the specific files from the the `photon-ansible` folder.

4. From a terminal window, change into the directory where you stored the files from step 3 and run `vagrant up`.

5. Once Vagrant has finished bringing up the VM and the Ansible playbook has completed running, simply use `vagrant ssh` to log into the system. Once logged into the system, you should note that the pre-installed Docker daemon has been reconfigured to listen on both a local UNIX socket as well as a TCP network socket.

Enjoy!
