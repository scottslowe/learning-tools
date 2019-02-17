#!/bin/bash

cat <<END
Executing ${0}
================================================================================

    https://github.com/nginxinc/kubernetes-ingress/blob/master/docs/installation.md
    https://kubernetes.github.io/ingress-nginx/deploy/#bare-metal
    
    Installing Ingress Controller

================================================================================

END

kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/master/deploy/namespace.yaml
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/master/deploy/mandatory.yaml
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/master/deploy/provider/baremetal/service-nodeport.yaml

if [ "${1}" != "true" ]; then
    MASTER_NAME=$(kubectl get nodes | grep master | head -1 | cut -d' ' -f1)
    IPADDR=$(kubectl get node ${MASTER_NAME} -o go-template --template '{{range .status.addresses}}{{if (eq .type "InternalIP")}}{{.address}}{{end}}{{end}}')
    kubectl patch svc ingress-nginx -n ingress-nginx --patch '{ "spec": {"externalIPs": [ "'${IPADDR}'" ] }}'
else
    kubectl patch -n ingress-nginx svc ingress-nginx --patch '{"spec": {"type": "LoadBalancer"}}'
fi

# # Wait until Ingress Controller is Ready
# phase=$(kubectl get pod $(kubectl get pod -n ingress-nginx | grep nginx-ingress-controller | cut -d' ' -f1) -n ingress-nginx -o template --template='{{.status.phase}}')
# while [ "${phase}" == "Pending" ]
# do
#   phase=$(kubectl get pod $(kubectl get pod -n ingress-nginx | grep nginx-ingress-controller | cut -d' ' -f1) -n ingress-nginx -o template --template='{{.status.phase}}')
#   echo $(date +"[%H:%M:%S]") Nginx Ingress Controller not Ready
#   sleep 10
# done

kubectl get pods --all-namespaces -l app.kubernetes.io/name=ingress-nginx