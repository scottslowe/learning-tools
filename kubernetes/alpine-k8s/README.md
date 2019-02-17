# Kubernetes on Alpine

Components: -
* Alpine Linux
* Docker
* Kubernetes (hyperkube, kubeadm and cni)
* Canal (Calico/Flannel) networking

This is an experimental project with the goal of creating the latest kubernetes clusters using the super small (at 246Mb including docker and kubernetes binaries this is as small as we can hope to get) and secure [Alpine Linux](https://www.alpinelinux.org/) distribution as a base running on Vagrant.  This image is small and simple so you can test out a full kubernetes environment on your laptop without wasting gobs of disk space.

My aim is to set up our cluster __the easy way__ but should this prove to be too restrictive then I'll have to consider [the hard way](https://github.com/kelseyhightower/kubernetes-the-hard-way).

## Bringing up the kubernetes cluster

```
git clone https://github.com/davidmccormick/alpine-k8s
cd alpine-k8s
vagrant box add dmcc/alpine-3.4.5-docker-1.12.3-kubernetes-v1.4.4
vagrant up
```

## Download and configure 'kubectl' to access your cluster.
You can vagrant ssh onto your master and use kubectl to adminster your cluster, but it is more convenient to download kubectl and configure for your host machine.  Run the script 'download_kubectl.sh" to download and configure a kubeconfig file admin.config for accessing your cluster.  e.g.

```
$ ./download_kubectl.sh 
kubectl for Linux
downloading https://storage.googleapis.com/kubernetes-release/release/v1.5.1/bin/linux/amd64/kubectl
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100 48.0M  100 48.0M    0     0  1296k      0  0:00:37  0:00:37 --:--:-- 1392k
Setting up kubeconfig file admin.config: -
Cluster "master1" set.
User "admin" set.
Context "default" set.
Switched to context "default".

Checking cluster.. you should see the nodes listed below: -
NAME                  STATUS         AGE
master1.example.com   Ready,master   3h
minion1.example.com   Ready          3h
```

You can run kubeclt commands like this: -

```
$ ./kubectl --kubeconfig=admin.config get pods --all-namespaces -o wide
NAMESPACE     NAME                                          READY     STATUS    RESTARTS   AGE       IP              NODE
kube-system   calico-policy-controller-wzkgq                1/1       Running   0          3h        10.250.250.10   minion1.example.com
kube-system   canal-etcd-sc7h4                              1/1       Running   0          3h        10.250.250.2    master1.example.com
kube-system   canal-node-h0rq0                              3/3       Running   0          3h        10.250.250.10   minion1.example.com
kube-system   canal-node-rpw02                              3/3       Running   0          3h        10.250.250.2    master1.example.com
kube-system   dummy-2088944543-dffx9                        1/1       Running   0          3h        10.250.250.2    master1.example.com
kube-system   etcd-master1.example.com                      1/1       Running   0          3h        10.250.250.2    master1.example.com
kube-system   heapster-2193675300-pwg37                     1/1       Running   0          3h        192.168.84.3    minion1.example.com
kube-system   kube-addon-manager-master1.example.com        1/1       Running   0          3h        10.250.250.2    master1.example.com
kube-system   kube-apiserver-master1.example.com            1/1       Running   1          3h        10.250.250.2    master1.example.com
kube-system   kube-controller-manager-master1.example.com   1/1       Running   0          3h        10.250.250.2    master1.example.com
kube-system   kube-proxy-fp883                              1/1       Running   0          3h        10.250.250.2    master1.example.com
kube-system   kube-proxy-z4nxb                              1/1       Running   0          3h        10.250.250.10   minion1.example.com
kube-system   kube-scheduler-master1.example.com            1/1       Running   0          3h        10.250.250.2    master1.example.com
kube-system   kubernetes-dashboard-3095304083-89zkz         1/1       Running   0          3h        192.168.84.4    minion1.example.com
kube-system   monitoring-grafana-810108360-77phz            1/1       Running   0          3h        192.168.84.2    minion1.example.com
kube-system   monitoring-influxdb-3065341217-2j9p2          1/1       Running   0          3h        192.168.84.5    minion1.example.com
```

## More information

### What do we get?

We presently install one master (master.example.com) and two minions (minion01/2.example.com).  The installation is performed by kubeadm and so most of the kubernetes components (execept kubectl, kubelet, kubeadm and cni) are downloaded and started up in their own docker containers (this is despite having hyperkube available natively inside the image) - this is because this is how kubeadm wants to work.  Having the binaries available in the image makes it more flexible and you can easily replace my provisioning scripts with your own.

* **Canal (Flannel/Calico)** is installed as an addon and interfaces into the kubelet via cni.
* **SkyDNS** is automatically* configured by kubeadm.
* **kube-dashboard** cluster gui
* **Heapster**, **Grafana** and **InfluxDB** for cluster metrics

### The Alpine-k8s vagrant image 

The job of adding the docker, kubernetes and cni binaries are taken care of wihin the build of the *alpine-x.x.x-docker-x.x.x-kubenetes-vx.x.x* image, which you can download from Atlas or build using the scripts in the **alpine-image** folder (please see the README in this folder about requirements and usage). 

### Cluster token

The cluster token is randomly generated in the Vagrantfile and saved to the file cluster-token.  This is so we can add nodes later to a running cluster.  You can generate a new cluster token by removing the cluster-token file.

### Provisioning Scripts

The **shared.sh** script sets up the networking and makes sure that the kubernetes kubelet is running by adding a cron job to restart it every 1 minute (this job is then removed again once everything is configured and running).

The **master.sh** script runs kubeadm init to set up your cluster and once available it is responsible for installing our addons such as canal networking and dashboard.

The **minion.sh** script runs kubeadm to join the cluster.

### No Rkt

I've chosen **Docker** as the container engine over Rkt, because it is already available for Alpine as an APK package and because it does not require systemd (which Alpine happily does not use).

### Dashboard

The kubernetes dashboard is installed automatically.  To access it you must proxy through a local running kubectl command - set up kubectl by running download_kubectl.sh (as above).  Then..

```
./kubectl --kubeconfig=admin.config proxy
Starting to serve on 127.0.0.1:8001
```

Without breaking the running kubectl browse to http://localhost:8001/ui/ in your web browser. 

## Present Limitations
1. No master HA.
2. No Ingress.
3. No physical volumes.
4. No authentication or quotas.
