## Typical Application Configuration


```
    internet -> [ ingress-controller ] -> [ service ] -> [ pods ]
```

```
$ kubectl apply -f /vagrant/samples/application/application.yaml

$ kubectl get ing
NAME                      HOSTS                                ADDRESS   PORTS     AGE
web-application-ingress   web-application.kubernetes.example             80        29m

$ kubectl describe ing web-application-ingress
Name:             web-application-ingress
Namespace:        default
Address:
Default backend:  default-http-backend:80 (<none>)
Rules:
  Host                                Path  Backends
  ----                                ----  --------
  web-application.kubernetes.example
                                      /   web-application-svc:80 (<none>)
Annotations:
Events:
  Type    Reason  Age   From                      Message
  ----    ------  ----  ----                      -------
  Normal  CREATE  29m   nginx-ingress-controller  Ingress default/web-application-ingress
  Normal  UPDATE  28m   nginx-ingress-controller  Ingress default/web-application-ingress


$ kubectl get svc web-application-svc
NAME                  TYPE       CLUSTER-IP       EXTERNAL-IP   PORT(S)        AGE
web-application-svc   NodePort   10.103.226.204   <none>        80:30080/TCP   1h

$ kubectl get pods -n ingress-nginx
NAME                                       READY     STATUS    RESTARTS   AGE
default-http-backend-55c6c69b88-n7h4h      1/1       Running   1          2d
nginx-ingress-controller-f8bdfcb68-xmkn5   1/1       Running   0          21m

$ kubectl exec -n ingress-nginx $(kubectl get pods -n ingress-nginx | grep nginx-ingress-controller | awk '{print $1}') cat /etc/nginx/nginx.conf

$ kubectl logs -n ingress-nginx $(kubectl get pods -n ingress-nginx | grep nginx-ingress-controller | awk '{print $1}')

$ curl -H "Host: web-application.kubernetes.example" 192.168.56.150
```

```
kubectl apply -f nginx.yaml --record
kubectl get deploy
kubectl rollout status deploy nginx
kubectl get rs
kubectl get pods -o wide
kubectl describe deploy/nginx
```

## Sample Stateful Application

Stack: [nginx-pv](nginx-pv.yaml)

