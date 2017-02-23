# Running a Consul Cluster in Vagrant (with Ansible)

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) and Ansible ([http://www.ansible.com](http://www.ansible.com)) to quickly and relatively easily spin up a three-node Consul ([http://www.consul.io](http://www.consul.io)) cluster. The configuration was tested using Vagrant 1.8.1, VMware Fusion 8.1.0, the Vagrant VMware plugin, and Ansible 1.9.1. Other versions of these components may work, but have not been tested.

## Contents

* **ansible.cfg**: This Ansible configuration file tells Ansible which inventory file to use (a file named `hosts` in the current directory), which SSH private key to use (the default Vagrant insecure key), and which remote user to use (the default user of `vagrant` that should be common to all Vagrant boxes). No edits to this file should be necessary.

* **config.json.j2**: This Jinja2 template is automatically filled in with appropriate values by Ansible when the Ansible playbook is applied against the Vagrant VMs. No edits to this file should be necessary.

* **consul.conf**: This Upstart script configures Consul to run as a background daemon (service) on the Ubuntu-based VMs created by Vagrant. This file is installed by Ansible when the Ansible playbook is applied against the Vagrant VMs. No edits to this file should be necessary.

* **hosts**: This Ansible inventory file is generated automatically by Vagrant once you run `vagrant status` or any other command that requires Vagrant to parse the `Vagrantfile`. Since it is automatically generated, no edits directly to this file are needed (they would be overwritten anyway).

* **machines.yml**: This YAML file contains a list of VM definitions. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs. Generally, the only change needed to this file is to specify the correct Vagrant box you will be used (see "Instructions" below). If necessary, you may need to edit the IP addresses supplied in this file to avoid IP addressing conflicts with other networks.

* **provision.yml**: This is the Ansible playbook that will configure the Vagrant VMs to become members of a Consul cluster. No edits to this file should be necessary.

* **README.md**: This file you're currently reading.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

## Instructions

These instructions assume you've already installed VMware Fusion, Vagrant, the Vagrant VMware plugin, and Ansible. Please refer to the documentation for those products for more information on installation or configuration.

1. Use `vagrant box add` to install an Ubuntu 14.04 x64 box for the vmware_fusion provider. The "bento/ubuntu-14.04" box is a good option here.

2. Place the files from the `consul` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`) or just download the specific files from the the `consul` folder.

3. If you are using a Vagrant box other than my Ubuntu 14.04 base box (referred to in step #1), edit the `machines.yml` file to specify the box in use. If necessary to avoid IP address conflicts with existing networks, you may also need to edit the IP addresses specified in this file. Generally, no other changes are needed, although (if you are comfortable with the settings) you can adjust the number of virtual CPUs and/or the amount of RAM assigned to each Vagrant VM in this file as well. Note that this environment _assumes_ the presence of an `eth1` in each Vagrant VM; therefore, do not remove the "ip_addr" value from `machines.yml`.

4. Once you have edited `machines.yml`, use `vagrant up` to bring up the 3 systems that will serve as your Consul cluster.

5. Once Vagrant has finished bringing up the VMs, run `ansible-playbook provision.yml`. Ansible will use the configuration file in the current directory to pull inventory from the file named `hosts` (it was automatically generated when you ran `vagrant up`). This will provision and configure Consul on each of the Vagrant VMs.

    **NOTE**: Due to the way the Ansible provisioner in Vagrant works, it's currently not possible to provision the VMs with Ansible from within the `Vagrantfile`. As a result, step #5 (running `ansible-playbook` manually) is needed.

At this point, you have a functional Consul cluster running under Vagrant. If you are using VMware Fusion, you should have IP connectivity to the VMs, and can use the OS X `consul` binary to connect to the cluster and test it. For example, this command would work to demonstrate that Consul is working (you would need to change the IP address provided after `-rpc-addr`):

	consul members -rpc-addr=192.168.1.101:8400

Enjoy!
