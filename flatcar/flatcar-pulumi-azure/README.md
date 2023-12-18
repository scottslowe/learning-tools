# Flatcar Linux on Azure Using Pulumi

These files were created to allow users to quickly and easily deploy a Flatcar Linux VM on Microsoft Azure using [Pulumi](https://www.pulumi.com). This Pulumi program was written in [Go](https://go.dev).

Similar to the Flatcar Linux on AWS Pulumi code, the Pulumi program here is not complex but does help illustrate a few things that newer users might find helpful:

* Uploading a VHD to an Azure storage account and container
* Registering a new VM image from the uploaded VHD
* Launch a VM using the new image

## Contents

* `go.mod`: This file contains dependencies used by this Go program.

* `go.sum`: This file contains checksums for each of the direct and indirect dependencies. The checksum is used to validate that none of them has been modified.

* `main.go`: This Go file is the Pulumi program executed by the `pulumi` CLI, and contains the resource definitions to create an Azure resource group, virtual network and subnet, storage account and container, VM image, and a VM.

* `Pulumi.yaml`: This is the Pulumi project file.

* `README.md`: This file you're currently reading.

## Instructions

These instructions assume you've already installed and configured Pulumi and all necessary dependencies (Go and the Azure CLI, for this example). Please refer to the Pulumi documentation for more details on installation or configuration.

1. Copy the contents of this directory down to a directory on your system, or clone the entire repository and then change into the directory where this section of the cloned repository resides.

1. Run `pulumi stack init` to create a new stack.

1. Run `pulumi config set azure-native:location <region-name>` to set the AWS region where the Pulumi program should create resources. _This is a required configuration value; CLI operations will fail if you don't set this value._

1. (Optional) Run `pulumi config set` to set configuration values that affect the behavior of the Pulumi program. The optional configuration values are:

    * `architecture`: Set this to "amd64" or "arm64". The values "x86_64" and "x64" are also supported and will have the same effect as "amd64". The default value is "arm64".
    * `networkcidr`: Set this to control the CIDR that will be used when the VPC is created. The default value is "10.0.0.0/16".
    * `channel`: Set this to "stable", "alpha", "beta", or "lts" to control the release channel for the Flatcar Linux instance. The default value is "stable".

1. Run `pulumi up` to instantiate the resources.

Enjoy! When you're finished, run `pulumi destroy` to tear down all the provisioned resources.

## License

This content is licensed under the MIT License.
