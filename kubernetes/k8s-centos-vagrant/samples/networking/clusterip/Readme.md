# ClusterIP

By having a single IP address it enables the service to be load balanced across multiple Pods.

Services are deployed via `kubectl apply -f clusterip.yaml`.

The definition can be viewed at `cat clusterip.yaml`

This will deploy a web app with two replicas to showcase load balancing along with a service. The Pods can be viewed at kubectl get pods

It will also deploy a service. kubectl get svc

More details on the service configuration and active endpoints (Pods) can be viewed via `kubectl describe svc/webapp1-clusterip-svc`

After deploying, the service can be accessed via the ClusterIP allocated.

```
export CLUSTER_IP=$(kubectl get services/webapp1-clusterip-svc -o go-template='{{(index .spec.clusterIP)}}')
echo CLUSTER_IP=$CLUSTER_IP
curl $CLUSTER_IP:80
```

Multiple requests will showcase how the service load balancers across multiple Pods based on the common label selector.

`curl $CLUSTER_IP:80`