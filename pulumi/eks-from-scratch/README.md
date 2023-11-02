# Building an AWS EKS Cluster from Scratch

This set of files provides an example on how to stand up an AWS Elastic Kubernetes Service (EKS) cluster "from scratch" with Pulumi. In this context, "from scratch" means not using any component resources such as those provided by the AWSX or EKS components.

This example uses [Go][link-1].

## Contents

* `go.mod`: This file contains dependencies used by this Go program.

* `go.sum`: This file contains checksums for each of the direct and indirect dependencies. The checksum is used to validate that none of them has been modified.

* `iam.go`: This Go file contains Pulumi code to create the necessary IAM elements for an EKS cluster. It is called by `main.go`.

* `main.go`: This Go file contains all the necessary Pulumi code to launch an EC2 instance on your default AWS infrastructure.

* `Pulumi.yaml`: This is the Pulumi project file.

* `README.md`: This file you're currently reading.

* `vpc.go`: This Go file contains Pulumi code to create a VPC and all necessary resources (subnets, gateways, etc.) for proper operation with an EKS cluster. It is called by `main.go`.

## Instructions

These instructions assume you've already installed and configured Pulumi and all necessary dependencies (Go, for this example). Please refer to the Pulumi documentation for more details on installation or configuration.

1. Copy the contents of this directory down to a directory on your system, or clone the entire repository and then change into the directory where this section of the cloned repository resides.

1. Run `pulumi stack init` to create a new stack.

1. Run `pulumi up` to instantiate the resources.

Enjoy! When you're finished, run `pulumi destroy` to tear down all the provisioned resources.

## Additional Notes

* The `vpc.go` file adds Kubernetes-related tags to resources to enable proper AWS integration. However, if you change the name of the cluster to something other than "testcluster" (this name is found in `main.go`), then be sure to adjust the tags appropriately (the cluster name is embedded in the tag names).

## License

This content is licensed under the MIT License.

[link-1]: https://go.dev
