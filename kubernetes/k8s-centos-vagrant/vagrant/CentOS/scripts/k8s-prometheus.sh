#!/bin/bash

bash /vagrant/vagrant/CentOS/scripts/helm.sh

cat <<END
================================================================================

    https://github.com/coreos/prometheus-operator/tree/master/helm

    Deploying Prometheus:
        - prometheus-operator
        - kube-prometheus

================================================================================
END

helm repo add coreos https://s3-eu-west-1.amazonaws.com/coreos-charts/stable/
helm upgrade --install --namespace monitoring prometheus-operator coreos/prometheus-operator
helm upgrade --install --namespace monitoring kube-prometheus coreos/kube-prometheus

## TODO Ing