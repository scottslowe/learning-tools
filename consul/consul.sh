#!/bin/bash

# Update list of packages
sudo apt-get update

# Install unzip package, needed to decompress Consul download
sudo apt-get -y install unzip

# Get Consul download
wget https://dl.bintray.com/mitchellh/consul/0.4.1_linux_amd64.zip

# Decompress and remove Consul download
unzip 0.4.1_linux_amd64.zip
rm 0.4.1_linux_amd64.zip

# Move Consul binary to location in path
sudo mv consul /usr/local/bin/

# Create directories needed by Consul
sudo mkdir -p /etc/consul.d/{bootstrap,server}
