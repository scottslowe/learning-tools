# Building an AWS AMI with Packer and Ansible

These files show an example of how to use Packer ([https://www.packer.io/](https://www.packer.io/)) along with Ansible ([https://www.ansible.com/](https://www.ansible.com/)) to create a custom AMI on Amazon Web Services (AWS). This environment was tested using Packer 1.0.0 and Ansible 2.3.0.0 on Fedora 25, but it should work identically on any system where Packer and Ansible are supported.

## Prerequisites

* These files do not supply any sort of AWS authentication credentials. If you have the AWS CLI installed and configured, then no additional action is needed. If you do _not_ have the AWS CLI installed and configured, then you'll need to add your AWS authentication credentials to `template.json` (details on what must be added are available [here](https://www.packer.io/docs/builders/amazon-ebs.html)).

## Contents

* **provision.yml**: This Ansible playbook applies the "docker-ce-edge" role to the AMI being created by Packer, and is called by `template.json`.

* **README.md**: This file you're currently reading.

* **roles**: This directory contains the "docker-ce-edge" Ansible role.

* **template.json**: This is the Packer template that provides Packer with the information on how to build the custom AMI. Aside from adding AWS authentication credentials (as described in the "Prerequisites" section), no edits to this file should be needed.

* **variables.json**: This JSON file supplies variables to be consumed by Packer. You _will_ need to edit this file to supply a VPC ID and subnet ID. You _may_ also want to edit the name and description variables to suit your needs.

## Instructions

These instructions assume you've already installed Packer and Ansible. Refer to the documentation for these products for information on how to install them.

1. Place the files from the `packer/aws-ami-ansible` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`), download a ZIP file of the entire "learning-tools" repository, or just download the specific files from the the `packer/aws-ami-ansible` folder.

2. If you are _not_ using the AWS CLI on the system where you'll use this environment, you'll need to edit `template.json` to supply AWS authentication credentials. (See the "Prerequisites" section above.)

3. Edit `variables.json` to supply a VPC ID and subnet ID where you'd like Packer to launch the temporary build instance. You may also want to customize the AWS region used and/or the name or description variables.

4. From a terminal window in the directory where these files were placed, run `packer validate -var-file=variables.json template.json` to validate that Packer has all the information it needs and that no syntax errors were introduced in previous steps.

5. Run `packer build -var-file=variables.json template.json` to run the build process. **This will take a few minutes.** When it's all said and done, Packer will output the AMI ID of the newly-created AMI. You can now use this AMI ID to launch instances, and these instances will be preconfigured with the Edge build of Docker CE.

Enjoy!
