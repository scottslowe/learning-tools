#!/bin/bash

# Initial housekeepting
export DEBIAN_FRONTEND=noninteractive

# Update package list
sudo apt-get update

# Install ipvsadm if not already installed
if [[ ! -e /sbin/ipvsadm ]]; then
    sudo apt-get -yq install ipvsadm
fi

# Configure /etc/default/ipvsadm
# Set ipvsadm to start automatically
sudo sed -i 's/^AUTO=\"false\"/AUTO=\"true\"/' /etc/default/ipvsadm

# Set ipvsadm to master mode
sudo sed -i 's/^DAEMON=\"none\"/DAEMON=\"master\"/' /etc/default/ipvsadm

# Set ipvsadm interface to eth1
sudo sed -i 's/^IFACE=\"eth0\"/IFACE=\"eth1\"/' /etc/default/ipvsadm

# Restart the ipvsadm service
sudo service ipvsadm restart
