# Target Port
Target ports allows us to separate the port the service is available on from the port the application is listening on. TargetPort is the Port which the application is configured to listen on. Port is how the application will be accessed from the outside.

Similar to previously, the service and extra pods are deployed via `kubectl apply -f clusterip-target.yaml`

The following commands will the service.
```
cat clusterip-target.yaml

kubectl get svc
kubectl describe svc/webapp1-clusterip-targetport-svc
```

After the service and pods have deployed, it can be accessed via the cluster IP as before, but this time on the defined port 8080.

```
export CLUSTER_IP=$(kubectl get services/webapp1-clusterip-targetport-svc -o go-template='{{(index .spec.clusterIP)}}')
echo CLUSTER_IP=$CLUSTER_IP
curl $CLUSTER_IP:8080
curl $CLUSTER_IP:8080
```

The application itself is still configured to listen on port 80. Kubernetes Service manages the translation between the two.