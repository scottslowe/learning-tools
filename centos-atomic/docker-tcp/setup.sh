#!/usr/bin/env bash

# Stop docker.service
sudo systemctl stop docker.service

# Make drop-in directory
if [ ! -d "/etc/systemd/system/docker.service.d" ]; then
    sudo mkdir -p /etc/systemd/system/docker.service.d
fi

# Copy docker.socket into place
if [ -f "/home/vagrant/sync/docker.socket" ]; then
    sudo cp /home/vagrant/sync/docker.socket /etc/systemd/system/
fi

# Copy docker-tcp.socket into place
if [ -f "/home/vagrant/sync/docker-tcp.socket" ]; then
    sudo cp /home/vagrant/sync/docker-tcp.socket /etc/systemd/system/
fi

# Copy docker.service drop-in into place
if [ -f "/home/vagrant/sync/docker-socket.conf" ]; then
    sudo cp /home/vagrant/sync/docker-socket.conf /etc/systemd/system/docker.service.d/
fi

# Add docker group
sudo groupadd docker

# Reload
sudo systemctl daemon-reload

# Restart services
sudo systemctl start docker.socket
sudo systemctl start docker-tcp.socket
sudo systemctl start docker.service
