# External IPs
Another approach to making a service available outside of the cluster is via External IP addresses.

Update the definition to the current cluster's IP address with `sed -i 's/HOSTIP/172.17.0.73/g' externalip.yaml`

```
$ cat externalip.yaml

$ kubectl apply -f externalip.yaml
deployment "webapp1-externalip-deployment" created

$ kubectl get svc

$ kubectl describe svc/webapp1-externalip-svc
```

The service is now bound to the IP address and Port 80 of the master node.

```
$ curl 172.17.0.73
```
