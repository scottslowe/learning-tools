#!/bin/bash

# Update package lists
sudo apt-get update

# Install necessary packages
sudo apt-get -y install python-pip python-dev

# Install OpenStack Client (OSC)
sudo pip install python-openstackclient
