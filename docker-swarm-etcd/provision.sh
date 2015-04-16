#!/bin/bash

# Define a couple variables for easier future modifications
VERS="v2.0.9"
FNAME="etcd-$VERS-linux-amd64.tar.gz"
URL="https://github.com/coreos/etcd/releases/download/$VERS/$FNAME"

# Install curl if needed
if [[ ! -e /usr/bin/curl ]]; then
  apt-get update
  apt-get -yqq install curl
fi

# Install etcd if not already present
if [[ ! -e /usr/local/bin/etcd ]]; then

  # Download the etcd release from GitHub
  curl -L $URL -o $FNAME

  # Expand the download
  tar xzvf $FNAME

  # Move etcd and etcdctl to /usr/local/bin
  cd etcd-v2.0.9-linux-amd64
  sudo mv etcd /usr/local/bin/
  sudo mv etcdctl /usr/local/bin/
  cd ..

  # Remove etcd download and directory
  rm $FNAME
  rm -rf etcd-v2.0.9-linux-amd64
fi

  # Create etcd data directory if not already present
if [[ ! -d /var/etcd ]]; then
  sudo mkdir /var/etcd
fi

# Copy files into the correct locations; requires shared folders
sudo cp /vagrant/etcd.conf /etc/init/etcd.conf
sudo cp /vagrant/$HOSTNAME.override /etc/init/etcd.override
