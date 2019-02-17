# Stateful Services on Kubernetes

## Deploy NFS Server
NFS is a protocol that allows nodes to read/write data over a network. The protocol works by having a master node running the NFS daemon and stores the data. This master node makes certain directories available over the network.

Clients access the masters shared via drive mounts. From the viewpoint of applications, they are writing to the local disk. Under the covers, the NFS protocol writes it to the master.

### Task
In this scenario, and for demonstration and learning purposes, the role of the NFS Server is handled by a customised container. The container makes directories available via NFS and stores the data inside the container. In production, it is recommended to configure a dedicated NFS Server.

Start the NFS using the command 
```
$ docker run -d --net=host \
   --privileged --name nfs-server \
   katacoda/contained-nfs-server:centos7 \
   /exports/data-0001 /exports/data-0002
```

The NFS server exposes two directories, data-0001 and data-0002. In the next steps, this is used to store data.

## Deploy Persistent Volume
For Kubernetes to understand the available NFS shares, it requires a PersistentVolume configuration. The PersistentVolume supports different protocols for storing data, such as AWS EBS volumes, GCE storage, OpenStack Cinder, Glusterfs and NFS. The configuration provides an abstraction between storage and API allowing for a consistent experience.

In the case of NFS, one PersistentVolume relates to one NFS directory. When a container has finished with the volume, the data can either be Retained for future use or the volume can be Recycled meaning all the data is deleted. The policy is defined by the persistentVolumeReclaimPolicy option.

For structure is:
```
apiVersion: v1
kind: PersistentVolume
metadata:
  name: <friendly-name>
spec:
  capacity:
    storage: 1Gi
  accessModes:
    - ReadWriteOnce
    - ReadWriteMany
  persistentVolumeReclaimPolicy: Recycle
  nfs:
    server: <server-name>
    path: <shared-path>
```

The spec defines additional metadata about the persistent volume, including how much space is available and if it has read/write access.

### Task
Create two new PersistentVolume definitions to point at the two available NFS shares.

```
$ kubectl create -f nfs-0001.yaml
persistentvolume "nfs-0001" created
$ kubectl create -f nfs-0002.yaml
persistentvolume "nfs-0002" created
```

Once created, view all PersistentVolumes in the cluster using 
```
$ kubectl get pv
NAME       CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS      CLAIM     STORAGECLASS   REASON    AGE
nfs-0001   2Gi        RWO,RWX        Recycle          Available                                      7s
nfs-0002   5Gi        RWO,RWX        Recycle          Available                                      5s
```

## Deploy Persistent Volume Claim
Once a Persistent Volume is available, applications can claim the volume for their use. The claim is designed to stop applications accidentally writing to the same volume and causing conflicts and data corruption.

The claim specifies the requirements for a volume. This includes read/write access and storage space required. An example is as follows:

```
kind: PersistentVolumeClaim
apiVersion: v1
metadata:
  name: claim-mysql
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 3Gi
```

### Task
Create two claims for two different applications. A MySQL Pod will use one claim, the other used by an HTTP server.

```
$ kubectl create -f pvc-mysql.yaml
persistentvolumeclaim "claim-mysql" created
$ kubectl create -f pvc-http.yaml
persistentvolumeclaim "claim-http" created
```

View the contents of the files using `cat pvc-mysql.yaml pvc-http.yaml`

Once created, view all PersistentVolumesClaims in the cluster using:

```
$ kubectl get pvc
NAME          STATUS    VOLUME     CAPACITY   ACCESS MODES   STORAGECLASS   AGE
claim-http    Bound     nfs-0001   2Gi        RWO,RWX                       33s
claim-mysql   Bound     nfs-0002   5Gi        RWO,RWX                       39s
```

The claim will output which Volume the claim is mapped to.

## Use Volume
When a deployment is defined, it can assign itself to a previous claim. The following snippet defines a volume mount for the directory /var/lib/mysql/data which is mapped to the storage mysql-persistent-storage. The storage called mysql-persistent-storage is mapped to the claim called claim-mysql.

```
  spec:
      volumeMounts:
        - name: mysql-persistent-storage
          mountPath: /var/lib/mysql/data
  volumes:
    - name: mysql-persistent-storage
      persistentVolumeClaim:
        claimName: claim-mysql
```

### Task
Launch two new Pods with Persistent Volume Claims. Volumes are mapped to the correct directory when the Pods start allowing applications to read/write as if it was a local directory.

```
$ kubectl create -f pod-mysql.yaml
pod "mysql" created
$ kubectl create -f pod-www.yaml
pod "www" created
```

You can see the status of the Pods starting using `kubectl get pods`

If a Persistent Volume Claim is not assigned to a Persistent Volume, then the Pod will be in Pending mode until it becomes available. In the next step, we'll read/write data to the volume.

## Read/Write Data
Our Pods can now read/write. MySQL will store all database changes to the NFS Server while the HTTP Server will serve static from the NFS drive. When upgrading, restarting or moving containers to a different machine the data will still be accessible.

To test the HTTP server, write a 'Hello World' index.html homepage. In this scenario, we know the HTTP directory will be based on data-0001 as the volume definition hasn't driven enough space to satisfy the MySQL size requirement.

```
$ docker exec -it nfs-server bash -c "echo 'Hello World' > /exports/data-0001/index.html"
$ ip=$(kubectl get pod www -o yaml |grep podIP | awk '{split($0,a,":"); print a[2]}'); echo $ip
$ curl $ip
Hello World
```

### Update Data
When the data on the NFS share changes, then the Pod will read the newly updated data.

```
$ docker exec -it nfs-server bash -c "echo 'Hello NFS World' > /exports/data-0001/index.html"
$ curl $ip
Hello NFS World
```

## Recreate Pod
Because a remote NFS server stores the data, if the Pod or the Host were to go down, then the data will still be available.

### Task
Deleting a Pod will cause it to remove claims to any persistent volumes. New Pods can pick up and re-use the NFS share.

```
$ kubectl delete pod www
$ kubectl create -f pod-www2.yaml
ip=$(kubectl get pod www2 -o yaml |grep podIP | awk '{split($0,a,":"); print a[2]}'); curl $ip
```

The applications now use a remote NFS for their data storage. Depending on requirements, this same approach works with other storage engines such as GlusterHQ, AWS EBS, GCE storage or OpenStack Cinder.