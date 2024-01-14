# Learning Tools: Debian GNU/Linux

Here you'll find Vagrant environments and other resources for learning more about Debian GNU/Linux.

## Contents

**debian-generic**: This folder has a Vagrant environment for spinning up a generic, 64-bit Debian 8.0 ("Jessie") VM. Note there's nothing special here---just a plain, generic Debian VM. No custom provisioning is provided in this environment.

**debian-pulumi-aws**: This folder has a Pulumi program for spinning up an EC2 instance in a public subnet of a dedicated VPC. The program can create an EC2 instance that is either AMD64- or ARM64-based, and supports Debian 10, 11, and 12. The CPU architecture and Debian version are configurable via configuration values.

**debian-pulumi-azure**: This folder has a Pulumi program for spinning up a Debian VM on Azure. The program supports Debian 10, 11, and 12. The Debian version is configurable via a configuration value.

**stretch-generic**: This folder has a Vagrant environment for spinning up a generic, 64-bit Debian 9.0 ("Stretch") VM. No custom provisioning is provided in this environment.
