# Ingress Routing

## Create Deployment
To start, deploy an example HTTP server that will be the target of our requests. The deployment contains three deployments, one called webapp1 and a second called webapp2, and a third called webapp3 with a service for each.

```
$ cat deployment.yaml
```

### Task
Deploy the definitions with 
```
$ kubectl create -f deployment.yaml
deployment "webapp1" created
deployment "webapp2" created
deployment "webapp3" created
service "webapp1-svc" created
service "webapp2-svc" created
service "webapp3-svc" created

$ kubectl get deployment
NAME      DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
webapp1   1         1         1            0           3s
webapp2   1         1         1            0           3s
webapp3   1         1         1            0           3s
```

## Deploy Ingress
Ingress is deployed as a Replication Controller. This controller combines a software load balancer, such as Nginx or HAProxy, with Kubernetes integration for configuring itself based on the defined rules.

The YAML file below defines a nginx-based Ingress controller together with a service making it available on Port 80 to external connections using ExternalIPs. If the Kubernetes cluster was running on a cloud provider then it would use a LoadBalancer service type.

```
$ cat ingress.yaml
```

### Task
The Ingress controllers are deployed in a familiar fashion to other Kubernetes objects with 
```
$ kubectl create -f ingress.yaml
replicationcontroller "nginx-ingress-rc" created
service "nginx-ingress-lb" created
```

The status can be identified using 
```
$ kubectl get rc
NAME               DESIRED   CURRENT   READY     AGE
nginx-ingress-rc   1         1         1         14s
```

## Deploy Ingress Rules
Ingress rules are an object type with Kubernetes. The rules can be based on a request host (domain), or the path of the request, or a combination of both.

An example set of rules are defined within cat ingress-rules.yaml

The important parts of the rules are defined below.

The rules apply to requests for the host my.kubernetes.example. Two rules are defined based on the path request with a single catch all definition. Requests to the path /webapp1 are forwarded onto the service webapp1-svc. Likewise, the requests to /webapp2 are forwarded to webapp2-svc. If no rules apply, webapp3-svc will be used.

This demonstrates how an application's URL structure can behave independently about how the applications are deployed.

```
- host: my.kubernetes.example
  http:
    paths:
    - path: /webapp1
      backend:
        serviceName: webapp1-svc
        servicePort: 80
    - path: /webapp2
      backend:
        serviceName: webapp2-svc
        servicePort: 80
    - backend:
        serviceName: webapp3-svc
        servicePort: 80
```

### Task
As with all Kubernetes objects, they can be deployed via 
```
$ kubectl create -f ingress-rules.yaml
ingress "webapp-ingress" created
```

Once deployed, the status of all the Ingress rules can be discovered via
```
$ kubectl get ing
NAME             HOSTS                   ADDRESS   PORTS     AGE
webapp-ingress   my.kubernetes.example             80        27s

$ kubectl describe ingress webapp-ingress
Name:             webapp-ingress
Namespace:        default
Address:
Default backend:  default-http-backend:80 (<none>)
Rules:
  Host                   Path  Backends
  ----                   ----  --------
  my.kubernetes.example
                         /webapp1   webapp1-svc:80 (<none>)
                         /webapp2   webapp2-svc:80 (<none>)
                                    webapp3-svc:80 (<none>)
Annotations:
Events:  <none>
```

## Test
With the Ingress rules applied, the traffic will be routed to the defined place.

```
# The first request will be processed by the webapp1 deployment.
curl -H "Host: my.kubernetes.example" 172.17.0.21/webapp1

# The second request will be processed by the webapp2 deployment.
curl -H "Host: my.kubernetes.example" 172.17.0.21/webapp2

# Finally, all other requests will be processed by webapp3 deployment.
curl -H "Host: my.kubernetes.example" 172.17.0.21
```