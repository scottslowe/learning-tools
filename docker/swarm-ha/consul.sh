#!/bin/bash
PLATFORM='linux_amd64'

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
  IFS=' ' CURL_OUT=$(curl https://releases.hashicorp.com/consul/)
  CONSUL_VERSION=$(echo ${CURL_OUT} | grep href | grep consul | head -n 1 | awk -F'/' '{print $3}')
  curl -kL https://releases.hashicorp.com/consul/$CONSUL_VERSION/consul_${CONSUL_VERSION}_${PLATFORM}.zip > consul.zip

  # Decompress and remove Consul download
  unzip consul.zip
  rm consul.zip

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

# Move files into the correct locations
sudo mv /home/vagrant/consul.conf /etc/init/consul.conf
sudo chown root:root /etc/init/consul.conf
sudo mv /home/vagrant/config.json /etc/consul.d/server/config.json
sudo chown root:consul /etc/consul.d/server/config.json

# Start Consul
sudo service consul start
