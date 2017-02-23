# SSH Bastion Hosts

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) to quickly and easily spin up an environment for learning and working with SSH bastion hosts and proxy commands. This environment was created and tested using VMware Fusion 8.1.0, Vagrant 1.8.6 (with the Vagrant VMware plugin), VirtualBox 5.1, and Ansible 2.1.1.

## Contents

* **bastion\_rsa** and **bastion\_rsa.pub**: Private/public key pair for accessing the SSH bastion host. The password for this key pair is `password` (all lowercase). These files are automatically installed by Vagrant into the correct VMs.

* **config**: This is an SSH configuration file that sets up the SSH bastion host configuration. This file is installed automatically by Vagrant into the client VM, but must be edited to properly reflect the IP address assigned to the bastion host (see the instructions below).

* **machines.yml**: This YAML file contains a list of VM definitions and associated configuration data. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs. Generally, this is the _only_ file that requires any edits; you'll edit this file to specify the correct Vagrant box installed on your system.

* **provision.yml**: This Ansible playbook configures the VMs appropriately with SSH public keys and SSH configuration files. No edits should be necessary to this file.

* **README.md**: This file you're currently reading.

* **remote\_rsa** and **remote\_rsa.pub**: Private/public key pair for accessing the remote SSH nodes behind the bastion host. The password for this key pair is `secure` (all lowercase). The `Vagrantfile` will automatically place these files in the correct locations on the appropriate VMs.

* **ssh-bastion-diagram.png**: This PNG diagram provides a graphical overview of the different VMs in this environment and how they are connected.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

## Instructions

These instructions assume you've already installed your virtualization provider, Vagrant, and any necessary Vagrant plugins. Please refer to the documentation for those products for more information on installation or configuration.

1. Use `vagrant box add` to add a 64-bit Ubuntu 14.04 ("Trusty Tahr") base box to be used by this `Vagrantfile`. Be sure to add a base box for the virtualization provider you will be using. For a VMware-formatted box, the "bento/ubuntu-14.04" box is a good option. For VirtualBox, the "ubuntu/trusty64" box should work well.

2. Edit the `machines.yml` file to ensure the box you downloaded in step 1 is specified in the "box:" line of this file for each VM. Modify the "vmw" line for a VMware-formatted box; modify the "vb" line for a VirtualBox-formatted box. (By default, there are four VMs, so make sure to specify the correct box name for all four VMs.)

3. Run `vagrant up` to bring up the environment. Vagrant will invoke Ansible to run the specified playbook against the VMs for configuration.

4. Run `vagrant ssh client` to access the SSH client VM.

5. In the client VM, load the SSH agent via this command:

        eval `ssh-agent -s`

7. Use `ssh-add` to add the bastion\_rsa and remote\_rsa keys. (The passphrase for `bastion_rsa` is "password"; for `remote_rsa` the passphrase is "secure".)

8. Use `ssh remote1` or `ssh remote2` to establish an SSH session _through_ the bastion host, as specified by the `ProxyCommand` in the SSH configuration file.

Enjoy!
