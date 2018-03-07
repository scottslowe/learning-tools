# Local Kubernetes Cluster with Kubeadm

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) quickly and relatively easily spin up a local Kubernetes cluster that is bootstrapped using `kubeadm`.

## Contents

* **machines.yml**: This YAML file contains a list of VM definitions. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs. You will need to edit this file to provide the correct Vagrant box names. Other edits should not be necessary.

* **README.md**: This file you're currently reading.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

## Instructions

These instructions assume you've already installed your virtualization provider (VMware Fusion/Workstation or VirtualBox), Vagrant, and any necessary plugins (such as the Vagrant VMware plugin). Please refer to the documentation for those products for more information on installation or configuration.

1. Use `vagrant box add` to install an Ubuntu 16.04 base box. Review `machines.yml` for some suggested boxes for both VirtualBox and VMware virtualization platforms.

2. Place the files from the `kubernetes/kubeadm-vagrant` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`) or just download the specific files from the the `kubernetes/kubeadm-vagrant` folder.

3. If the name of your Ubuntu 16.04 base box is _not_ one of the ones listed in `machines.yml`, edit `machines.yml` to supply the correct box name(s). This file has separate lines for the VirtualBox and VMware providers; be sure to edit the correct line for your provider.

4. Run `vagrant up` to instantiate 3 VMs---one manager and two nodes. All the VMs will be running Ubuntu 16.04.

5. Once Vagrant is finished, use `vagrant ssh master` to log into the manager VM.

6. 

Enjoy!
