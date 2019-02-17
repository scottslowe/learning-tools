#!/bin/bash

set -e

# These are hardcoded in the kubelet start up script.
CLUSTER_DNS="10.96.0.10"
CLUSTER_DOMAIN="cluster.local"

## Add kubeadm config and KUBE_HYPERKUBE_IMAGE	``
# etcd endpoints in the form... scheme://ip:port
# apiVersion: kubeadm.k8s.io/v1alpha1
# kind: MasterConfiguration
# api:
#   advertiseAddresses:
#   - <address1|string>
#   - <address2|string>
#   bindPort: <int>
#   externalDNSNames:
#   - <dnsname1|string>
#   - <dnsname2|string>
# cloudProvider: <string>
# discovery:
#   bindPort: <int>
# etcd:
#   endpoints:
#   - <endpoint1|string>
#   - <endpoint2|string>
#   caFile: <path|string>
#   certFile: <path|string>
#   keyFile: <path|string>
# kubernetesVersion: <string>
# networking:
#   dnsDomain: <string>
#   serviceSubnet: <cidr>
#   podSubnet: <cidr>
# secrets:
#   givenToken: <token|string>

add_cluster_service_label() {
	local MANIFEST=$1

	# make sure has the kubernetes.io/cluster-service: 'true' label for addon manager to pick up
	if ! grep -q "^    kubernetes.io/cluster-service" /etc/kubernetes/addons/${MANIFEST}; then
		sed -e 's/^  labels:/  labels:\n    kubernetes.io\/cluster-service: "true"/' -i /etc/kubernetes/addons/${MANIFEST}
	fi
	if ! grep -q "^  labels:" /etc/kubernetes/addons/${MANIFEST}; then
		sed -e 's/^metadata:/metadata:\n  labels:\n    kubernetes.io\/cluster-service: "true"/' -i /etc/kubernetes/addons/${MANIFEST}
	fi
}

install_addon() {
	local URL=$1
	local MAN=$2

	echo "Downloading addon $URL..."
	curl -k -L -s ${URL} >/etc/kubernetes/addons/${MAN}
	#separate multiple objects in one 1 file into multiple manifests
	if grep -q "^---" /etc/kubernetes/addons/${MAN}; then
		local NUM_OBJECTS=$(cat /etc/kubernetes/addons/${MAN}  | grep "^---" | wc -l)

		# loop over the objects creating separate files file1.yaml file2.yaml
		local l=0
		while [ $l -le $NUM_OBJECTS ]
		do  
			local NEW_NAME=${MAN/%.yaml/$l.yaml}
			echo "Creating new addon manifest ${NEW_NAME}" 
			cat /etc/kubernetes/addons/${MAN} | awk 'BEGIN{count=0} ($0 ~ /^---/){count++;next} ( count == '$l' ){print}' >/etc/kubernetes/addons/${NEW_NAME}
			add_cluster_service_label "${NEW_NAME}"
			l=$((l+=1))
		done
		rm -f /etc/kubernetes/addons/${MAN}
	else
		add_cluster_service_label ${MAN}
	fi
}

# install the cluster with kubeadm
mkdir -p /etc/kubernetes
cat <<EOT >/etc/kubernetes/kubeadm.conf
apiVersion: kubeadm.k8s.io/v1alpha1
kind: MasterConfiguration
api:
  advertiseAddresses:
  - ${MY_IP}
  bindPort: 6443
  externalDNSNames:
  - $(hostname)
etcd:
  endpoints:
$(for ep in ${ETCD_ENDPOINTS}; do echo -e "  - ${ep}"; done)
kubernetesVersion: ${KUBERNETES_VERSION} 
secrets:
  givenToken: ${KUBE_TOKEN}
EOT

echo "Starting kubelet..."
rc-update add kubelet
rc-service kubelet start

echo "Running kubeadm init to configure kubernetes..."
export KUBE_HYPERKUBE_IMAGE="gcr.io/google_containers/hyperkube:${KUBERNETES_VERSION}"
echo "MY_IP is ${MY_IP}"
echo "cluster_token is ${KUBE_TOKEN}"
echo ""
echo "Kubeadm settings: -"
cat /etc/kubernetes/kubeadm.conf
echo ""
echo "Running: kubeadm"
kubeadm init --config /etc/kubernetes/kubeadm.conf | tee /root/kubeadm_init.log

#copy kubeconfig for root's usage
mkdir -p /root/.kube
cp /etc/kubernetes/admin.conf /root/.kube/config

echo "Patching the apiserver manifest to advertise the master on the right address..."
sed -e 's/"--allow-privileged",/"--allow-privileged","--advertise-address='${MY_IP}'",/' -i /etc/kubernetes/manifests/kube-apiserver.json
echo "Set the right number of master servers..."
sed -e 's/"--v=2",/"--v=2", "--apiserver-count='${MASTER_COUNT}'",/' -i /etc/kubernetes/manifests/kube-apiserver.json

echo "Preparing Addons..."
mkdir -p /etc/kubernetes/addons

echo "Preparing SkyDNS as addon"
install_addon https://raw.githubusercontent.com/kubernetes/kubernetes/master/cluster/addons/dns/kubedns-controller.yaml.sed kubedns-controller.yaml
sed -e 's/\$DNS_DOMAIN\.?/'${CLUSTER_DOMAIN}'/g' -i /etc/kubernetes/addons/kubedns-controller.yaml
install_addon https://raw.githubusercontent.com/kubernetes/kubernetes/master/cluster/addons/dns/kubedns-svc.yaml.sed kubedns-svc.yaml
sed -e 's/\$DNS_SERVER/'${CLUSTER_DNS}'/g' -i /etc/kubernetes/addons/kubedns-svc.yaml

echo "Preparing canal as addon"
install_addon https://raw.githubusercontent.com/tigera/canal/master/k8s-install/kubeadm/canal.yaml canal.yaml
# change the interface to eth1
sed -e 's/canal_iface: ""/canal_iface: "eth1"/' -i /etc/kubernetes/addons/canal0.yaml

echo "Preparing Kubernetes Dashboard as addon"
install_addon https://rawgit.com/kubernetes/dashboard/master/src/deploy/kubernetes-dashboard.yaml kubernetes-dashboard.yaml

echo "Adding NGINX ingress addon"
#install_addon https://raw.githubusercontent.com/kubernetes/contrib/master/ingress/controllers/nginx/examples/daemonset/as-daemonset.yaml nginx-ingress.yaml
install_addon https://raw.githubusercontent.com/containous/traefik/master/examples/k8s/traefik.yaml ingress-traefik.yaml
install_addon https://raw.githubusercontent.com/containous/traefik/master/examples/k8s/ui.yaml ingress-ui.yaml
sed -e 's/traefik-ui.local/ingress-ui.k8s.local/g' -i /etc/kubernetes/addons/ingress-ui2.yaml

#echo "Preparing Heapster, InfluxDB and Grafana as addons"
#for MANIFEST in heapster-deployment.yaml heapster-service.yaml grafana-deployment.yaml grafana-service.yaml influxdb-deployment.yaml influxdb-service.yaml
#do
#  install_addon "https://raw.githubusercontent.com/kubernetes/heapster/master/deploy/kube-config/influxdb/${MANIFEST}" "${MANIFEST}"
#done

# Install the addon manager as a direct kubelet manifest
echo "Installing Addon Manager - to install/manage addons"
curl -k -L -s https://raw.githubusercontent.com/kubernetes/kubernetes/master/cluster/saltbase/salt/kube-addons/kube-addon-manager.yaml >/etc/kubernetes/manifests/addon-manager.yaml

