# Using Pulumi to create a VPC Route Server

This [Pulumi](https://www.pulumi.com/) project allows users to create a VPC Route Server, typically used for exchanging routes via BGP with some entity within the VPC (an EC2 instance running BGP, or a Kubernetes cluster using a CNI that supports BGP). This Pulumi program was written in [Go](https://go.dev/).

This Pulumi program is not complex, but it does provide a working example of how to manage all the different components involved in setting up a working VPC Route Server.

## Contents

* `go.mod`: This file contains the dependencies used by this Go program.

* `go.sum`: This file contains checksums for each of the direct and indirect dependencies used by this Go program. This checksum is used to validate that none of them has been modified.

* `main.go`: This Go file is the Pulumi program executed by the `pulumi` CLI.

* `Pulumi.yaml`: This is the Pulumi project file.

* `README.md`: This file you're currently reading.

## Instructions

These instructions assume you've already installed and configured Pulumi and all necessary dependencies (the AWS CLI and Go, for this example), and that you are already logged into a supported Pulumi backend. Please refer to [the Pulumi documentation](https://www.pulumi.com/docs/index.html) for more details on installation or configuration.

1. Copy the contents of this directory down to a directory on your system, or clone the entire repository and then change into the directory where this section of the cloned repository resides.

1. Edit `main.go` and replace any `userSupplied*` variables with actual values. This program requires the following values:

      * The ID of a VPC with which to associate the Route Server (noted in the program as `userSuppliedVpcId`)
      * The ID of a subnet in which to create the Route Server Endpoint (noted in the program as `userSuppliedPrivateSubnetId`)
      * The ID of a VPC route table into which the Route Server should inject routes learned via BGP (noted in the program as `userSuppliedPrivateRouteTableId`)
      * The IP address of an entity running BGP with which the Route Server should create a peering relationship (noted in the program as `userSuppliedPeerIpAddress`)

      The program will not work until all four of these values are supplied.

1. Run `pulumi stack init` to create a new stack.

1. Run `pulumi config set aws:region <region-name>` to set the AWS region where the Pulumi program should create resources. Depending on the region you wish to use and the configuration of your local AWS CLI, you may be able to omit this step.

1. Run `pulumi up` to instantiate the resources.

Once the resources are provisioned, you should be able to use the AWS CLI (in particular, the `aws vpc describe-route-servers` command) to verify that the VPC Route Server has been created. Due to the number of interrelated components (the Route Server, the Route Server Endpoint, the Route Server Propagation, and the Route Server Peer), you may find it more useful to use the AWS management UI instead of the CLI.

When you're finished, run `pulumi destroy` to tear down all the provisioned resources.

## Next Steps

It is left as an exercise for the reader to create the necessary prerequisite infrastructure (VPC, subnets, route tables, etc.) using Pulumi and pass that information to these resources. This could be done in a single Pulumi program or using StackReferences with separate Pulumi programs.

## License

This content is licensed under [the MIT License](../../LICENSE).
