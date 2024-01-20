# Remote Docker Host via SSH on AWS Using Pulumi

These files were created to demonstrate how to create a [Pulumi](https://www.pulumi.com) Docker provider that connects via SSH to a remote [Flatcar Linux](https://www.flatcar.org/) EC2 instance on AWS. This Pulumi program was written in [Go](https://go.dev).

Based on the "Flatcar Linux on AWS Using Pulumi" code, the Pulumi program here illustrates a couple useful patterns:

* Use of the `slices.Contains()` method for checking configuration values passed in from the user
* Using [the Pulumiverse Time provider](https://www.pulumi.com/registry/packages/time/) to introduce a delay in resource creation (allowing the EC2 instance to become ready)
* Configuring an explicit Docker provider to use SSH

## Contents

* `go.mod`: This file contains dependencies used by this Go program.

* `go.sum`: This file contains checksums for each of the direct and indirect dependencies. The checksum is used to validate that none of them has been modified.

* `main.go`: This Go file is the Pulumi program executed by the `pulumi` CLI, and contains the resource definitions to create a VPC with only public subnets, a security group to allow SSH access, and a Flatcar Linux-based EC2 instance in one of the public subnets.

* `Pulumi.yaml`: This is the Pulumi project file.

* `README.md`: This file you're currently reading.

## Instructions

These instructions assume you've already installed and configured Pulumi and all necessary dependencies (Go, for this example). Please refer to the Pulumi documentation for more details on installation or configuration.

1. Copy the contents of this directory down to a directory on your system, or clone the entire repository and then change into the directory where this section of the cloned repository resides.

1. Run `pulumi stack init` to create a new stack.

1. Run `pulumi config set aws:region <region-name>` to set the AWS region where the Pulumi program should create resources. _This is a required configuration value; CLI operations will fail if you don't set this value._

1. Run `pulumi config set keypair <name-of-aws-keypair>` to set the name of the AWS keypair that should be used. _This is a required configuration value._

1. Run `pulumi config set privatekeyfile <path-to-private-key-file>` to set the name of the private key file for the AWS keypair specified in the previous step. _This is a required configuration value._

1. (Optional) Run `pulumi config set` to set configuration values that affect the behavior of the Pulumi program. The optional configuration values are:

    * `architecture`: Set this to "amd64" or "arm64". The values "x86_64" and "x64" are also supported and will have the same effect as "amd64". The default value is "arm64".
    * `networkcidr`: Set this to control the CIDR that will be used when the VPC is created. The default value is "10.0.0.0/16".
    * `channel`: Set this to "stable", "alpha", "beta", or "lts" to control the release channel for the Flatcar Linux instance. The default value is "stable".

1. Run `pulumi up` to instantiate the resources. The Pulumi program will create the Flatcar Linux EC2 instance, then use the Docker provider to pull down an image and launch a container---all remotely via SSH.

Enjoy! When you're finished, run `pulumi destroy` to tear down all the provisioned resources.

## License

This content is licensed under the MIT License.
