#!/bin/bash

# Initial housekeepting
export DEBIAN_FRONTEND=noninteractive

# Add the PPA repository for LXD/LXC stable
if [[ ! -e /etc/apt/sources.list.d/ubuntu-lxc-lxd-stable-trusty.list ]]; then
    sudo add-apt-repository -y ppa:ubuntu-lxc/lxd-stable
fi

# Update package list
sudo apt-get update

# Install LXC/LXD if not already installed
if [[ ! -e /usr/bin/lxd ]]; then
    sudo apt-get -y install lxd
fi
