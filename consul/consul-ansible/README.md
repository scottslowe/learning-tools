# Running a Consul Cluster in Vagrant (with Ansible)

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) and Ansible ([http://www.ansible.com](http://www.ansible.com)) to quickly and relatively easily spin up a three-node Consul ([http://www.consul.io](http://www.consul.io)) cluster. The configuration was tested using Vagrant, VMware Fusion with the Vagrant VMware plugin, Libvirt, and Ansible. VirtualBox will probably work fine but hasn't been explicitly tested.

## Contents

* **ansible.cfg**: This Ansible configuration file tells Ansible which inventory file to use (the default Vagrant-generated inventory), which SSH private key to use (the default Vagrant insecure key), and provides other configuration values. No edits to this file should be necessary.

* **config.json.j2**: This Jinja2 template is automatically filled in with appropriate values by Ansible when the Ansible playbook is applied against the Vagrant VMs. If the Vagrant box in use does not use `ethX` to refer to network interfaces, this file will need to be edit to use the correct network interface name(s). No edits to this file should be necessary.

* **consul.service.j2**: This Jinja2 template is used by Ansible to create a systemd unit for Consul. If the Vagrant box in use does not use `ethX` to refer to network interfaces, this file will need to be edit to use the correct network interface name(s). Otherwise, no edits to this file should be necessary.

* **machines.yml**: This YAML file contains a list of VM definitions. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs. Generally, the only change needed to this file is to specify the correct Vagrant box you will be used (see "Instructions" below). If necessary, you may need to edit the IP addresses supplied in this file to avoid IP addressing conflicts with other networks.

* **provision.yml**: This is the Ansible playbook that will configure the Vagrant VMs to become members of a Consul cluster. No edits to this file should be necessary.

* **README.md**: This file you're currently reading.

* **setup.yml**: This simple Ansible playbook installs Python 2 via the `raw` module. No edits to this file should be necessary.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

## Instructions

These instructions assume you've already installed Vagrant, your virtualization provider (and any associated plugins needed to use it with Vagrant), and Ansible. Please refer to the documentation for those products for more information on installation or configuration.

1. Use `vagrant box add` to install an Ubuntu 16.04 x64 box for your virtualization provider. The `machines.yml` file contains some suggested boxes for various providers.

2. Place the files from the `consul-ansible` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`) or just download the specific files from the the `consul-ansible` folder.

3. If you are using a Vagrant box other than one of the ones listed in `machines.yml`, you'll need to edit `machines.yml` file to specify the box in use. If necessary to avoid IP address conflicts with existing networks, you may also need to edit the IP addresses specified in this file. Generally, no other changes are needed.

4. Once you have edited `machines.yml`, use `vagrant up` to bring up the 3 systems that will serve as your Consul cluster.

5. Once Vagrant has finished bringing up the VMs, run `ansible-playbook ansible.yml`. Ansible will provision and configure Consul on each of the Vagrant VMs.

    **NOTE**: Due to the way the Ansible provisioner in Vagrant works, it's currently not possible to provision the VMs with Ansible from within the `Vagrantfile`. As a result, step #5 (running `ansible-playbook` manually) is needed.

At this point, you have a functional Consul cluster running under Vagrant. You can use `vagrant ssh <vm-name>` to connect to one of the VMs and run the following command to demonstrate that Consul is working:

	consul members -rpc-addr=192.168.1.101:8400

Enjoy!
