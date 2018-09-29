#!/bin/bash

# Update list of packages
export DEBIAN_FRONTEND=noninteractive
sudo apt-get update

# Install packages as needed
if [[ ! -e /usr/bin/curl ]]; then
  apt-get -yqq install curl
fi

if [[ ! -e /usr/bin/unzip ]]; then
  apt-get -yqq install unzip
fi

# Download and install Consul binary, if needed
if [[ ! -e /usr/local/bin/consul ]];then

  # Download Consul
  curl -kLOs https://releases.hashicorp.com/consul/1.2.3/consul_1.2.3_linux_amd64.zip

  # Decompress and remove Consul download
  unzip consul_1.2.3_linux_amd64.zip
  rm consul_1.2.3_linux_amd64.zip

  # Move Consul binary to location in path
  sudo mv consul /usr/local/bin/
fi

# Create consul user, if it doesn't already exist
if [ -z "$(getent passwd consul)" ]; then
  useradd -M -d /var/consul -r -s /usr/bin/nologin consul
 else
   echo "Consul user already created."
 fi

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

# Customize the systemd unit file
sed -i s/BINDADDR/$(hostname -I | cut -f2 -d' ')/ /home/vagrant/consul.service

# Move files into the correct locations
sudo mv /home/vagrant/consul.service /etc/systemd/system/consul.service
sudo chown root:root /etc/systemd/system/consul.service
sudo mv /home/vagrant/config.json /etc/consul.d/server/config.json
sudo chown root:consul /etc/consul.d/server/config.json

# Reload systemd, then enable and start Consul
sudo systemctl daemon-reload
if systemctl is-enabled --quiet consul; then
  systemctl reenable consul
else
  systemctl enable consul
fi

if systemctl is-active --quiet consul; then
  systemctl restart consul
else
  systemctl start consul
fi
