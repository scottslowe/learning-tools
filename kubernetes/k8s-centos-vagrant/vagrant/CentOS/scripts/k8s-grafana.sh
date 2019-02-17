#!/bin/bash
# https://github.com/kubernetes/heapster/blob/master/docs/influxdb.md 

echo "Executing ${0}"

echo "================================================================"
echo 
echo "Installing Grafana Monitoring Dashboard:"
echo "  - influxdb"
echo "  - heapster rbac"
echo 
echo "================================================================"

IPADDR=$1

yum install -y git

cd /tmp/
git clone https://github.com/kubernetes/heapster.git
cd heapster

kubectl create -f deploy/kube-config/influxdb/
kubectl create -f deploy/kube-config/rbac/heapster-rbac.yaml

# Change ClusterIP to NodePort
kubectl patch svc monitoring-grafana -n kube-system --patch '{"spec": {"type": "NodePort"}}'
kubectl get svc -n kube-system monitoring-grafana -ojson \
    | jq '.spec.ports[].nodePort | tostring | "Grafana URL: http://'${IPADDR}':" + .'