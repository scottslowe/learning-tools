# Using the Default AWS Infrastructure

This set of files provides an example on how to use the "default AWS infrastructure" (the VPC, subnets, and Internet gateway that are automatically created for you in a region when you start using AWS).

This example uses [Go][link-1].

## Contents

* `go.mod`: This file contains dependencies used by this Go program.

* `main.go`: This Go file contains all the necessary Pulumi code to launch an EC2 instance on your default AWS infrastructure.

* `README.md`: This file you're currently reading.

## Instructions

These instructions assume you've already installed and configured Pulumi and all necessary dependencies (Go, for this example). Please refer to the Pulumi documentation for more details on installation or configuration.

1. Start a new Pulumi project, configured for AWS and Go (you can use `pulumi new` for this step if you prefer).

2. Copy `main.go` and `go.mod` from this directory into the directory for the new Pulumi project.

3. Run `go mod tidy` in the directory where you copied `main.go` and `go.mod`.

4. Edit `main.go` as outlined below in the section "Providing Your Own Information".

5. Once `main.go` has been appropriately customized for your specific environment, run `pulumi up` and follow the prompts.

Enjoy!

## Providing Your Own Information

The `main.go` file provided in this folder **will not work** without providing your own information. This section outlines the changes that need to be made to the `main.go` file.

1. On line 82, verify the AMI ID specified there (everything before `_VERIFY_ME`) is valid for the AWS region you will use. Change this as necessary to a valid AMI ID for your region.

2. On line 83, verify the instance type (everything before `_VERIFY_ME`) is valid for your AWS region. Change as necessary.

3. On line 85, replace `CHANGE_ME` with the name of a valid key pair.

After making these changes, you should be able to run `pulumi up` without any issues.

## License

This content is licensed under the MIT License.

[link-1]: https://go.dev
