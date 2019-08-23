# Creating AWS Infrastructure for Kubernetes

This set of files provides an example on how to create AWS infrastructure for use with Kubernetes using Pulumi and TypeScript.

## Contents

* **index.ts**: This TypeScript file contains the code used by Pulumi to instantiate the AWS infrastructure.

* **README.md**: This file you're currently reading.

## Instructions

These instructions assume you've already installed and configured Pulumi and all necessary dependencies (Node, NPM, and associated packages, as needed by your particular OS). Please refer to the Pulumi documentation for more details on installation or configuration.

1. Start a new Pulumi project, configured for AWS and TypeScript (you can use `pulumi new` for this step if you prefer).

2. Copy `index.ts` from this directory into the directory for the new Pulumi project.

3. Edit `index.ts` as outlined below in the section "Providing Your Own Information".

4. Once `index.ts` has been appropriately customized for your specific environment, run `pulumi up` and follow the prompts.

5. After Pulumi has finished, you can use the resulting AWS infrastructure to bootstrap a Kubernetes cluster using `kubeadm` as outlined in [this blog post][link-1].

Enjoy!

## Providing Your Own Information

The `index.ts` file provided in this folder **will not work** without providing your own information. This section outlines the changes that need to be made to the `index.ts` file.

1. On line 7, change the value of `keypair` to the name of an AWS keypair to which you have access.

2. On line 21, change the value of `owners` to an appropriate value. This could be the account number for your own AWS account (if looking up a private AMI), or it could be the account number for an account that distributes public AMIs, like the Canonical account that distributes public Ubuntu AMIs.

3. On line 24, change this value to a search string that will find the AMI you're seeking. For example, the search string "ubuntu/images/hvm-ssd/ubuntu-bionic-18.04-amd64-server*" will generally help locate an x86_64 version of the Ubuntu 18.04 server AMI.

4. On lines 181 and 201, a reference is made to an IAM instance profile. This profile is not created for you; it must be created manually beforehand. If you use a different name for the instance profiles, you must edit these lines accordingly. Refer [here][link-1] for more details on these IAM instance profiles.

These are the only _required_ changes. However, you may wish to make further customizations:

* If you change the value of `cidrBlock` on line 33, then you **must** also adjust the value of `netAddr` on line 46.
* On line 10, the name of an AWS tag that is required by Kubernetes is specified. Whatever value is included in the last part of this tag name (after the `kubernetes.io/cluster/` portion) should _also_ be used in the `kubeadm` configuration files used to bootstrap Kubernetes.

## License

This content is licensed under the MIT License.

[link-1]: https://blog.scottlowe.org/2019/08/14/setting-up-aws-integrated-kubernetes-115-cluster-kubeadm/
