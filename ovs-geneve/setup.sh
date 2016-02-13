#!/bin/bash

# Add new network namespace if it doesn't already exist
# Delete existing network namespace named "testns"
if [ -e "/var/run/netns/testns" ]; then
	sudo ip netns del testns
	echo "Existing network namespace removed"
fi

# Create new "testns" namespace
sudo ip netns add testns
echo "New network namespace named \"testns\" created"

# Create new veth pair, deleting existing veth pair if necessary
# If 'veth0' already exists, then delete it
if [ -e "/sysclass/net/veth0" ]; then
	sudo ip link del veth0 type veth
	echo "Existing veth pair removed"
fi

# Create new veth pair
sudo ip link add veth0 type veth peer name veth1
echo "New veth pair created"

# Get last octet of IP address of VM for use later
IP=$(grep 'address' /etc/network/interfaces | tr "." " " | awk '{print $5'})
#echo $IP
NS_IP="10.1.1.$IP/24"
#echo $NS_IP

# Set up network namespace
# Move veth1 to testns if it still exists in primary namespace
if [ -e /sys/class/net/veth1 ]; then
	sudo ip link set veth1 netns testns
	echo "Moved veth1 to new network namespace"
fi

# Add IP address to veth1 if IP address isn't already there
if [ `sudo ip netns exec testns ip addr list | grep 'inet ' | wc -l` -eq 0 ]; then
	sudo ip netns exec testns ip addr add $NS_IP dev veth1
	echo "IP address $NS_IP assigned to veth1 in new network namespace"
fi

# Set the veth pair to up
sudo ip netns exec testns ip link set veth1 up
sudo ip link set veth0 up
