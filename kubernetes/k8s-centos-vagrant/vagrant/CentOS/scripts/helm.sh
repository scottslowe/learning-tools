#!/bin/bash

cat <<END
Executing ${0}
================================================================================

    Installing HELM

================================================================================

END

kubectl create clusterrolebinding add-on-cluster-admin --clusterrole=cluster-admin --serviceaccount=kube-system:default

export HELM_RELEASE=2.12.1
echo "Helm Version to be Installed: ${HELM_RELEASE}"
echo
wget -qO- https://storage.googleapis.com/kubernetes-helm/helm-v${HELM_RELEASE}-linux-amd64.tar.gz | tar xvz linux-amd64/helm --to-stdout > /usr/bin/helm
chmod a+x /usr/bin/helm

helm init
while [[ -z "$(helm version | grep Server)" ]]
do
	sleep 10
done

helm version