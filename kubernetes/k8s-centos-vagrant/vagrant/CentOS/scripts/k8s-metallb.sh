#!/bin/bash

cat <<END
Executing ${0}
================================================================================

    https://metallb.universe.tf/installation/

    Installing Metal LB

================================================================================

END

IP_RANGE=${1}

kubectl apply -f https://raw.githubusercontent.com/google/metallb/v0.7.3/manifests/metallb.yaml
echo

cat << EOF | kubectl apply -f -
apiVersion: v1
kind: ConfigMap
metadata:
  namespace: metallb-system
  name: config
data:
  config: |
    address-pools:
    - name: default
      protocol: layer2
      addresses:
      - ${IP_RANGE}
EOF