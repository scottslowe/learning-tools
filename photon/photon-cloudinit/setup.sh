#!/bin/bash

# Make directory for NoCloud cloud-init datasource
if [ ! -d "/var/lib/cloud/seed/nocloud" ]; then
    mkdir /var/lib/cloud/seed/nocloud
fi

# Put files into the correct locations for cloud-init
# First move the user-data file, if it exists in the home directory
if [ -f "/home/vagrant/user-data" ]; then
    mv /home/vagrant/user-data /var/lib/cloud/seed/nocloud/
fi

# Next move the meta-data file, if it exists in the home directory
if [ -f "/home/vagrant/meta-data" ]; then
    mv /home/vagrant/meta-data /var/lib/cloud/seed/nocloud/
fi
