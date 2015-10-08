# Using Vagrant with OpenStack (Single Instance)

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) with OpenStack, where the VMs managed by Vagrant are actually instances in an OpenStack cloud. Using these files, Vagrant can only operate against a single instance at a time. This configuration was tested using Vagrant 1.7.4, version 0.7.0 of [the vagrant-openstack-provider plugin](https://github.com/ggiamarchi/vagrant-openstack-provider), and OpenStack "Juno".

## Contents

* **credentials.yml**: This YAML file contains the username, password, and tenant name for authenticating against OpenStack. You **must** edit this file to specify the correct information for your OpenStack installation.

* **instances.yml**: This YAML file contains the instance-specific information for the instance that will be created in OpenStack. You **must** edit this file to specify the correct instance name (as shown in the OpenStack Dashboard), flavor, image, floating IP pool, network name, SSH keypair name, and SSH username. _Failure to edit this file may result in errors trying to run Vagrant._

* **README.md**: The file you're currently reading.

* **Vagrantfile**: This file is used by Vagrant to spin up the OpenStack instance. There are three changes that **must** be made to this file in order for this environment to function properly in your environment: the authentication URL, the path to your SSH keypair, and the security groups for the instance. (See the instructions below for more information.)

## Instructions

Note that you'll need a working OpenStack installation in order to use these files.

1. Edit `credentials.yml` to supply a valid username, password, and tenant name for your particular OpenStack installation.
2. Edit `instances.yml`. The fields in this YAML file is fairly straightforward, but here's a quick breakdown:
    * A display name for the instance ("name")
    * The name of an OpenStack flavor ("flavor"), which you can obtain using `nova flavor-list`
    * The name of an OpenStack image ("image"), obtainable via the `glance image-list` command
    * The name of a floating IP pool ("ip_pool")
    * The name of the tenant network to which the new instance should be attached ("networks")
    * The SSH keypair that OpenStack should inject into the instance ("keypair")
    * The username Vagrant can use to log into the instance ("ssh_user"); this will vary from image to image
3. Edit `Vagrantfile` and look for comments stating "Edit the following line with your correct information". There are **three** changes you must make in the `Vagrantfile`:
    * You must specify the correct URL for authenticating with OpenStack
    * You must provide the correct path for the private key that matches the keypair specified in `instances.yml`
    * You must supply the list of security groups that OpenStack should apply to the new instance
4. Ensure that the system running Vagrant has connectivity to the authentication URL for OpenStack.
5. Run `vagrant up` to have Vagrant authenticate to OpenStack and create the desired instance for you. Once the instance is created, you can use `vagrant ssh` to connect to the instance, and `vagrant destroy` will terminate (destroy) the OpenStack instance for you.

Enjoy!

## Additional Notes

This environment will only create a single instance on OpenStack using Vagrant. If you wish to use Vagrant to create/manage multiple instances, please see the "vagrant-openstack-multi" directory in this repository instead.
