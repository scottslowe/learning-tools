# Debian on Azure Using Pulumi

These files were created to allow users to quickly and easily deploy a Debian VM on Azure using [Pulumi](https://www.pulumi.com). This Pulumi program was written in [Go](https://go.dev).

While not complex, the Pulumi program here does illustrate a few things that might be useful for newer users:

* Use of the `value, ok := map[key]` idiom for checking configuration values passed in from the user
* Creating and assigning a public IP to a VM

## Contents

* `go.mod`: This file contains dependencies used by this Go program.

* `go.sum`: This file contains checksums for each of the direct and indirect dependencies. The checksum is used to validate that none of them has been modified.

* `main.go`: This Go file is the Pulumi program executed by the `pulumi` CLI, and contains the resource definitions to create a Vnet, a single subnet, a security group to allow SSH access, and a Debian-based VM with a public IP address.

* `Pulumi.yaml`: This is the Pulumi project file.

* `README.md`: This file you're currently reading.

## Instructions

These instructions assume you've already installed and configured Pulumi and all necessary dependencies (Go, for this example). Please refer to the Pulumi documentation for more details on installation or configuration.

1. Copy the contents of this directory down to a directory on your system, or clone the entire repository and then change into the directory where this section of the cloned repository resides.

1. Run `pulumi stack init` to create a new stack.

1. Run `pulumi config set azure-native:location <region-name>` to set the Azure region where the Pulumi program should create resources. _This is a required configuration value; CLI operations will fail if you don't set this value._

1. (Optional) Run `pulumi config set` to set configuration values that affect the behavior of the Pulumi program. The optional configuration values are:

    * `architecture`: Set this to "amd64" or "arm64". The values "x86_64" and "x64" are also supported and will have the same effect as "amd64". The default value is "arm64".
    * `versionname`: Set this to "buster", "bullseye", or "bookworm" to control the version of Debian used in the VM. These version names correspond to version numbers 10, 11, and 12, respectively. The default value is "bookworm".

1. Run `pulumi up` to instantiate the resources.

Enjoy! When you're finished, run `pulumi destroy` to tear down all the provisioned resources.

## License

This content is licensed under the MIT License.
