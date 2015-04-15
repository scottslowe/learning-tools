#!/bin/bash

# Get etcd download
curl -L  https://github.com/coreos/etcd/releases/download/v2.0.9/etcd-v2.0.9-linux-amd64.tar.gz -o etcd-v2.0.9-linux-amd64.tar.gz

# Expand the download
tar xzvf etcd-v2.0.9-linux-amd64.tar.gz

# Move etcd and etcdctl to /usr/local/bin
cd etcd-v2.0.9-linux-amd64
sudo mv etcd /usr/local/bin/
sudo mv etcdctl /usr/local/bin/
cd ..

# Remove etcd download and directory
rm etcd-v2.0.9-linux-amd64.tar.gz
rm -rf etcd-v2.0.9-linux-amd64

# Create directories needed by etcd
sudo mkdir /var/etcd

# Copy files into the correct locations; requires shared folders
sudo cp /vagrant/etcd.conf /etc/init/etcd.conf
sudo cp /vagrant/$HOSTNAME.override /etc/init/etcd.override
