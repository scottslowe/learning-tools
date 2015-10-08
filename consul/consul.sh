#!/bin/bash

# Update list of packages
sudo apt-get update

# Install unzip package, needed to decompress Consul download
export DEBIAN_FRONTEND=noninteractive
sudo apt-get -y install unzip

# Download and install Consul binary, if needed
if [[ ! -e /usr/local/bin/consul ]];then

  # Download Consul
  curl -kLO https://dl.bintray.com/mitchellh/consul/0.5.2_linux_amd64.zip

  # Decompress and remove Consul download
  unzip 0.5.2_linux_amd64.zip
  rm 0.5.2_linux_amd64.zip

  # Move Consul binary to location in path
  sudo mv consul /usr/local/bin/
fi

# Create consul user
useradd -M -d /var/consul -r -s /usr/bin/nologin consul

# Create and configure Consul working directory
sudo mkdir -p /var/consul
if [ -d /var/consul ]; then
  sudo chown -R consul:consul /var/consul
fi

# Create and configure Consul configuration directories
sudo mkdir -p /etc/consul.d/server
if [ -d /etc/consul.d ]; then
  sudo chown -R root:consul /etc/consul.d
fi

# Move files into the correct locations
sudo mv /home/vagrant/consul.conf /etc/init/consul.conf
sudo chown root:root /etc/init/consul.conf
sudo mv /home/vagrant/config.json /etc/consul.d/server/config.json
sudo chown root:consul /etc/consul.d/server/config.json
