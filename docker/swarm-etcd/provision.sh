#!/bin/bash

# Define a couple variables for easier future modifications
VERS="v2.3.7"
BASENAME="etcd-$VERS-linux-amd64"
FNAME="$BASENAME.tar.gz"
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
  cd $BASENAME
  sudo mv etcd /usr/local/bin/
  sudo mv etcdctl /usr/local/bin/
  cd ..

  # Remove etcd download and directory
  rm $FNAME
  rm -rf $BASENAME
fi

  # Create etcd data directory if not already present
if [[ ! -d /var/etcd ]]; then
  sudo mkdir /var/etcd
fi

# Copy files into the correct locations; requires shared folders
sudo cp /home/vagrant/etcd.conf /etc/init/etcd.conf
sudo cp /home/vagrant/$HOSTNAME.defaults /etc/default/etcd

# Restart if already running; otherwise, start etcd.
sudo initctl status etcd && initctl restart etcd || initctl start etcd
