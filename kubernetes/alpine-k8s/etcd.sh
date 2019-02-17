#!/bin/bash

set -e

# This script sets up Etcd

echo "Running etcd in a container..."
mkdir -p /var/etcd
docker run -d --network host -e ETCD_NAME=${ETCD_NAME} -e ETCD_ADVERTISE_CLIENT_URLS=${ETCD_ADVERTISE_CLIENT_URLS} -e ETCD_LISTEN_CLIENT_URLS=${ETCD_LISTEN_CLIENT_URLS} -e ETCD_INITIAL_ADVERTISE_PEER_URLS=${ETCD_INITIAL_ADVERTISE_PEER_URLS} -e ETCD_LISTEN_PEER_URLS=${ETCD_LISTEN_PEER_URLS} -e ETCD_INITIAL_CLUSTER=${ETCD_INITIAL_CLUSTER} -e ETCD_DATA_DIR=/var/etcd -p 4001:4001 -p 2380:2380 -p 2379:2379 --name etcd quay.io/coreos/etcd

