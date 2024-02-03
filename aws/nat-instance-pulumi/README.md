# Using a NAT Instance for Private Subnet Connectivity

This [Pulumi](https://www.pulumi.com) project allows users to stand up and configure a NAT instance---instead of a Managed NAT Gateway---for internet connectivity from private subnets in a VPC. This Pulumi program was written in [Go](https://go.dev).

While not complex, the Pulumi program here does illustrate a few things that might be useful for newer users:

* How to structure Go code in Pulumi when splitting the code into multiple files for manageability
* Supporting both X86_64/AMD64- as well as ARM64-based configurations
* Dynamically looking up an AMI
* Creating an SSH key

## Contents

* `go.mod`: This file contains dependencies used by this Go program.

* `go.sum`: This file contains checksums for each of the direct and indirect dependencies. The checksum is used to validate that none of them has been modified.

* `main.go`: This Go file is the Pulumi program executed by the `pulumi` CLI. It calls `vpc.go` and `nat.go` to build out all the underlying infrastructure, then launches an Ubuntu-based EC2 instance in a private subnet. This instance can be used to verify connectivity is working through the NAT instance as expected.

* `nat.go`: This Go file contains a function (`buildNat`) that is called by `main.go` to build out the pieces for the NAT instance.

* `Pulumi.yaml`: This is the Pulumi project file.

* `README.md`: This file you're currently reading.

* `vpc.go`: This Go file contains a function (`buildInfrastructure`) that is called by `main.go` to create the VPC, subnets, and route tables. Routes for public subnets are also created here, but routes for private subnets are defined in `nat.go`.

## Instructions

These instructions assume you've already installed and configured Pulumi and all necessary dependencies (the AWS CLI and Go, for this example). Please refer to the Pulumi documentation for more details on installation or configuration.

1. Copy the contents of this directory down to a directory on your system, or clone the entire repository and then change into the directory where this section of the cloned repository resides.

1. Run `pulumi stack init` to create a new stack.

1. Run `pulumi config set aws:region <region-name>` to set the AWS region where the Pulumi program should create resources. _This is a required configuration value; CLI operations will fail if you don't set this value._

1. (Optional) Run `pulumi config set` to set configuration values that affect the behavior of the Pulumi program. The optional configuration values are:

    * `architecture`: Set this to "amd64" or "arm64". The values "x86_64" and "x64" are also supported and will have the same effect as "amd64". The default value is "arm64".
    * `versionname`: Set this to "bionic", "focal", or "jammy" to control the version of Ubuntu used in the EC2 instance. These version names correspond to the 18.04, 20.04, and 22.04 releases, respectively. The default value is "jammy".

1. Run `pulumi up` to instantiate the resources.

Once the resources are provisioned, you should be able to SSH to the NAT instance, or use the NAT instance as an SSH bastion host to get to the private instance. The private instance should have full Internet connectivity.

When you're finished, run `pulumi destroy` to tear down all the provisioned resources.

## License

This content is licensed under the MIT License.
