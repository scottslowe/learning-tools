#!/bin/bash

# get latest version of the kubectl command...
if [[ -z "${KUBERNETES_VERSION}" ]]; then
  KUBERNETES_VERSION=$(curl -s -k -L https://storage.googleapis.com/kubernetes-release/release/stable.txt)
fi

BINNAME="kubectl"
case $(uname -s) in
   Darwin)
     echo "kubectl for Mac OS X"
     CODEBASE="darwin"
     ;;
   Linux)
     echo "kubectl for Linux"
     CODEBASE="linux"	
     ;;
   CYGWIN*|MINGW32*|MSYS*)
     echo "kubectl for Windows/Cygwin"
     CODEBASE="windows"	
     BINNAME="kubectl.exe"
     ;;
   *)
     echo "Sorry! Can't detect the OS... you will need to manually download."
     exit 1 
     ;;
esac

if [ ! -f "${BINNAME}" ]; then
  echo "downloading https://storage.googleapis.com/kubernetes-release/release/${KUBERNETES_VERSION}/bin/${CODEBASE}/amd64/${BINNAME}" 
  curl -L -k https://storage.googleapis.com/kubernetes-release/release/${KUBERNETES_VERSION}/bin/${CODEBASE}/amd64/${BINNAME} >${BINNAME}
  chmod a+x ${BINNAME}
fi

# resolve windows paths or leave as is
#resolve_path() {
#  local PATH=$1
#  case ${CODEBASE} in
#    windows) echo "$(/usr/bin/cygpath -w $PATH)"
#      ;;
#    *) echo "$PATH"
#      ;;
#  esac
#}

if [ -f "cluster-token" ]; then 
  echo "Setting up kubeconfig file admin.config: -"
  CLUSTER_TOKEN=$(cat cluster-token)
  # the second half of the cluster token can be used as a user token!!!
  # this behaviour will change in the future.
  TOKEN=${CLUSTER_TOKEN##*.}
  ./${BINNAME} --kubeconfig=admin.config config set-cluster master1 --insecure-skip-tls-verify=true --server=https://10.250.250.2:6443 --api-version=v1
  ./${BINNAME} --kubeconfig=admin.config config set-credentials admin --token $TOKEN
  ./${BINNAME} --kubeconfig=admin.config config set-context default --cluster master1 --user admin
  ./${BINNAME} --kubeconfig=admin.config config use-context default
fi

echo ""
echo "Checking cluster.. you should see the nodes listed below: -"
./${BINNAME} --kubeconfig=admin.config get nodes
