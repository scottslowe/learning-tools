#!/bin/bash

# Define Libvirt network from XML
sudo virsh net-define /home/vagrant/ovs.xml
sudo virsh net-autostart ovs
sudo virsh net-start ovs

# Turn off default network
sudo virsh net-autostart --disable default
sudo virsh net-destroy default
