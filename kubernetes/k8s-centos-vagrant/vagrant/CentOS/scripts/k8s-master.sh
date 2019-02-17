#!/bin/bash

cat <<END
Executing ${0}
================================================================================

    https://kubernetes.io/docs/setup/independent/create-cluster-kubeadm/

    Configuring Kubernetes Master:
      - cluster init
      - Pod network: Flannel

================================================================================

END

IPADDR=$1
TOKEN=$2

yum install -y etcd

if [ ! -e /etc/kubernetes/kubelet.conf ]; then
    echo kubeadm init --apiserver-advertise-address ${IPADDR} --token ${TOKEN} --pod-network-cidr 10.244.0.0/16
    kubeadm init \
        --pod-network-cidr 10.244.0.0/16 \
        --apiserver-advertise-address ${IPADDR} \
        --token ${TOKEN}

    mkdir -p $HOME/.kube
    /bin/cp -f /etc/kubernetes/admin.conf $HOME/.kube/config
    chown $(id -u):$(id -g) $HOME/.kube/config

    if [ -d /vagrant ]; then
        rm -rf /vagrant/.kube
        /bin/cp -rf $HOME/.kube /vagrant/.kube
    fi

    export KUBECONFIG=/etc/kubernetes/admin.conf

    # Installing a Pod Network
    # kubectl apply -f https://raw.githubusercontent.com/coreos/flannel/v0.9.1/Documentation/kube-flannel.yml
    # https://github.com/coreos/flannel/blob/master/Documentation/troubleshooting.md#vagrant
    IPETHX=$(ip r | grep $(hostname -I | sed 's/10.0.2.15//' | awk '{print $1}') | cut -d' ' -f3)
    kubectl apply -f https://raw.githubusercontent.com/coreos/flannel/bc79dd1505b0c8681ece4de4c0d86c5cd2643275/Documentation/kube-flannel.yml
    kubectl patch daemonsets kube-flannel-ds-amd64 -n kube-system --patch='{"spec":{"template":{"spec":{"containers":[{"name": "kube-flannel", "args": ["--ip-masq", "--kube-subnet-mgr", "--iface='${IPETHX}'"]}]}}}}'

    kubectl get daemonsets -n kube-system kube-flannel-ds-amd64

    # Wait until Master is Up
    while kubectl get nodes | grep master | grep NotReady >/dev/null;
    do
        echo $(date +"[%H:%M:%S]") Master is not ready yet
        sleep 10
    done

    echo $(date +"[%H:%M:%S]") Master is in Ready mode
fi

kubectl get nodes