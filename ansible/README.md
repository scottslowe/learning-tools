# Learning Tools: Ansible

This folder contains tools, resources, and examples to help with learning how to use Ansible in a variety of environments and for a variety of use cases.

## Contents

**ansible-aws**: This set of files shows how to use Ansible to both provision infrastructure on AWS as well as how to decommission (tear down) that same infrastructure.

**bootstrap**: This folder contains files that show how to use Ansible to "bootstrap" an Ubuntu 16.04 node with the necessary Python support in order to be able to run additional Ansible modules/playbooks.

**extract-gh-archive**: This folder contains a Vagrant+Ansible environment that demonstrates a few techniques for using Ansible to download a binary release from GitHub, extract it into a temporary directory, copy out the relevant file(s), and then clean up.

**kubeadm-template**: In this folder is an example Jinja2 template and Ansible playbook that provide an example of how to create a templated Kubeadm configuration file.

**src-dst-list**: This set of files shows how to use complex lists with Ansible's "with_items" construct. This allows you to specify, for example, both source and location for a "copy" task in a single block (when both source and destination vary from item to item).
