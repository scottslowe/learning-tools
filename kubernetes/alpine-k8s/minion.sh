#!/bin/bash

set -e

# This script sets up Etcd, Flannel and Kubernetes Master
# for a single master and 3 minions configuration.

echo "Starting kubelet..."
rc-update add kubelet
rc-service kubelet start

echo "Running kubeadm join to configure kubernetes..."
echo "cluster_token is ${KUBE_TOKEN}"
echo kubeadm join --token "${KUBE_TOKEN}" ${MASTER_LB_IP}
kubeadm join --token "${KUBE_TOKEN}" ${MASTER_LB_IP}

#copy kubeconfig for root's usage
mkdir -p /root/.kube
cp /etc/kubernetes/kubelet.conf /root/.kube/config

rc-service kubelet restart


