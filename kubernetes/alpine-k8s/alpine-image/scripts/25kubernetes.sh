set -eux

echo "Installing Kubeadm dependencies..."
apk add ebtables ethtool socat iproute2 iptables

echo "Using kubeadm binary from local http server"
chmod +x /usr/local/bin/kubectl
chmod +x /usr/local/bin/kubeadm

#echo "Installing CNI"
#mkdir -p /opt/cni /etc/kubernetes/manifests /etc/cni/net.d
#cd /opt/cni
#tar xvfpz /tmp/cni.tar.gz
#rm -f /tmp/cni.tar.gz

echo "Pre-loading the hyperkube image gcr.io/google_containers/hyperkube:${KUBERNETES_VERSION}..."
service docker start
sleep 2
ls -al /var/run/docker.sock
cat /var/log/docker.log
ps -ef | grep docker
docker pull gcr.io/google_containers/hyperkube:${KUBERNETES_VERSION} 
docker pull gcr.io/google_containers/hyperkube-amd64:${KUBERNETES_VERSION}
service docker stop

echo "Setup shared /var/lib/kubelet bind mount service..."
cat >/etc/init.d/kubelet_bind_mount <<EOT
#!/sbin/openrc-run
# Copyright 1999-2013 Gentoo Foundation
# Distributed under the terms of the GNU General Public License v2
# \$Header: \$

depend() {
  need sysfs
}

start_pre() {
  return 0
}

start() {
  ebegin "Creating /var/lib/kubelet shared mount"
  mkdir -p /var/lib/kubelet && \
  mount --bind /var/lib/kubelet /var/lib/kubelet && \
  mount --make-shared /var/lib/kubelet 
  eend \$?
}

stop() {
   ebegin "Umounting shared bind volume /var/lib/kubelet"
   umount /var/lib/kubelet
   eend \$?
}
EOT
chmod +x /etc/init.d/kubelet_bind_mount
rc-update add kubelet_bind_mount boot

echo "Create kubelet service..."
cat >/etc/init.d/kubelet <<EOT
#!/sbin/openrc-run
# Copyright 1999-2013 Gentoo Foundation
# Distributed under the terms of the GNU General Public License v2
# \$Header: \$

depend() {
  need net
  need docker
  need sysfs
  need kubelet_bind_mount
}

start_pre() {
  ulimit -n 1048576
  return 0
}

start() {
  ebegin "Starting Kubelet"
  #start-stop-daemon --background --start --exec /usr/local/bin/kubelet --make-pidfile --pidfile /run/kubelet.pid --stdout /var/log/kubelet.log --stderr /var/log/kubelet.log    -- --require-kubeconfig --kubeconfig=/etc/kubernetes/kubelet.conf --pod-manifest-path=/etc/kubernetes/manifests --allow-privileged=true --network-plugin=cni --cni-conf-dir=/etc/cni/net.d --cni-bin-dir=/opt/cni/bin --cluster-dns=10.96.0.10 --cluster-domain=cluster.local --v=4 --hostname-override=MY_HOSTNAME --node-ip=MY_IPADDRESS
  MY_HOSTNAME=\`hostname\`
  MY_IP=\`nslookup \$MY_HOSTNAME 2>&1 | grep '^Address' | awk '{print \$3}'\`
  /usr/bin/docker run -d --restart=on-failure --name kubelet \
       --volume=/:/rootfs:ro \
       --volume=/sys:/sys:ro \
       --volume=/dev:/dev \
       --volume=/var/lib/docker/:/var/lib/docker:rw \
       --volume=/var/lib/kubelet/:/var/lib/kubelet:shared \
       --volume=/etc:/etc:rw \
       --volume=/var/run:/var/run:rw \
       --net=host --pid=host --privileged=true \
       gcr.io/google_containers/hyperkube:${KUBERNETES_VERSION} \
       /hyperkube kubelet \
        --containerized \
        --address="0.0.0.0" \
        --require-kubeconfig --kubeconfig=/etc/kubernetes/kubelet.conf \
        --pod-manifest-path=/etc/kubernetes/manifests \
        --allow-privileged=true \
        --network-plugin=cni \
        --cni-conf-dir=/etc/cni/net.d \
        --cni-bin-dir=/opt/cni/bin \
        --cluster-dns=10.96.0.10 \
        --cluster-domain=cluster.local \
        --v=4 \
        --hostname-override=\$MY_HOSTNAME \
        --node-ip=\$MY_IP
  eend \$?
}

stop() {
   ebegin "Stopping Kubelet"
   /usr/bin/docker stop kubelet && /usr/bin/docker rm kubelet
   eend \$?
}
EOT
chmod +x /etc/init.d/kubelet

exit 0

