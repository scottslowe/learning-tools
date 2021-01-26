# Basic Wireguard Installation

These files provide a very basic installation of [Wireguard](https://www.wireguard.com/) on Ubuntu.

## Contents

* **ansible.cfg**: This file tells Ansible where to find the default inventory information (it looks for a file named `hosts` in the same directory).

* **README.md**: The file you're currently reading.

* **wireguard.yml**: This Ansible playbook performs a very basic installation of Wireguard, including the generation of private and public keys.

## Instructions

These instructions assume that Ansible is installed and working on your system.

1. Place the files from the `ansible/wireguard` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`) or just download the specific files from the `ansible/wireguard` folder.

2. Edit `hosts` and add the necessary inventory information for Ansible.

3. Run `ansible-playbook wireguard.yml` to run the Ansible playbook against the systems specified in the inventory file.

## License

This content is licensed under the MIT License.
