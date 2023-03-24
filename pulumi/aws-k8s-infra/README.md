# Creating AWS Infrastructure for Kubernetes

This set of files provides an example on how to create AWS infrastructure for use with Kubernetes using Pulumi and Go.

## Contents

* **go.mod**: This file is used by Go to determine modules/libraries that are needed by the program. You shouldn't need to modify this file unless you are modifying the code.

* **main.go**: This is the Go program that Pulumi will compile and execute to create the infrastructure defined in it. Unless you need to modify the infrastructure resources it creates, you shouldn't need to modify this file.

* **Pulumi.yaml**: This is the Pulumi project file. You can modify this file _before creating resources_ if you want to change the name of the project or the description of the project. Don't modify it after you've created resources.

* **README.md**: This file you're currently reading.

## Instructions

These instructions assume you've already installed and configured Pulumi, the AWS CLI, Go, and all necessary dependencies (as needed by your particular OS). Please refer to the Pulumi documentation for more details on installation or configuration.

1. Clone this repository down to your local system, and change into this directory.
2. Run `pulumi whoami -v` to ensure you are logged into a Pulumi backend. (This project assumes you are using the Pulumi Service.)
3. If you wish to change the name or description of the Pulumi project, edit `Pulumi.yaml` and provide the desired values.
4. Run `pulumi stack init` to create a new stack.
5. Review the configuration values listed in the "Configuration Values" section below, and use `pulumi config set` to set any values that are required or where you don't want to accept the default value.
6. Run `pulumi up` and follow the prompts.
7. After Pulumi has finished, you can use the resulting AWS infrastructure to bootstrap a Kubernetes cluster using `kubeadm` as outlined in [this blog post][link-1].

Enjoy!

## Configuration Values

This Pulumi program uses the following configuration values:

* `aws:region`: This is a _required_ configuration value. Set it to the AWS region where you'd like the infrastructure created.
* `sshKeyPair`: This is a _required_ configuration value. Set it to the name of an AWS key pair present in the region you've specified.
* `networkCidr`: Specify the CIDR block you'd like to use for the AWS VPC in the form of "X.X.X.X/X". The default value is "10.0.0.0/16". This configuration value is optional.
* `subnetMask`: Specify the prefix you'd like to use for the subnets created in the VPC. The default value is 22 (i.e., it will create subnets with enough IP addresses for about 1000 instances). This configuration value is optional.
* `clusterName`: Provide the name of the Kubernetes cluster you plan to provision using `kubeadm`. This is needed to populate tags on various AWS resources in order to support AWS integration with Kubernetes. The default value is "test". This configuration value is optional, but _strongly recommended_.
* `ownerTagValue`: Use this configuration value to populate an "Owner" tag on all provisioned AWS resources. This configuration value is optional, and the default value is "nodody@nowhere.com".
* `teamTagValue`: Use this optional configuration value to populate a "Team" tag on all provisioned AWS resources. The default value is "TeamOfOne".

## License

This content is licensed under the MIT License.

[link-1]: https://blog.scottlowe.org/2019/08/14/setting-up-aws-integrated-kubernetes-115-cluster-kubeadm/
