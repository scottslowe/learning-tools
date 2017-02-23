#!/bin/bash

# Provision secondary network interface
sudo ip addr add 192.168.100.101/24 dev ens33
sudo ip link set ens33 up

# Install curl if needed
if [[ ! -e /usr/bin/curl ]]; then
  apt-get update
  apt-get -yqq install curl
fi

if [[ ! -e /usr/local/bin/docker-latest ]]; then
  # Get download of experimental Docker binary
  curl -sLO https://experimental.docker.com/builds/Linux/x86_64/docker-latest

  # Set properties on Docker binary
  chmod a+x docker-latest

  # Move etcd and etcdctl to /usr/local/bin
  sudo mv docker-latest /usr/local/bin/

  # Stop existing Docker daemon
  sudo systemctl stop docker

  # Substitute experimental Docker binary for existing version
  sudo mv /usr/bin/docker /usr/bin/docker-old
  sudo ln -s /usr/local/bin/docker-latest /usr/bin/docker

  # Restart Docker service
  sudo systemctl start docker
fi
