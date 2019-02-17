# Local Kubernetes Environment on Vagrant/Virtualbox and CentOS 7

## Kubernetes Architecture
![Kubernetes High-Level Diagram](images/basic-architecture.png)
![Kubernetes Architecture](images/architecture.png)

## HOW TO
- [Config File](config.rb)
- [Vagrantfile](vagrant/)

```
$ make up
$ make load
$ make whoup
$ make ssh
$ make down
$ make destroy

# K8s Dashboard
$ kubectl proxy

# K8s bash completion
$ source <(kubectl completion bash)
 
$ kubectl get nodes
...
```

## Tested on:
- VirtualBox 5.2.6
- Vagrant 2.0.1
- CentOS 7.4
- Docker 1.12.6
- Kubernetes 1.9.2
- Flannel 0.9.1

## Supported Configurations
- Master + Workers (worker_count > 0)
- Master Isolation (worker_count = 0)

## TODO List

### Fundamental
- [x] Kubernetes Dashboard
- [x] Grafana + InfluxDB
- [x] Ingress Controller

### Configuration Samples and Keynotes
- [x] Application (Deployment, Service, Ingress Rules)
- [x] Networking (ClusterIP, ExternalIP, NodePort)
- [x] Users
- [ ] Secrets
- [ ] ConfigMap
- [ ] Storages / GlusterFS
- [ ] RBAC
- [ ] Helm Tiller
- [x] Prometheus

## Documentation References

### Kubernetes Architecture
- https://github.com/kubernetes/community/blob/master/contributors/design-proposals/architecture/architecture.md
- https://github.com/kubernetes/kubernetes/blob/release-1.5/docs/design/architecture.md
- https://github.com/kubernetes/kubernetes/blob/release-1.5/docs/design/clustering.md
- https://kubernetes.io/docs/concepts/overview/components/
- https://kubernetes.io/docs/concepts/architecture/cloud-controller/

### Basic Installation and Configuration
- https://kubernetes.io/docs/setup/independent/install-kubeadm/
- https://kubernetes.io/docs/setup/independent/create-cluster-kubeadm/
- https://kubernetes.io/docs/setup/independent/troubleshooting-kubeadm/

