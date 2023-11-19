# Learning Tools: Ubuntu Linux

This folder contains materials for learning more about Ubuntu Linux.

## Contents

**ubuntu-generic**: This folder contains a Vagrant environment for spinning up a generic 64-bit Ubuntu 14.04 VM. Note there's nothing special going on here---just a plain, generic, Ubuntu VM. No custom provisioning is provided in this environment.

**ubuntu-pulumi-aws**: This folder has a Pulumi program for spinning up an EC2 instance in a public subnet of a dedicated VPC. The program can create an EC2 instance that is either AMD64- or ARM64-based, and supports Ubuntu 18.04, 20.04, and 22.04. The CPU architecture and Ubuntu version are configurable via configuration values.

**xenial-generic**: This folder contains a Vagrant environment for spinning up a generic Ubuntu 16.04 VM. No custom provisioning is provided in this environment.
