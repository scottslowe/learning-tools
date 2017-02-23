# Using Terraform with OpenStack

These files were created to allow users to use an example Terraform ([http://terraform.io](http://terraform.io)) configuration with OpenStack. These files require a working Terraform installation and a working OpenStack environment.

## Contents

* **README.md**: This file you're currently reading.

* **tf-example**: This directory contains an example Terraform configuration written in Terraform format.

* **tf-json-example**: This directory contains an example Terraform configuration written in JSON format.

## Instructions

These instructions assume you've already installed Terraform. These instructions also assume that you have a working OpenStack environment against which to run the Terraform configurations.

1. Edit `provider.tf` or `provider.tf.json` with the correct username, tenant, password, and authentication URL for your OpenStack environment.
2. Edit `vars.tf` or `vars.tf.json` to provide the correct values for image name, flavor, external network (for the logical router), SSH key pair, and floating IP pool.
3. From either the `tf-example` or `tf-json-example` directory, run `terraform plan` to see what changes will be made.
4. If you are happy with the output of #3, run `terraform apply`.

Enjoy!

## License

This material is licensed under the MIT License.