### A Pod Network Addon (CNI)
- [Flannel](https://github.com/coreos/flannel)
- [Flannel Vagrant Settings](https://github.com/coreos/flannel/blob/master/Documentation/troubleshooting.md#vagrant)
- [Weave](https://www.weave.works/docs/net/latest/kubernetes/kube-addon/)

### Kubernetes Objects (Kinds) and Operations
Primitives:

- [Pod](https://kubernetes.io/docs/concepts/workloads/pods/pod-overview/)
- [Assigning Pods to Nodes](https://kubernetes.io/docs/concepts/configuration/assign-pod-node/)

Controllers:

- [ReplicationController](https://kubernetes.io/docs/concepts/workloads/controllers/replicationcontroller/)
- [ReplicaSet](https://kubernetes.io/docs/concepts/workloads/controllers/replicaset/)
- [HorizontalPodAutoscaler](https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale/)
- [Deployment](https://kubernetes.io/docs/concepts/workloads/controllers/deployment/)
- [StatefulSet](https://kubernetes.io/docs/concepts/workloads/controllers/statefulset/)
- [DaemonSet](https://kubernetes.io/docs/concepts/workloads/controllers/daemonset/)

Volumes:

- [PersistentVolume](https://kubernetes.io/docs/concepts/storage/persistent-volumes/)
- [PersistentVolumeClaim](https://kubernetes.io/docs/concepts/storage/persistent-volumes/#persistentvolumeclaims)
- [PV/PVC Walkthrough](https://kubernetes.io/docs/tasks/configure-pod-container/configure-persistent-volume-storage/)

### Kubernetes Dashboard
- https://github.com/kubernetes/dashboard
- https://github.com/kubernetes/dashboard/wiki/Creating-sample-user

### Grafana and InfluxDB
- https://github.com/kubernetes/heapster
- https://github.com/kubernetes/heapster/blob/master/docs/influxdb.md
- https://kubernetes.io/docs/tasks/run-application/update-api-object-kubectl-patch/

### Ingress Controller
- https://kubernetes.io/docs/concepts/services-networking/ingress/
- https://github.com/kubernetes/ingress-gce/blob/master/BETA_LIMITATIONS.md#glbc-beta-limitations
- https://github.com/kubernetes/ingress-nginx/tree/master/deploy
- https://github.com/nginxinc/kubernetes-ingress/blob/master/examples/multiple-ingress-controllers/README.md

### Kubernetes Storage
- https://kubernetes.io/docs/concepts/storage/volumes/
- https://kubernetes.io/docs/concepts/storage/persistent-volumes/

### Kubernetes Tutorials
- https://kubernetes.io/docs/tutorials/
- http://katacoda.com/courses/kubernetes
- https://www.edx.org/course/introduction-kubernetes-linuxfoundationx-lfs158x?gclid=EAIaIQobChMI06mUq96K2QIViKMYCh0B6wl9EAMYASAAEgKRFfD_BwE

### Other
- https://kubernetes.io/docs/reference/kubectl/cheatsheet/
- https://github.com/Praqma/LearnKubernetes/blob/master/kamran/Kubernetes-kubectl-cheat-sheet.md
- http://design.jboss.org/redhatdeveloper/marketing/kubernetes_cheatsheet/cheatsheet/cheat_sheet/images/kubernetes_cheat_sheet_r1v1.pdf
- https://kubernetes.io/docs/tasks/access-kubernetes-api/http-proxy-access-api/
- http://hyperpolyglot.org/json

## Fixes & Work Arounds
- [VirtualBox NAT Interface](vagrant/CentOS/Vagrantfile#L21)
- [VirtualBox CPU Usage](vagrant/CentOS/Vagrantfile#L22)
- [Flannel + VirtualBox](configs/kube-flannel.yaml#L111)

## Kubectl Cheatsheet
```
$ kubectl cluster-info
$ kubectl cluster-info dump

$ kubectl get nodes
$ kubectl get pods
$ kubectl get rc
$ kubectl get deploy
$ kubectl get rs
$ kubectl rollout status deploy DEPLOY_NAME

$ kubectl run nginx-deployment --image=nginx --port=80
$ kubectl expose deployment nginx-deployment --port=80 --type=NodePort 

$ kubectl expose deployment nginx-deployment --external-ip="192.168.56.150" --port=8000 --target-port=80
$ kubectl get svc nginx-deployment 
$ kubectl get svc nginx-deployment -o go-template='{{range.spec.ports}}{{if .nodePort}}{{.nodePort}}{{"\n"}}{{end}}{{end}}'
$ kubectl get deployment nginx
$ kubectl describe deployment nginx-deployment

$ kubectl run nginx-deployment --image=nginx --port=80 --replicas=1
$ kubectl run nginx-deployment --image=nginx --port=80 --replicas=1 --hostport=8001
$ kubectl scale --replicas=10 deployment nginx-deployment

$ kubeadm init --token=102952.1a7dd4cc8d1f4cc5 --kubernetes-version v1.8.0
$ kubeadm token list
$ kubeadm join --token 102952.1a7dd4cc8d1f4cc5 172.17.0.43:6443

$ kubectl apply -f https://git.io/weave-kube
$ kubectl apply -f "https://cloud.weave.works/k8s/net?k8s-version=$(kubectl version | base64 | tr -d '\n')"
$ kubectl get pod -n kube-system

$ kubectl exec -n ingress-nginx $(kubectl get pods -n ingress-nginx | grep controller | awk '{print $1}') cat /etc/nginx/nginx.conf
$ kubectl logs -n ingress-nginx $(kubectl get pods -n ingress-nginx | grep controller | awk '{print $1}')


$ kubectl run cent --image=sbeliakou/centos --command sleep 3600
$ dig @10.96.0.10 grafana.monitoring.svc.cluster.local

```