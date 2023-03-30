# Learning Tools: Pulumi

This folder contains environments, resources, and sample code for working with [Pulumi](https://www.pulumi.com).

## Contents

**aws-k8s-infra**: This folder contains example code to instantiate AWS infrastructure for use by Kubernetes clusters bootstrapped using `kubeadm`.

**default-aws-infra**: This folder contains example code to use the "default" infrastructure in your AWS account (the default VPC, subnets, Internet gateway, etc.).

**sandbox**: This folder contains Vagrant, Ansible, and Packer resources to create a sandbox for learning Pulumi.

## Related Projects

[talos-aws-pulumi](https://github.com/scottslowe/talos-aws-pulumi): This repository contains Pulumi code (in Golang) to stand up a Talos Linux cluster on AWS.

[talos-azure-pulumi](https://github.com/scottslowe/talos-azure-pulumi): This repository contains Pulumi code (in Golang) to stand up a Talos Linux cluster on Azure.
