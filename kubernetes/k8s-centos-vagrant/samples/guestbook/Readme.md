# Deploy Guestbook example on Kubernete

The first stage of launching the application is to start the Redis Master. A Kubernetes service deployment has, at least, two parts. A replication controller and a service.

The replication controller defines how many instances should be running, the Docker Image to use, and a name to identify the service. Additional options can be utilized for configuration and discovery. Use the editor above to view the YAML definition.

If Redis were to go down, the replication controller would restart it on an active node.

## Replication Controller
In this example, the YAML defines a redis server called redis-master using the official redis running port 6379.

The kubectl create command takes a YAML definition and instructs the master to start the controller.

```
$ kubectl create -f redis-master-controller.yaml
```

### What's running?
The above command created a Replication Controller. The Replication

```
$ kubectl get rc
```

All containers described as Pods. A pod is a collection of containers that makes up a particular application, for example Redis. You can view this using kubectl

```
$ kubectl get pods
```

If you're interested in what's launching then you can still use the docker client like before.

```
$ docker ps | grep redis
```

## Redis Master Service
The second part is a service. A Kubernetes service is a named load balancer that proxies traffic to one or more containers. The proxy works even if the containers are on different nodes.

Services proxy communicate within the cluster and rarely expose ports to an outside interface.

When you launch a service it looks like you cannot connect using curl or netcat unless you start it as part of Kubernetes. The recommended approach is to have a LoadBalancer service to handle external communications.

### Create Service
The YAML defines the name of the replication controller, redis-master, and the ports which should be proxied.

```
$ kubectl create -f redis-master-service.yaml
$ kubectl get services
$ kubectl describe services redis-master
```

## Replication Slave Pods
In this example we'll be running Redis Slaves which will have replicated data from the master. More details of Redis replication can be found at http://redis.io/topics/replication

As previous described, the controller defines how the service runs. In this example we need to determine how the service discovers the other pods. The YAML represents the `_GET_HOSTSFROM` property as DNS. You can change it to use Environment variables in the yaml but introduces creation-order dependencies as the service needs to be running for the environment variable to be defined.

### Start Redis Slave Controller
In this case, we'll be launching two instances of the pod using the image `kubernetes/redis-slave:v2`. It will link to redis-master via DNS.

```
$ kubectl create -f redis-slave-controller.yaml
$ kubectl get rc
```

## Redis Slave Service
As before we need to make our slaves accessible to incoming requests. This is done by starting a service which knows how to communicate with redis-slave.

Because we have two replicated pods the service will also provide load balancing between the two nodes.

### Start Redis Slave Service
```
$ kubectl create -f redis-slave-service.yaml
$ kubectl get services
```

## Frontend Replicated Pods
With the data services started we can now deploy the web application. The pattern of deploying a web application is the same as the pods we've deployed before.

### Launch Frontend
The YAML defines a service called frontend that uses the image kubernetes/example-guestbook-php-redis:v2. The replication controller will ensure that three pods will always exist.

```
$ kubectl create -f frontend-controller.yaml
$ kubectl get rc
$ kubectl get pods
```

### PHP Code
The PHP code uses HTTP and JSON to communicate with Redis. When setting a value requests go to redis-master while read data comes from the redis-slave nodes.

## Frontend Replicated Pods
With the data services started we can now deploy the web application. The pattern of deploying a web application is the same as the pods we've deployed before.

## Launch Frontend
The YAML defines a service called frontend that uses the image kubernetes/example-guestbook-php-redis:v2. The replication controller will ensure that three pods will always exist.

```
$ kubectl create -f frontend-controller.yaml
$ kubectl get rc
$ kubectl get pods
```

### PHP Code
The PHP code uses HTTP and JSON to communicate with Redis. When setting a value requests go to redis-master while read data comes from the redis-slave nodes.

## Guestbook Frontend Service
To make the frontend accessible we need to start a service to configure the proxy.

### Start Proxy
The YAML defines the service as a NodePort. NodePort allows you to set well-known ports that are shared across you entire cluster. This is like -p 80:80 in Docker.

In this case, we define our web app is running on port 80 but we'll expose the service on 30080.

```
$ kubectl create -f frontend-service.yaml
$ kubectl get services
```

We'll discuss NodePort in future scenarios.

## Access Guestbook Frontend
With all controllers and services defined Kubernetes will start launching them as Pods. A pod can have different states depending on what's happening. For example, if the Docker Image is still being downloaded then the Pod will have a pending state as it's not able to launch. Once ready the status will change to running.

### View Pods Status
You can view the status using the following command:

```
$ kubectl get pods
```

### Find NodePort
If you didn't assign a well-known NodePort then Kubernetes will assign an available port randomly. You can find the assigned NodePort using kubectl.

```
$ kubectl describe service frontend | grep NodePort
```

### View UI
Once the Pod is in running state you will be able to view the UI via port 30080. 

Under the covers the PHP service is discovering the Redis instances via DNS. You now have a working multi-tier application deployed on Kubernetes.