```
$ kubectl get nodes --show-labels
NAME           STATUS    ROLES     AGE       VERSION   LABELS
k8s-master     Ready     master    20h       v1.9.2    beta.kubernetes.io/arch=amd64,beta.kubernetes.io/os=linux,kubernetes.io/hostname=k8s-master,node-role.kubernetes.io/master=
k8s-worker-1   Ready     <none>    20h       v1.9.2    beta.kubernetes.io/arch=amd64,beta.kubernetes.io/os=linux,kubernetes.io/hostname=k8s-worker-1,team=mtp,tool=jenkins
k8s-worker-2   Ready     <none>    20h       v1.9.2    beta.kubernetes.io/arch=amd64,beta.kubernetes.io/os=linux,kubernetes.io/hostname=k8s-worker-2

$ kubectl apply -f samples/application/nginx-pv.yaml
namespace "devops-ns" created
persistentvolume "web-application-pv" created
persistentvolumeclaim "web-application-pvc" created
deployment "web-application-deployment" created
service "web-application-svc" created
ingress "web-application-ingress" created

$ kubectl rollout status deployment web-application-deployment -n devops-ns
Waiting for rollout to finish: 0 of 1 updated replicas are available...
deployment "web-application-deployment" successfully rolled out

$ curl -H "Host: web-application.kubernetes.example" 192.168.56.150
Hello from Kubernetes storage (init container)

$ kubectl describe pod $(kubectl get po -n devops-ns | awk '/web-application/{print $1}') -n devops-ns
Name:           web-application-deployment-7447679f7b-tjjxv
Namespace:      devops-ns
Node:           k8s-worker-1/10.0.2.15
Start Time:     Tue, 06 Feb 2018 20:27:35 +0300
Labels:         app=web-application
                pod-template-hash=3003235936
Annotations:    <none>
Status:         Running
IP:             10.244.1.21
Controlled By:  ReplicaSet/web-application-deployment-7447679f7b
Init Containers:
  web-application-container-init:
    Container ID:  docker://10638d707c243066ba867747f2cb331249d07772daed6d1f25a9042c78dd3610
    Image:         busybox
    Image ID:      docker-pullable://docker.io/busybox@sha256:1669a6aa7350e1cdd28f972ddad5aceba2912f589f19a090ac75b7083da748db
    Port:          <none>
    Command:
      sh
      -c
      echo 'Hello from Kubernetes storage (init container)' > /usr/share/nginx/html/index.html
    State:          Terminated
      Reason:       Completed
      Exit Code:    0
      Started:      Tue, 06 Feb 2018 20:27:43 +0300
      Finished:     Tue, 06 Feb 2018 20:27:44 +0300
    Ready:          True
    Restart Count:  0
    Environment:    <none>
    Mounts:
      /usr/share/nginx/html from web-application-storage (rw)
      /var/run/secrets/kubernetes.io/serviceaccount from default-token-mmtqt (ro)
Containers:
  web-application-container:
    Container ID:   docker://7ebfabd75290d24c85efaf5535c9b935653a655cd54c336533e8bdce2ba1597c
    Image:          nginx:latest
    Image ID:       docker-pullable://docker.io/nginx@sha256:285b49d42c703fdf257d1e2422765c4ba9d3e37768d6ea83d7fe2043dad6e63d
    Port:           80/TCP
    State:          Running
      Started:      Tue, 06 Feb 2018 20:27:49 +0300
    Ready:          True
    Restart Count:  0
    Environment:    <none>
    Mounts:
      /usr/share/nginx/html from web-application-storage (rw)
      /var/run/secrets/kubernetes.io/serviceaccount from default-token-mmtqt (ro)
Conditions:
  Type           Status
  Initialized    True
  Ready          True
  PodScheduled   True
Volumes:
  web-application-storage:
    Type:       PersistentVolumeClaim (a reference to a PersistentVolumeClaim in the same namespace)
    ClaimName:  web-application-pvc
    ReadOnly:   false
  default-token-mmtqt:
    Type:        Secret (a volume populated by a Secret)
    SecretName:  default-token-mmtqt
    Optional:    false
QoS Class:       BestEffort
Node-Selectors:  team=mtp
                 tool=jenkins
Tolerations:     node.kubernetes.io/not-ready:NoExecute for 300s
                 node.kubernetes.io/unreachable:NoExecute for 300s
Events:
  Type    Reason                 Age   From                   Message
  ----    ------                 ----  ----                   -------
  Normal  Scheduled              18m   default-scheduler      Successfully assigned web-application-deployment-7447679f7b-tjjxv to k8s-worker-1
  Normal  SuccessfulMountVolume  18m   kubelet, k8s-worker-1  MountVolume.SetUp succeeded for volume "web-application-pv"
  Normal  SuccessfulMountVolume  17m   kubelet, k8s-worker-1  MountVolume.SetUp succeeded for volume "default-token-mmtqt"
  Normal  Pulling                17m   kubelet, k8s-worker-1  pulling image "busybox"
  Normal  Pulled                 17m   kubelet, k8s-worker-1  Successfully pulled image "busybox"
  Normal  Created                17m   kubelet, k8s-worker-1  Created container
  Normal  Started                17m   kubelet, k8s-worker-1  Started container
  Normal  Pulling                17m   kubelet, k8s-worker-1  pulling image "nginx:latest"
  Normal  Pulled                 17m   kubelet, k8s-worker-1  Successfully pulled image "nginx:latest"
  Normal  Created                17m   kubelet, k8s-worker-1  Created container
  Normal  Started                17m   kubelet, k8s-worker-1  Started container
```