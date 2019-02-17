# Load Balancer
When running in the cloud, such as EC2 or Azure, it's possible to configure and assign a Public IP address issued via the cloud provider. This will be issued via a Load Balancer such as ELB. This allows additional public IP addresses to be allocated to a Kubernetes cluster without interacting directly with the cloud provider.

As Katacoda is not a cloud provider, it's still possible to dynamically allocate IP addresses to LoadBalancer type services. This is done by deploying the Cloud Provider using `kubectl apply -f cloudprovider.yaml`. When running in a service provided by a Cloud Provider this is not required.

When a service requests a Load Balancer, the provider will allocate one from the 10.10.0.0/26 range defined in the configuration.

```
$ kubectl apply -f cloudprovider.yaml
daemonset "kube-keepalived-vip" created
configmap "vip-configmap" created
deployment "keepalived-cloud-provider" created

$ kubectl get pods -n kube-system

$ kubectl apply -f loadbalancer.yaml
service "webapp1-loadbalancer-svc" created
deployment "webapp1-loadbalancer-deployment" created
```

The service is configured via a Load Balancer as defined in cat loadbalancer.yaml

While the IP address is being defined, the service will show Pending. When allocated, it will appear in the service list.

```
$ kubectl get svc

$ kubectl describe svc/webapp1-loadbalancer-svc
```

The service can now be accessed via the IP address assigned, in this case from the 10.10.0.0/26 range.

```
export LoadBalancerIP=$(kubectl get services/webapp1-loadbalancer-svc -o go-template='{{(index .status.loadBalancer.ingress 0).ip}}')
echo LoadBalancerIP=$LoadBalancerIP
curl $LoadBalancerIP

curl $LoadBalancerIP
```