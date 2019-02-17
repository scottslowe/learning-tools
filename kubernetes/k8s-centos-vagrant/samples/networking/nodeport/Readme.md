# NodePort
While TargetPort and ClusterIP make it available to inside the cluster, the NodePort exposes the service on each Node’s IP via the defined static port. No matter which Node within the cluster is accessed, the service will be reachable based on the port number defined.

```
$ kubectl apply -f nodeport.yaml
service "webapp1-nodeport-svc" created
deployment "webapp1-nodeport-deployment" created
```

When viewing the service definition, notice the additional type and NodePort property defined 
```
$ cat nodeport.yaml

$ kubectl get svc
$ kubectl describe svc/webapp1-nodeport-svc
```

The service can now be reached via the Node's IP address on the NodePort defined.

```
$ curl 172.17.0.73:30080
```