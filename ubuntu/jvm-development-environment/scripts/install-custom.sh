#!/usr/bin/env bash

if [ "$1" = "" ]
then
 echo "No user-specific Ansible playbook defined. Nothing to do."
 exit
fi

# make sure we can get the apt lock before running the plays
until sudo apt-get update; do echo "Waiting for apt-get lock"; sleep 5; done

# the GitHub location of the custom Ansible plays to run eg, kurron/ansible-pull-desktop-tweaks.git
PROJECT=$1

ansible-pull --checkout master \
             --directory /tmp/ansible-pull-custom \
             --inventory=localhost, \
             --module-name=git \
             --url=https://github.com/${PROJECT} \
             --verbose \
             --only-if-changed \
             --user vagrant \
             playbook.yml

