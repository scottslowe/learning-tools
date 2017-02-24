# Learning Tools: Vagrant

This folder contains materials for learning more about how to use Vagrant. This includes various `Vagrantfile` examples with different data formats and structures. Examples of working with various providers are also found here.

## Contents

**aws**: This folder contains a `Vagrantfile` and supporting files for using Vagrant to spin up an instance on AWS. This allows you to use Vagrant to spin up and destroy an instance on AWS.

**aws-multi**: Like "vagrant-aws", but for spinning up multiple instances on AWS.

**complex-json**: This folder contains files that illustrate how to use a "complex" JSON file as an external data source for a `Vagrantfile`.

**complex-yaml**: This folder contains files that illustrate how to use "complex" YAML files in a `Vagrantfile`.

**docker**: In this folder you'll find `Vagrantfiles` and supporting documents for using the Docker provider in Vagrant to turn up Docker containers. The provisioning is handled by Vagrant's built-in Docker provisioner.

**docker-json**: Like `docker-yaml`, this folder is an extension of "vagrant-docker" in that it provides the ability to specify, in an external JSON file, a list of Docker containers to be instantiated by Vagrant. The provisioning is handled by Vagrant's built-in Docker provisioner.

**docker-yaml**: This folder is an extension of "vagrant-docker" in that it provides the ability to specify the list of Docker containers that Vagrant will create in a separate YAML file. The provisioning is handled by Vagrant's built-in Docker provisioner.

**json**: The files in this folder illustrate the use of an external JSON data file to drive Vagrant.

**multi-platform**: This Vagrant environment demonstrates one possible way to create a `Vagrantfile` that supports multiple virtualization platforms without any edits needed across platforms.

**multi-provider**: The `Vagrantfile` and supporting documents in this folder show how to build Vagrant environments to support multiple back-end providers.

**openstack**: This folder contains a `Vagrantfile` and supporting files for using Vagrant with the OpenStack provider. This allows you to use Vagrant to provision and destroy an instance in an OpenStack cloud.

**openstack-multi**: This folder is similar to `vagrant/openstack`, but supports Vagrant operations on multiple instances.
