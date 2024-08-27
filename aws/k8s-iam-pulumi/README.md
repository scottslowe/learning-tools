# Using Pulumi to Manage AWS IAM Entities for Kubernetes

This [Pulumi](https://www.pulumi.com) project allows users to create some Amazon Web Services (AWS) Identity & Access Management (IAM) entities used when standing up self-managed Kubernetes clusters on AWS. This Pulumi program was written in [Go](https://go.dev).

While not complex, the Pulumi program here does provide an example of creating IAM policies, IAM roles, and instance profiles.

## Contents

* `go.mod`: This file contains dependencies used by this Go program.

* `go.sum`: This file contains checksums for each of the direct and indirect dependencies. The checksum is used to validate that none of them has been modified.

* `main.go`: This Go file is the Pulumi program executed by the `pulumi` CLI.

* `Pulumi.yaml`: This is the Pulumi project file.

* `README.md`: This file you're currently reading.

## Instructions

These instructions assume you've already installed and configured Pulumi and all necessary dependencies (the AWS CLI and Go, for this example), and that you are already logged into a supported Pulumi backend. Please refer to the Pulumi documentation for more details on installation or configuration.

1. Copy the contents of this directory down to a directory on your system, or clone the entire repository and then change into the directory where this section of the cloned repository resides.

1. Run `pulumi stack init` to create a new stack.

1. Run `pulumi config set aws:region <region-name>` to set the AWS region where the Pulumi program should create resources. _This is a required configuration value; CLI operations will fail if you don't set this value._

1. Run `pulumi up` to instantiate the resources.

Once the resources are provisioned, you should be able to use the AWS CLI (in particular, the `aws iam get-policy` command; the necessary policy ARNs are exported as stack outputs) to verify that new IAM entities have been created.

When you're finished, run `pulumi destroy` to tear down all the provisioned resources.

## License

This content is licensed under the MIT License.
