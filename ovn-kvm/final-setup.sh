#!/bin/bash

# Get VM interface's iface-id (assumes VM's interface is "vnet0")
IFACE_ID=$(sudo ovs-vsctl get interface vnet0 external_ids:iface-id | sed s/\"//g)

# Get VM interface's MAC address (assumes VM's interface is "vnet0")
MAC_ADDR=$(sudo ovs-vsctl get interface vnet0 external_ids:attached-mac | sed s/\"//g)

# Create a new logical port on logical switch
# Logical port name *must* match VM interface's iface-id value
sudo ovn-nbctl --db=tcp:192.168.100.101:6641 lsp-add demo $IFACE_ID

# Set MAC address for new logical switch port
sudo ovn-nbctl --db=tcp:192.168.100.101:6641 lsp-set-addresses $IFACE_ID $MAC_ADDR
