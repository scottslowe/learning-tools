#!/bin/bash

# Update list of packages
sudo apt-get update

# Install unzip package needed to decompress Consul download
export DEBIAN_FRONTEND=noninteractive
sudo apt-get -y install unzip

# Get Consul download
wget https://dl.bintray.com/mitchellh/consul/0.5.0_linux_amd64.zip

# Decompress and remove Consul download
unzip 0.5.0_linux_amd64.zip
rm 0.5.0_linux_amd64.zip

# Move Consul binary to location in path
sudo mv consul /usr/local/bin/

# Create consul user
useradd -M -d /var/consul -r -s /usr/bin/nologin consul

# Create directories needed by Consul
sudo mkdir /var/consul
sudo chown -R consul:consul /var/consul
sudo mkdir -p /etc/consul.d/server
sudo chown -R root:consul /etc/consul.d

# Move files into the correct locations
sudo mv /home/vagrant/consul.conf /etc/init/consul.conf
sudo chown root:root /etc/init/consul.conf
sudo mv /home/vagrant/config.json /etc/consul.d/server/config.json
sudo chown root:consul /etc/consul.d/server/config.json
