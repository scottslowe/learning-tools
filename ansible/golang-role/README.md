# Installing Go on Linux

This directory contains a simple Ansible role for installing Go onto a Linux system. It's been tested with Debian 10.4 and Ubuntu 18.04, but should work with just about any Linux distribution.

## Contents

* **tasks/main.yml:** This contains the list of Ansible tasks to be performed when the role is executed.

* **vars/main.yml:** This contains some variables for the role.

## Instructions

Simply copy the contents of this directory into the correct location for your Ansible roles (this will vary based on use case), and then include the role in your Ansible playbook.

## More Information

* The role defaults to installing Go 1.15.2. You can change the version by changing the value of the `golang_version` variable in `vars/main.yml`.
* The role defaults to installing Go into the `/usr/local/go` directory. You can modify the prefix (say, to install to `/opt/go`) by changing the value of the `golang_install_path` variable in `vars/main.yml`.
* The role downloads the Go installation archive to `/usr/local/src/go<version>/` (creating this directory if necessary), and if the archive already exists it won't download it again.
* If `/usr/local/go/bin/go` exists, then the role won't extract the Go installation archive to install Go. If you've changed the `golang_install_path` variable, then the role uses this to check for the presence of the Go binary.
