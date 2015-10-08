# Using Vagrant with OpenStack (Multiple Instances)

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) with OpenStack ([http://www.openstack.org](http://www.openstack.org)), where the VMs managed by Vagrant are instances in an OpenStack cloud. This configuration supports Vagrant operations against multiple instances, and was tested using Vagrant 1.7.4, version 0.7.0 of [the vagrant-openstack-provider plugin](https://github.com/ggiamarchi/vagrant-openstack-provider), and the "Juno" release of OpenStack.

## Contents

* **credentials.yml**: This YAML file contains the username, password, and tenant name for authenticating against OpenStack. You **must** edit this file to specify the correct information for your OpenStack installation.

* **instances.yml**: This YAML file contains the instance-specific information for the instance that will be created in OpenStack. You **must** edit this file to specify the correct instance name (as shown in the OpenStack Dashboard), flavor, image, floating IP pool, network name, SSH keypair name, and SSH username. _Failure to edit this file may result in errors trying to run Vagrant._

* **README.md**: The file you're currently reading.

* **Vagrantfile**: This file is used by Vagrant to spin up the OpenStack instance. You **must** edit this file to specify the correct URL for use with your OpenStack installation.

## Instructions

Please note that you'll need a working OpenStack installation in order to use these files.

1. Edit `credentials.yml` to supply a valid username, password, and tenant name for your particular OpenStack installation.

2. Edit `Vagrantfile` and look for comments stating "Edit the following line with your correct information". There are **three** changes you must make in the `Vagrantfile`:
    * You must set the correct URL for use with your OpenStack installation (see line 21 of `Vagrantfile`).
    * You must set the correct path and filename for the SSH private key to use with the OpenStack instances (see line 31 of `Vagrantfile`)
    * You must verify the names of the security groups specified for use with the OpenStack instances (see line 46 of `Vagrantfile`).

3. Edit `instances.yml`. The fields in this YAML file is fairly straightforward, but here's a quick breakdown:
    * A display name for the instance ("name")
    * The name of an OpenStack flavor ("flavor"), which you can obtain using `nova flavor-list`
    * The name of an OpenStack image ("image"), obtainable via the `glance image-list` command
    * The name of a floating IP pool ("ip_pool")
    * The name of the tenant network to which the new instance should be attached ("networks")
    * The SSH keypair that OpenStack should inject into the instance ("keypair")
    * The username Vagrant can use to log into the instance ("ssh_user"); this will vary from image to image

4. If you wish to spin up additional instances, add stanzas to the `instances.yml` file, making sure to observe the correct YAML syntax.

5. Ensure that the system running Vagrant has connectivity to the authentication URL for OpenStack.

6. Run `vagrant up` to have Vagrant authenticate to OpenStack and create the desired instance for you. Once the instance is created, you can use `vagrant ssh` to connect to the instance, and `vagrant destroy` will terminate (destroy) the OpenStack instance for you.

Enjoy!
