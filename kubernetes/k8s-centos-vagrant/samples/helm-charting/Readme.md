## Generate Your First Chart
The best way to get started with a new chart is to use the helm create command to scaffold out an example we can build on. Use this command to create a new chart named mychart in a new directory:

```
$ helm create mychart
```

Helm will create a new directory in your project called mychart with the structure shown below. Let’s navigate our new chart (pun intended) to find out how it works.
```
$ tree mychart
mychart/
├── Chart.yaml
├── charts
├── templates
│   ├── NOTES.txt
│   ├── _helpers.tpl
│   ├── deployment.yaml
│   ├── ingress.yaml
│   └── service.yaml
└── values.yaml
```

## Dry Run a Chart
We can do a dry-run of a helm install and enable debug to inspect the generated definitions:
```
$ helm install --dry-run --debug ./mychart
...
# Source: mychart/templates/service.yaml
apiVersion: v1
kind: Service
metadata:
  name: inky-mule-mychart
  labels:
    app: mychart
    chart: mychart-0.1.0
    release: inky-mule
    heritage: Tiller
spec:
  type: ClusterIP
  ports:
    - port: 80
      targetPort: http
      protocol: TCP
      name: http
  selector:
    app: mychart
    release: inky-mule
...
```

If a user of your chart wanted to change the default configuration, they could provide overrides directly on the command-line:
```
helm install --dry-run --debug ./mychart --set service.internalPort=8080
```

## Chart Deployment
The chart you generated in the previous step is setup to run an NGINX server exposed via a Kubernetes Service. By default, the chart will create a ClusterIP type Service, so NGINX will only be exposed internally in the cluster. To access it externally, we’ll use the NodePort type instead. We can also set the name of the Helm release so we can easily refer back to it. Let’s go ahead and deploy our NGINX chart using the helm install command:

```
$ helm install --name example ./mychart --set service.type=NodePort
NAME:   example
LAST DEPLOYED: Sun Dec 30 10:46:25 2018
NAMESPACE: default
STATUS: DEPLOYED

RESOURCES:
==> v1/Service
NAME             TYPE      CLUSTER-IP     EXTERNAL-IP  PORT(S)       AGE
example-mychart  NodePort  10.104.211.85  <none>       80:32554/TCP  0s

==> v1beta2/Deployment
NAME             DESIRED  CURRENT  UP-TO-DATE  AVAILABLE  AGE
example-mychart  1        0        0           0          0s

==> v1/Pod(related)
NAME                              READY  STATUS             RESTARTS  AGE
example-mychart-645b79d79d-fwqq7  0/1    ContainerCreating  0         0s


NOTES:
1. Get the application URL by running these commands:
  export NODE_PORT=$(kubectl get --namespace default -o jsonpath="{.spec.ports[0].nodePort}" services example-mychart)
  export NODE_IP=$(kubectl get nodes --namespace default -o jsonpath="{.items[0].status.addresses[0].address}")
  echo http://$NODE_IP:$NODE_PORT
```

## Modify Chart To Deploy A Custom Service



## Updating Chart

```
$ helm update example ./mychart
```

## Packaging Chart
```
$ helm package ./mychart
```

Helm will create a mychart-0.1.0.tgz package in our working directory, using the name and version from the metadata defined in the Chart.yaml file. A user can install from this package instead of a local directory by passing the package as the parameter to helm install.

```
helm install --name example2 mychart-0.1.0.tgz --set service.type=NodePort
```

