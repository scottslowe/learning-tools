#!/bin/bash

# Download rkt from GitHub
wget https://github.com/coreos/rkt/releases/download/v0.5.5/rkt-v0.5.5.tar.gz

# Unpack the rkt files and move to final destination
tar xzvf rkt-v0.5.5.tar.gz
cd rkt-v0.5.5
sudo mv rkt /usr/local/bin/
sudo mv stage1.aci /usr/local/bin/
rm -rf rkt-v0.5.5

# Create a rkt user and group
sudo useradd -M -d /var/lib/rkt -r -s /usr/bin/nologin rkt

# Add default vagrant user to rkt group
sudo usermod -G rkt -a vagrant

# Set up directories for rkt
sudo mkdir -p /var/lib/rkt
sudo chown -R rkt:rkt /var/lib/rkt
sudo chmod -R 0775 /var/lib/rkt
sudo chmod -R g+s /var/lib/rkt
sudo mkdir -p /etc/rkt
sudo chown -R rkt:rkt /etc/rkt
sudo chmod -R 0775 /etc/rkt
sudo chmod -R g+s /etc/rkt
