# Customizing the CoreOS Toolbox using Cloud-Init

These files were created to allow users to see how to use cloud-init ([http://cloudinit.readthedocs.org/](http://cloudinit.readthedocs.org/)) to customize the CoreOS ([https://coreos.com/](https://coreos.com/)) toolbox. There are three ways you can use this learning environment:

* Users can use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) and a virtualization provider (such as VMware Fusion with the Vagrant VMware plugin) to spin up a local environment.
* Users can spin up an instance in an existing OpenStack cloud.
* Users can spin up an AWS instance.

## Contents

* **machines.yml**: This YAML file contains a list of VM definitions. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs. You may need to edit this file to provide appropriate IP addresses and other VM configuration data (see "Instructions" below). This file is _not_ needed when testing with OpenStack or AWS intances.

* **README.md**: This file you're currently reading.

* **user-data**: This cloud-init configuration file should be able to be used unchanged, and is used by Vagrant, OpenStack, and AWS (see "Instructions") below.

* **Vagrantfile**: This file is used by Vagrant to spin up local VMs. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file. This file is _not_ needed when testing with OpenStack or AWS instances.

## Instructions

### Using Vagrant

This section assumes you've already installed Vagrant and the necessary virtualization provider. Please refer to the documentation for those products for more information on installation or configuration. This was tested using Vagrant 1.8.1, VMware Fusion 8.1.0, and the Vagrant VMware plugin.

1. Use `vagrant box add` to install the CoreOS Vagrant box. See [http://stable.release.core-os.net/amd64-usr/](http://stable.release.core-os.net/amd64-usr/) for downloads of the Vagrant box for the CoreOS Stable release channel. 

2. Place the files from the `coreos-cloudinit-toolbox` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`) or just download the specific files from the the `coreos-cloudinit-toolbox` folder.

3. Edit `machines.yml` file to provide the specific details on the VMs that Vagrant should create. The `Vagrantfile` expects five values for each VM: `name` (the user-friendly name of the VM, which will also be used as the hostname for the guest OS inside the VM); `box` (the name of the CoreOS Vagrant box downloaded in step #1); `ram` (the amount of memory to be assigned to the VM); `vcpu` (the number of vCPUs that should be assigned to the VM); and `ip_addr` (a private IP address to be statically assigned to the VM).

4. If the box you downloaded in step #1 is named something _other_ than "coreos-stable", then edit `Vagrantfile` and change all instances of "coreos-stable" to match the name of the box you downloaded.

5. Once you have edited `machines.yml` (and `Vagrantfile`, if necessary), use `vagrant up` to bring up the VMs (a single CoreOS VM, by default).

6. Log into the VM using `vagrant ssh`. Once you're logged in, verify that `.toolboxrc` exists in the `core` user's home directory, and verify that `/opt/bin/python` exists. The presence of these two files shows that cloud-init worked properly. Optionally, feel free to run `python -i` and watch CoreOS download the appropriate Python container and drop you at a Python interpreter prompt.

### Using OpenStack

This section assumes that you have a functioning OpenStack cloud and that this cloud already has a CoreOS image for users to use. If this is _not_ the case, you'll need to upload a CoreOS image (or ask your cloud administrator to make one accessible). This was tested using OpenStack "Juno" on Ubuntu/KVM with VMware NSX providing the networking functionality.

Because of wide variations in how OpenStack is configured and in how users consume OpenStack, this section provides only high-level directions as opposed to step-by-step instructions.

* You'll need to supply the `user-data` file from the `coreos-cloudinit-toolbox` directory of the repository (or its contents) to OpenStack when launching your instance(s). In the Dashboard GUI, this is done via the "Post-Creation" tab of the Launch Instance wizard. When using the `nova boot` CLI command, this is done via the `--user-data` flag.

* When you log into the running instance (note you may need to use an SSH bastion host or assign a floating IP to the instance), check for the existence of `.toolboxrc` in the `core` user's home directory and the existence of `/opt/bin/python`. Both of these files are created by cloud-init when OpenStack launches the instance. Optionally, feel free to run `python -i` and watch CoreOS download the appropriate Python container and drop you at a Python interpreter prompt.

### Using AWS

This section assumes you have an Amazon AWS account and are relatively familiar with launching instances on AWS. Due to wide variations in how users may consume AWS resources, this section provides only high-level directions.

* Launch your instance in the desired AWS region. Refer to [https://coreos.com/os/docs/latest/booting-on-ec2.html](https://coreos.com/os/docs/latest/booting-on-ec2.html) for a list of the CoreOS AMIs for each AWS region.

* If you're using the AWS Management Console, you'll need to supply the `user-data` file (or its contents) from the `coreos-cloudinit-toolbox` directory of the repository in step 3 (Configure Instance). This is found in the Advanced Details section. If you're using the AWS CLI, this is done with the `--user-data` flag to the `aws ec2 run-instances` command.

* When you log into the running instance, check for the existence of `.toolboxrc` in the `core` user's home directory and the existence of `/opt/bin/python`. Both of these files are created by cloud-init when AWS launches the instance. Optionally, feel free to run `python -i` and watch CoreOS download the appropriate Python container and drop you at a Python interpreter prompt.

Enjoy!
