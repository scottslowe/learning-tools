#!/bin/bash

cat <<END
Executing ${0}
================================================================================

    Installing Ingress Controller Istio

    https://istio.io/docs/setup/kubernetes/quick-start/

================================================================================

END

HOMEDIR=/opt
ISTIO_VERSION="1.0.2"

cd ${HOMEDIR} && curl https://raw.githubusercontent.com/istio/istio/master/downloadIstio.sh | sh -
kubectl apply -f ${HOMEDIR}/istio-${ISTIO_VERSION}/install/kubernetes/helm/istio/templates/crds.yaml
kubectl apply -f ${HOMEDIR}/istio-${ISTIO_VERSION}/install/kubernetes/istio-demo.yaml

# Wait until Istio is Up
    while kubectl get pods -n istio-system | grep ContainerCreating >/dev/null;
    do
        echo $(date +"[%H:%M:%S]") Istio is not ready yet
        sleep 30
    done

echo $(date +"[%H:%M:%S]") Istio has been installed
kubectl get pods -n istio-system
