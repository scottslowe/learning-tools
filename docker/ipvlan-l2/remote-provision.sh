#!/bin/bash

# Provision secondary network interface
sudo ip addr add 192.168.100.101/24 dev eth1
sudo ip link set eth1 up
