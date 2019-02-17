#!/bin/bash

# https://github.com/kubernetes/dashboard
# https://github.com/kubernetes/dashboard/wiki/Creating-sample-user

cat <<END
Executing ${0}
================================================================================

    https://github.com/kubernetes/dashboard
    https://github.com/kubernetes/dashboard/wiki/Creating-sample-user
    
    Installing Dashboard:
      - dashboard
      - admin user

================================================================================

END


dash=$(kubectl get deployments --all-namespaces | grep kubernetes-dashboard >/dev/null; echo $?)
if [ $dash -ne 0 ]; then 
    echo "Deploying Dashboard"
    # kubectl create -f https://raw.githubusercontent.com/kubernetes/dashboard/master/src/deploy/recommended/kubernetes-dashboard.yaml
    echo Get More Details: https://github.com/kubernetes/dashboard#kubernetes-dashboard
    kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v1.10.1/src/deploy/recommended/kubernetes-dashboard.yaml
    # Add to Cluster Info
    kubectl patch svc -n kube-system kubernetes-dashboard --patch='{"metadata": {"labels": {"kubernetes.io/cluster-service": "true"}}}'
    
    cat << EOF | kubectl apply -f -
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: admin-user
  namespace: kube-system
---
apiVersion: rbac.authorization.k8s.io/v1beta1
kind: ClusterRoleBinding
metadata:
  name: admin-user
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: cluster-admin
subjects:
- kind: ServiceAccount
  name: admin-user
  namespace: kube-system
EOF
fi

kubectl -n kube-system describe secret $(kubectl -n kube-system get secret | grep admin-user | awk '{print $1}')

# kubectl --kubeconfig .kube/config proxy --address='0.0.0.0' --accept-hosts='^*$'

cat << EOF
For Accessing Kub Dashboard:
  1. kubectl --kubeconfig .kube/config proxy
  2. In browser go to: http://localhost:8001/api/v1/namespaces/kube-system/services/https:kubernetes-dashboard:/proxy/
  3. Sign in with above mentioned token
    
EOF
