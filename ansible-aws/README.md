# Using Ansible to Orchestrate AWS

These files provide an example of how to use Ansible to orchestrate actions on Amazon Web Services (AWS). These files were tested using Ansible 2.1.1 on macOS 10.11.5.

## Contents

* **ansible.cfg**: This file tells Ansible where to find the default inventory file (the file `inventory` in the same directory).

* **create.yml**: This Ansible playbook creates infrastructure on AWS. Be sure to edit this file to specify the values listed in the `vars` section at the top of the file.

* **delete.yml**: This Ansible playbook leverages the `ec2.py` dynamic inventory script to tear down (delete) AWS infrastructure. Be sure to edit this file to specify the values in the `vars` sections (one at the top and one farther down).

* **ec2.ini**: This file is the configuration file for the dynamic inventory script. Edit the `regions=` line in this file to specify the AWS regions where instances may be running.

* **ec2.py**: This is a dynamic inventory script to query AWS APIs and generate an inventory that Ansible can use. No edits are needed to this file.

* **inventory**: This is a simple Ansible inventory file that points to the local host. It includes a definition of "ansible_python_interpreter" to help work around Python virtualenv issues.

* **README.md**: The file you're currently reading.

## Instructions

These instructions assume that you have an AWS account, that you know your AWS access key ID and secret access key, and that Ansible is installed and working on your system.

1. Place the files from the `ansible-aws` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`) or just download the specific files from the `ansible-aws` folder.

2. Edit `create.yml` and `delete.yml` to specify the values listed in the `vars` section of each file. Note that there are _two_ `vars` sections in `delete.yml` because there are two plays in the playbook.

3. To create AWS infrastructure using ansible, run `ansible-playbook create.yml`.

4. To delete the infrastructure created in step 3, run `ansible-playbook -i ./ec2.py delete.yml`.

Enjoy!

## License

This content is licensed under the MIT License.
