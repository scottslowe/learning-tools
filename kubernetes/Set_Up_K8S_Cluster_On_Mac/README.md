# Install A Multi-node Kubernetes Cluster with Vagrant and VirtualBox on Mac

In the article, we will describe how to set up a full Kubernetes cluster on a MacPro laptop. The Mac we are using is at version 10.12.5. 

The steps are sourced from multiple articles to set up K8S cluster on a typical MacProc laptop with Vagrant & CoreOS, which has corrections to the errors/mismatches in the original artiticles and chains them together to be a full set of practical detailed steps to 
set up a Kubernetes cluster. 

At the high level, there are three steps during the process: 

    Section A. Use Vagrant to create four CoreOS VMs on a MacPro laptop. 

    Section B. On those 4 CoreOS VMs, set up a Kubernetes cluster step by step with 
      i. one etcd server
      ii. one k8s master node
      iii. Two k8s worker nodes (more can be added afterwards)
  
      We choose to install K8s cluster step by step so that it can give you deeper understanding about the moving parts and we can 
      benefit more in the long run. 
   
    Section C. Set up the k8s example of guestbook on the newly created k8s cluster. 


## Section A. SET UP 4 COREOS VMs WITH VAGRANT ON Mac

The steps in this section are mainly sourced from  https://coreos.com/os/docs/latest/booting-on-vagrant.html. However some steps in that article are not consistent with the correpsonding artifects. As a consequence, we have corrected some steps according to the README.mod file clones from the corresponding GitHub repository and CoreOS common practice. 

Vagrant is a simple-to-use command line virtual machine manager. By working with VirtualBox (or VMWare which requires to purchase plug-in), it virtually turns your MacPro laptop into a datacentre.

Before we start the steps, we assume the Oracle VirtualBox and Vagrant have already been installed on the Mac laptop. VirtualBox for Mac can be downloaded from https://www.virtualbox.org/wiki/Downloads and Vagrant binary for Mac can be downloaded from https://www.vagrantup.com/downloads.html. 

In our setting up, we will have 4 VMs and 1 of them will be running etcd server and the other three will be etcd clients/proxies. So we use the URL of  https://discovery.etcd.io/new?size=1 (please note size is 1 here) to request a new etcd discovery token and using vagrant to create and boot the first VM of core-01 first which will become the etcd server member (only 1 member in the etcd cluster). Then we use vagrant to create/boot another 3 VMs with the same disco token which will be become etcd proxies to connect to core-01. As a result, 1 VM will be dedicated to etcd server and the other 3 VMs will be etcd clients and installed with Kubernetes components. As per k8s best practice, it's strongly recommanded to have VMs dedicated to etcd cluster separate to the VMs used in K8S cluster. In our case, the etcd cluster is composed of 1 VM only to save resources of the Mac. 

### Step A1. Clone the CoreOS-Vagrant repository from GitHub

On the MacPro laptop, clone the Vagrant + CoreOS repository released by CoreOS. 
```
MacBook-Pro:~ jaswang$ mkdir k8s
MacBook-Pro:~ jaswang$ cd k8s/
MacBook-Pro:k8s jaswang$ git clone https://github.com/coreos/coreos-vagrant.git
```
### Step A2. Request a new etcd discovery token

In our setting up, we will have 4 VMs and 1 of them will be running etcd server and the other three will be etcd clients. So we use the URL of  https://discovery.etcd.io/new?size=1 (please note size is 1 here) to request a new etcd discovery token and the token will be applied to all 4 VMs to be created. As a result, 1 VM will be dedicated to etcd server and the other 3 VMs will be etcd clients and installed with Kubernetes components. As per k8s best practice, it's strongly recommanded to have VMs dedicated to etcd cluster separate to the VMs used in K8S cluster. In our case, the etcd cluster is composed of 1 VM only to save resources of the Mac. 

Please note in the latest CoreOS Container Linux image, it's using etcd V3 (i.e. etcd-member.service) instead of etcd v2 (i.e. etcd2.service). 
```
MacBook-Pro:~ jaswang$ cd ~/k8s/coreos-vagrant
MacBook-Pro:cab3eed2aa1a29c6bab1714a49b87dbb2oreos-vagrant jaswang$ curl https://discovery.etcd.io/new\?size\=1
ab3eed2aa1a29c6bab1714a49b87dbb2 (record this string for later use)
```
### Step A3. Update the human readiable cl.conf file and translate to CoreOS ignition file 

In the latest version of CoreOS, the traditional cloud-config to bootstrap CoreOS has been superceded by CoreOS Ignition. In particular, in the repo downloaded in Step 1, by default it's using Vagrant with VirtualBox and in particular it's expecting a Ignition file of config.ign file instead of a cloud-config file of user-data. So as per CoreOS common practice, we need to update the cl.conf in the cloned repository with the etcd discovery token retrieved in Step 2 above and use CoreOS config transpiler (i.e. ct tool) to translate cl.conf file to CoreOS ignition file of config.ign. 
```
Download Mac binary of CoreOS config transpiler ct-<version>-x86_64-apple-darwin from https://github.com/coreos/container-linux-config-transpiler/releases. Copy it to /user/local/bin, change its name to "ct" and set it to be executable. 

MacBook-Pro:~ jaswang$ cd ~/k8s/coreos-vagrant
MacBook-Pro:coreos-vagrant jaswang$ vi cl.conf
(Replace <token> in the line of discovery with the etcd discovery token retrieved in Step 2. Importantly also change flannel network range from "10.1.0.0/16" to "10.2.0.0/16" to match with POD_NETWORK=10.2.0.0/16 later on and also the backend type is vxlan as shown below;
     
- name: flanneld.service
  dropins:
    - name: 50-network-config.conf
      contents: |
        [Service]
        ExecStartPre=/usr/bin/etcdctl set /flannel/network/config '{ "Network": "10.2.0.0/16", "Backend":{"Type":"vxlan"}  }'
)
MacBook-Pro:coreos-vagrant jaswang$ ct --platform=vagrant-virtualbox < cl.conf > config.ign
```
### Step A4. Set VM number and enable Docker Port Forwarding in config.rb file

In this step, we set up the number of VM to be created by Vagrant as 1 which will be the first VM to be created and will become the one-node etcd cluster.
```
MacBook-Pro:~ jaswang$ cd ~/k8s/coreos-vagrant
MacBook-Pro:coreos-vagrant jaswang$ mv config.rb.sample config.rb
MacBook-Pro:coreos-vagrant jaswang$ vi config.rb
(verify $num_instances=1)
```
### Step A5. Choose to use CoreOS Beta channel instead of alpha channel

By default, Vagrant pulls CoreOS image from Alpha Channel. To be safe, we change it to Beta channel instead. 

    MacBook-Pro:~ jaswang$ cd ~/k8s/coreos-vagrant
    MacBook-Pro:coreos-vagrant jaswang$ vi Vagrantfile
    (replace "alpha" with "beta". so we can have a more stable version.)

### Step A6. Create & Boot First VM to be etcd Server

In this step, we will actually create and start the first CoreOS VMs using Vagrant's default VirtualBox provider, which becomes etcd server. 

    MacBook-Pro:~ jaswang$ cd ~/k8s/coreos-vagrant
    MacBook-Pro:coreos-vagrant jaswang$ vagrant up
    (During the process, Vagrant will pull latest the CoreOS image from Beta channel and started in VirtualBox.)
    MacBook-Pro:coreos-vagrant jaswang$ vagrant status
    Current machine states:
    core-01                   running (virtualbox)

    The VM is running. To stop this VM, you can run `vagrant halt` to
    shut it down forcefully, or you can run `vagrant suspend` to simply
    suspend the virtual machine. In either case, to restart it again,
    simply run `vagrant up`.
    
Long onto newly created VM core-01 and verify etcd cluster is working (only 1 server member)

    MacBook-Pro:coreos-vagrant jaswang$ vagrant ssh core-01
    Last login: Sun Aug  6 03:33:07 UTC 2017 from 10.0.2.2 on ssh
    Container Linux by CoreOS beta (1465.3.0)
    core@core-01 ~ $ etcdctl member list
    e601a65b304e868f: name=core-01 peerURLs=http://172.17.8.101:2380 clientURLs=http://172.17.8.101:2379 isLeader=true
    core@core-01 ~ $ etcdctl ls / --recursive
    /flannel
    /flannel/network
    /flannel/network/config
    /flannel/network/subnets
    /flannel/network/subnets/10.2.19.0-24

### Step A7. Create & Boot Additional 3 VMs Via Vagrant

In this step, we will create & boot additional 3 VMs via vagrant, which will become etcd proxies and will host the K8S cluster.

First change config.rb to increase instance number and vm_memory

    MacBook-Pro:~ jaswang$ cd ~/k8s/coreos-vagrant
    MacBook-Pro:coreos-vagrant jaswang$ vi config.rb
    (set $num_instances=4 and $vm_memory = 2048)

Then change Vagrantfile to enable shared directory for those 3 VMs so that they have visibility to the local directory of Mac. By this way, it's easy to get codes and Docker files from local Mac directory into CoreOS VMs. 

    MacBook-Pro:~ jaswang$ cd ~/k8s/coreos-vagrant
    MacBook-Pro:coreos-vagrant jaswang$ vi Vagrantfile
    (Uncomment out the line of config.vm.synced_folder ".", "/home/core/share", id: "core", :nfs => true, :mount_options => ['nolock,vers=3,udp'])

Now create and boot 3 new VMs in VirtualBox provider. 

    MacBook-Pro:~ jaswang$ cd ~/k8s/coreos-vagrant
    MacBook-Pro:coreos-vagrant jaswang$ vagrant up
    (During the process, it will prompt for the Mac local admin passport when setting up shared directory in VMs. Just key in the password as requested)
    MacBook-Pro:coreos-vagrant jaswang$ vagrant status
    Current machine states:
    core-01                   running (virtualbox)
    core-02                   running (virtualbox)
    core-03                   running (virtualbox)
    core-04                   running (virtualbox)

    This environment represents multiple VMs. The VMs are all listed
    above with their current state. For more information about a specific
    VM, run `vagrant status NAME`.

### Step A8 Verify CoreOS VMs created by Vagrant

In this step, we verify the newly-created CoreOS VMs by ssh onto each VMs.

    MacBook-Pro:~ jaswang$ cd ~/k8s/coreos-vagrant
    MacBook-Pro:coreos-vagrant jaswang$ vagrant ssh core-01 -- -A
    Last login: Sun Jul 23 12:19:31 UTC 2017 from 10.0.2.2 on pts/0
    Container Linux by CoreOS alpha (1478.0.0)
    core@core-01 ~ $ journalctl -f --lines=50
    (Verify there is no error as shown below)
    ...
    Jul 23 13:19:36 core-01 systemd[1258]: Reached target Basic System.
    Jul 23 13:19:36 core-01 systemd[1258]: Reached target Default.
    Jul 23 13:19:36 core-01 systemd[1258]: Startup finished in 21ms.
    Jul 23 13:19:36 core-01 systemd[1]: Started User Manager for UID 500.

## Section B. SET UP KUBERNETES CLUSTER ON MAC

Now we have 4 CoreOS VMs created on Mac with Vagrant & VirtualBox. In this section, we will set up Kubernetes cluster on those VMs step by step, in which core-02 is the master node and core-03 & core-04 are the worker nodes. 

The steps in this section are mainly sourced from https://coreos.com/kubernetes/docs/latest/getting-started.html with important corrections and adjustments. Before start, we list the actual values of the following variables which will be used throughout this section. 

      MASTER_HOST=172.17.8.102 - IP address of the K8S master node core-02 which can be accessed by worker nodes and kubectl clien on Mac.
      ETCD_ENDPOINTS=http://172.17.8.101:2379 - List of etcd machines, comma separated. As only core-01 runs etcd so only 1 URL
      POD_NETWORK=10.2.0.0/16 - The CIDR network to use for pod IPs. the flannel overlay network will provide routing to this network.
      SERVICE_IP_RANGE=10.3.0.0/24 - The CIDR network to use for service cluster VIPs, routing is handled by local kube-proxy
      K8S_SERVICE_IP=10.3.0.1 - The VIP (Virtual IP) address of the Kubernetes API Service.
      DNS_SERVICE_IP=10.3.0.10 - The VIP (Virtual IP) address of the cluster DNS service. 

### Step B1 Verify etcd Service status on CoreOS VMs & Troubleshooting when restart failing upon VM reboot

Kubernetes uses etcd service, which is a distributed key-value database, to store all kinds of configurations and status information. When the 4 CoreOS VMs were created in Step A7, etcd service (etcd v3) has been set up as 1 VM running etcd server and 3 VMs as etcd proxy/client. 

So in this step, we verify the etcd service is working well before setting up K8S components. 

First we log onto core-01 VM first to verfiy the etcd server is running on core-01 VM

    MacBook-Pro:~ jaswang$ cd ~/k8s/coreos-vagrant
    MacBook-Pro:coreos-vagrant jaswang$ vagrant ssh core-01 -- -A
    core@core-01 ~ $ etcdctl member list
    (it should shown one etcd server is running on core-01 VM as shown below)
    f3c0b70e84d56c98: name=core-01 peerURLs=http://172.17.8.101:2380 clientURLs=http://172.17.8.101:2379 isLeader=true
    core@core-01 ~ $ systemctl status etcd-member.service
    (Verify etcd-member service is running as etcdserver instead of etcd proxy, i.e. client)
    
    ● etcd-member.service - etcd (System Application Container)
    Loaded: loaded (/usr/lib/systemd/system/etcd-member.service; enabled; vendor preset: enabled)
    Drop-In: /etc/systemd/system/etcd-member.service.d
               └─20-clct-etcd-member.conf
           
    Active: active (running) since Sun 2017-07-23 03:53:37 UTC; 9h ago  
    ...
    Jul 23 12:40:28 core-01 etcd-wrapper[773]: 2017-07-23 12:40:28.726727 I | etcdserver: compacted raft log at 25003

Then we log onto each of the rest 3 VMs to verify the etcd proxy is working by the steps below: 

    MacBook-Pro:~ jaswang$ cd ~/k8s/coreos-vagrant
    MacBook-Pro:coreos-vagrant jaswang$ vagrant ssh core-02 -- -A
    core@core-02 ~ $ systemctl status etcd-member
    (Verify etcd-member service is running as etcd proxy/client instead of etcd server)
    
    ● etcd-member.service - etcd (System Application Container)
    ...
    Active: active (running) since Sun 2017-07-23 03:53:56 UTC; 9h ago
    ...
    Jul 23 03:53:56 core-02 etcd-wrapper[767]: 2017-07-23 03:53:56.464032 I | etcdmain: proxy: listening for client requests on     http://0.0.0.0:2379
    core@core-02 ~ $ etcdctl ls / --recursive
    (Verify the etcd tree can be displayed as below)
    /flannel
    /flannel/network
    /flannel/network/config
    /flannel/network/subnets
    /flannel/network/subnets/10.1.55.0-24
    core@core-02 ~ $ etcdctl set /message Hello
    (Verify on K8S VM we can set etcd key and value pairs)
    Hello
    core@core-02 ~ $ etcdctl get /message
    (Verify on K8S VM we can get etcd key and value pairs)
    Hello
    core@core-02 ~ $ curl http://172.17.8.101:2379/v2/keys/message
    (Verify on K8S VM we can get etcd key and value pairs via etcd server URL)
    {"action":"get","node":{"key":"/message","value":"Hello","modifiedIndex":16,"createdIndex":16}}
    core@core-02 ~ $ etcdctl rm /message
    (Verify on K8S VM we can remove etcd key and value pairs)
    PrevNode.Value: Hello
    core@core-04 /etc/systemd/system/etcd-member.service.d $ etcdctl cluster-health
    member f3c0b70e84d56c98 is healthy: got healthy result from http://172.17.8.101:2379
    cluster is healthy

Please note it's possible that the etcd-member service of proxy style on core-02 & 03 & 04 fails to start when VM reboot, even though the etcd-member of server style on core-01 starts successfully when VM reboot. In this case execute the following steps to trouble shoot: 

    core@core-02 ~ $ systemctl status etcd-member
    ● etcd-member.service - etcd (System Application Container)
       Loaded: loaded (/usr/lib/systemd/system/etcd-member.service; enabled; vendor preset: enabled)
      Drop-In: /etc/systemd/system/etcd-member.service.d
               └─20-clct-etcd-member.conf
       Active: activating (auto-restart) (Result: exit-code) since Wed 2017-08-02 13:12:54 UTC; 6s ago
         Docs: https://github.com/coreos/etcd
      Process: 13263 ExecStart=/usr/lib/coreos/etcd-wrapper $ETCD_OPTS --name=${COREOS_VAGRANT_VIRTUALBOX_HOSTNAME} --listen-peer-    urls=http://${COREOS_VAGRANT_VIRTUALBOX_PRIVATE_IPV4}:2380 --listen-client-url
      Process: 13252 ExecStartPre=/usr/bin/rkt rm --uuid-file=/var/lib/coreos/etcd-member-wrapper.uuid (code=exited, status=0/SUCCESS)
      Process: 13249 ExecStartPre=/usr/bin/mkdir --parents /var/lib/coreos (code=exited, status=0/SUCCESS)
     Main PID: 13263 (code=exited, status=1/FAILURE)
        Tasks: 0 (limit: 32768)
       Memory: 0B
          CPU: 0
       CGroup: /system.slice/etcd-member.service

    Aug 02 13:12:54 core-02 systemd[1]: Failed to start etcd (System Application Container).
    Aug 02 13:12:54 core-02 systemd[1]: etcd-member.service: Unit entered failed state.
    Aug 02 13:12:54 core-02 systemd[1]: etcd-member.service: Failed with result 'exit-code'.
    
    core@core-02 ~ $ journalctl -u etcd-member -f
    (The following error repeats)
    ...
    Aug 02 13:15:32 core-02 systemd[1]: etcd-member.service: Service hold-off time over, scheduling restart.
    Aug 02 13:15:32 core-02 systemd[1]: Stopped etcd (System Application Container).
    Aug 02 13:15:32 core-02 systemd[1]: Starting etcd (System Application Container)...
    Aug 02 13:15:32 core-02 rkt[14013]: "666f8d6f-3915-4b49-b658-0ba3dc3151d2"
    Aug 02 13:15:32 core-02 etcd-wrapper[14022]: ++ id -u etcd
    Aug 02 13:15:32 core-02 etcd-wrapper[14022]: + exec /usr/bin/rkt run --uuid-file-save=/var/lib/coreos/etcd-member-wrapper.uuid --trust-keys-from-https --mount volume=coreos-systemd-dir,target=/run/systemd/system --volume coreos-systemd-dir,kind=host,source=/run/systemd/system,readOnly=true --mount volume=coreos-notify,target=/run/systemd/notify --volume coreos-notify,kind=host,source=/run/systemd/notify --set-env=NOTIFY_SOCKET=/run/systemd/notify --volume coreos-data-dir,kind=host,source=/var/lib/etcd,readOnly=false --volume coreos-etc-ssl-certs,kind=host,source=/etc/ssl/certs,readOnly=true --volume coreos-usr-share-certs,kind=host,source=/usr/share/ca-certificates,readOnly=true --volume coreos-etc-hosts,kind=host,source=/etc/hosts,readOnly=true --volume coreos-etc-resolv,kind=host,source=/etc/resolv.conf,readOnly=true --mount volume=coreos-data-dir,target=/var/lib/etcd --mount volume=coreos-etc-ssl-certs,target=/etc/ssl/certs --mount volume=coreos-usr-share-certs,target=/usr/share/ca-certificates --mount volume=coreos-etc-hosts,target=/etc/hosts --mount volume=coreos-etc-resolv,target=/etc/resolv.conf --inherit-env --stage1-from-dir=stage1-fly.aci quay.io/coreos/etcd:v3.1.8 --user=232 -- --name=core-02 --listen-peer-urls=http://172.17.8.102:2380 --listen-client-urls=http://0.0.0.0:2379 --initial-advertise-peer-urls=http://172.17.8.102:2380 --advertise-client-urls=http://172.17.8.102:2379 --discovery=https://discovery.etcd.io/ab3eed2aa1a29c6bab1714a49b87dbb2
    Aug 02 13:15:32 core-02 etcd-wrapper[14022]: 2017-08-02 13:15:32.934767 I | pkg/flags: recognized and used environment variable ETCD_DATA_DIR=/var/lib/etcd
    Aug 02 13:15:32 core-02 etcd-wrapper[14022]: 2017-08-02 13:15:32.935060 I | pkg/flags: recognized environment variable ETCD_NAME, but unused: shadowed by corresponding flag
    Aug 02 13:15:32 core-02 etcd-wrapper[14022]: 2017-08-02 13:15:32.935228 W | pkg/flags: unrecognized environment variable ETCD_USER=etcd
    Aug 02 13:15:32 core-02 etcd-wrapper[14022]: 2017-08-02 13:15:32.935369 W | pkg/flags: unrecognized environment variable ETCD_IMAGE_TAG=v3.1.8
    Aug 02 13:15:32 core-02 etcd-wrapper[14022]: 2017-08-02 13:15:32.938642 I | etcdmain: etcd Version: 3.1.8
    Aug 02 13:15:32 core-02 etcd-wrapper[14022]: 2017-08-02 13:15:32.938818 I | etcdmain: Git SHA: d267ca9
    Aug 02 13:15:32 core-02 etcd-wrapper[14022]: 2017-08-02 13:15:32.938965 I | etcdmain: Go Version: go1.7.5
    Aug 02 13:15:32 core-02 etcd-wrapper[14022]: 2017-08-02 13:15:32.939095 I | etcdmain: Go OS/Arch: linux/amd64
    Aug 02 13:15:32 core-02 etcd-wrapper[14022]: 2017-08-02 13:15:32.939222 I | etcdmain: setting maximum number of CPUs to 1, total number of available CPUs is 1
    Aug 02 13:15:32 core-02 etcd-wrapper[14022]: 2017-08-02 13:15:32.939373 C | etcdmain: invalid datadir. Both member and proxy directories exist.
    Aug 02 13:15:32 core-02 systemd[1]: etcd-member.service: Main process exited, code=exited, status=1/FAILURE
    Aug 02 13:15:32 core-02 systemd[1]: Failed to start etcd (System Application Container).
    Aug 02 13:15:32 core-02 systemd[1]: etcd-member.service: Unit entered failed state.
    Aug 02 13:15:32 core-02 systemd[1]: etcd-member.service: Failed with result 'exit-code'.
    ...
    
    In this case, remove the sub directories of member then etcd will be started successfully. 
    
    core@core-02 ~ $ sudo rm -rf /var/lib/etcd/member
    core@core-02 ~ $ sudo systemctl restart etcd-member 
    core@core-02 ~ $ etcdctl member list
    f3c0b70e84d56c98: name=core-01 peerURLs=http://172.17.8.101:2380 clientURLs=http://172.17.8.101:2379 isLeader=true

    To make a permenant fix, add one line in the etcd-member servcie drop-in file as shown below (on etcd proxy VMs only). 
    
    core@core-02 ~ $ cd /etc/systemd/system/etcd-member.service.d/
    core@core-02 /etc/systemd/system/etcd-member.service.d $ vi 20-clct-etcd-member.conf 
    core@core-02 /etc/systemd/system/etcd-member.service.d $ sudo vi 20-clct-etcd-member.conf 
    Add the line of "ExecStartPre=-/usr/bin/rkt rm -rf /var/lib/etcd/member" before the line of "ExecStart="

### Step B2 - Set Up Flanneld On Each K8S Node

Each pod launched in the K8S cluster will be assigned an IP out of the range of POD_NETWORK=10.2.0.0/16. This network must be routable between all hosts in the cluster. In a default installation, the flannel overlay network will provide routing to this network. 

The flannel works as below on each K8S cluster node:

    i. First Flanneld service on each K8S node (master and worker) binds to the VM Network Interface which is public IP routable amongst the K8S cluster VMs, in our case, it should be interface eth1 which has IP address of "172.17.8.10*". 

    ii. When Flanneld service on each K8S node first started, it will check the values stored in etcd to find an un-allocated C-class subnet within the range of POD_NETWORK=10.2.0.0/16, then allocate that C-class subnet to the flannel interface on that VM and write the C-class into etcd so Flanneld service on other node won't pick up duplicate C-class subnet. 

    iii. On each node, traffic to the any IP in the range of 10.2.0.0/16 will be routed by Flanneld service so that the PODs in the K8S cluster can communicate to each other.   

As the proper setting for Flanneld service is crucial for the K8S PODs to work correctly, so we highlight this part in this dedicated Step B2 to make sure the Flanneld service work as the way described above. By the way, even though the VM core-01 just runs etcd server and does not participate in the K8S cluster, we still configure its Flanneld purely for consistency purpose rather than technically required. 

Fisrt on each VM, verify the flanneld drop-in file points to the IP range of 10.2.0.0/16 as specified in Step A3 above. 

    core@core-02 ~ $ cd /etc/systemd/system/flanneld.service.d/
    core@core-02 /etc/systemd/system/flanneld.service.d $ vi 50-network-config.conf 
    (Verify the IP range & Backend settings are as below, if not just change it)
    [Service]
    ExecStartPre=/usr/bin/etcdctl set /flannel/network/config '{ "Network": "10.2.0.0/16" , "Backend":{"Type":"vxlan"} }'
    core@core-02 ~ $ ps -ef|grep flanneld
    (Verify flanneld process has "--ectd-prefix=/flannel/network" means it uses configuration under etcd directory of /flannel/network instead of the default "/coreos.com/network"
    root       780     1  0 12:07 ?        00:00:00 /opt/bin/flanneld --ip-masq=true --etcd-prefix=/flannel/network


Then we configure flannel to source its local configuration in /etc/flannel/options.env and cluster-level configuration in etcd. 

    core@core-02 ~ $ sudo mkdir /etc/flannel
    core@core-02 ~ $ sudo vi /etc/flannel/options.env
    (Add the following two lines. Please note FLANNELD_IFACE needs to be set to the IP address of eth1 on each VM)
    FLANNELD_IFACE=172.17.8.102
    FLANNELD_ETCD_ENDPOINTS=http://172.17.8.101:2379

Next create another systemd drop-in for enabling the flanneld service setting described above. 

    core@core-03 ~ $ sudo netstat -nap|grep flanneld
    (Initially Flanneld does not connect to etcd on 172.17.8.101)
    tcp        0      0 127.0.0.1:47748         127.0.0.1:2379          ESTABLISHED 1036/flanneld       
    tcp        0      0 127.0.0.1:47744         127.0.0.1:2379          ESTABLISHED 1036/flanneld       
    udp        0      0 10.0.2.14:8285          0.0.0.0:*                           1036/flanneld         
    core@core-02 /etc/kubernetes/ssl $ cd /etc/systemd/system/flanneld.service.d/
    core@core-02 /etc/systemd/system/flanneld.service.d $ sudo vi 40-ExecStartPre-symlink.conf
    (Add the following two lines)
    [Service]
    ExecStartPre=/usr/bin/ln -sf /etc/flannel/options.env /run/flannel/options.env
    core@core-02 /etc/systemd/system/flanneld.service.d $ sudo systemctl daemon-reload
    core@core-02 /etc/systemd/system/flanneld.service.d $ sudo systemctl restart flanneld
    core@core-02 ~ $ sudo netstat -nap|grep flanneld
    (After the change, flanneld now is connecting to etcd server on core-01)
    tcp        0      0 172.17.8.102:49880      172.17.8.101:2379       ESTABLISHED 2232/flanneld       
    tcp        0      0 172.17.8.102:49876      172.17.8.101:2379       ESTABLISHED 2232/flanneld       
    tcp        0      0 172.17.8.102:49878      172.17.8.101:2379       ESTABLISHED 2232/flanneld
    udp        0      0 172.17.8.102:8285       0.0.0.0:*                           2232/flanneld
    
Then after each VM's flanneld set up by the above steps, we verify the C-class allocated to flannel virtual network interface on each VM which should be consistent with the values stored in etcd and also pingable from each node. 

    core@core-02 ~ $ ifconfig
    ...
    flannel0: flags=4305<UP,POINTOPOINT,RUNNING,NOARP,MULTICAST>  mtu 1472
        inet 10.2.14.0  netmask 255.255.0.0  destination 10.2.14.0
        inet6 fe80::f3db:e4b1:94e2:efbf  prefixlen 64  scopeid 0x20<link>
        unspec 00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00  txqueuelen 500  (UNSPEC)
        RX packets 6  bytes 504 (504.0 B)
        RX errors 0  dropped 0  overruns 0  frame 0
        TX packets 17  bytes 1032 (1.0 KiB)
        TX errors 0  dropped 0 overruns 0  carrier 0  collisions 0
    ...
    core@core-02 ~ $ etcdctl ls / --recursive
    /flannel
    /flannel/network
    /flannel/network/config
    /flannel/network/subnets
    /flannel/network/subnets/10.2.19.0-24 (Please note this one is initially created before we configure Flanneld and can be ignored)
    /flannel/network/subnets/10.2.14.0-24
    /flannel/network/subnets/10.2.40.0-24
    /flannel/network/subnets/10.2.35.0-24
    /flannel/network/subnets/10.2.2.0-24
    core@core-02 ~ $ ping 10.2.14.0
    core@core-02 ~ $ ping 10.2.40.0
    core@core-02 ~ $ ping 10.2.35.0
    core@core-02 ~ $ ping 10.2.2.0
    core@core-02 ~ $ netstat -rn
    Kernel IP routing table
    Destination     Gateway         Genmask         Flags   MSS Window  irtt Iface
    ...
    10.2.0.0        0.0.0.0         255.255.0.0     U         0 0          0 flannel0
    172.17.0.0      0.0.0.0         255.255.0.0     U         0 0          0 eth1

### Step B3 - Generate Kubernetes TLS Assets

The Kubernetes API has various methods for validating clients. In this practice, we will configure the API server to use client certificate authentication. If we are in an enterprise which has an exising PKI infrastructure, we should follow the normal enterprise PKI procedure to create certificate requests and sign them with enterprise root certificate. In this practice, however, we will use openssl tool to create our own certificates as below: 
        
        Root CA Public & Private keys - ca.pem & ca-key.pam
        API Server Public & Private Keys - apiserver.pem & apiserver-key.pem
        Worker Node Public & Private Keys - ${WORKER_FQDN}-worker.pem & ${WORKER_FQDN}-worker-key.pem
        Cluster Admin Public & Private Keys - admin.pem & admin-key.pem

First we create the cluster root CA keys on one of the CoreOS VMs by the steps below: 

    core@core-01 ~ $ cd share/
    core@core-01 ~/share $ mkdir certificates
    core@core-01 ~/share $ cd certificates/
    core@core-01 ~/share $ openssl genrsa -out ca-key.pem 2048
    Generating RSA private key, 2048 bit long modulus       
    ...+++
    e is 65537 (0x10001)
    core@core-02 ~/share/certificates $ openssl req -x509 -new -nodes -key ca-key.pem -days 10000 -out ca.pem -subj "/CN=kube-ca"

Then we create Kubernetes API Server Keypair

    core@core-02 ~/share/certificates $ vi openssl.cnf
    [req]
    req_extensions = v3_req
    distinguished_name = req_distinguished_name
    [req_distinguished_name]
    [ v3_req ]
    basicConstraints = CA:FALSE
    keyUsage = nonRepudiation, digitalSignature, keyEncipherment
    subjectAltName = @alt_names
    [alt_names]
    DNS.1 = kubernetes
    DNS.2 = kubernetes.default
    DNS.3 = kubernetes.default.svc
    DNS.4 = kubernetes.default.svc.cluster.local
    IP.1 = 10.3.0.1
    IP.2 = 172.17.8.102
    core@core-02 ~/share/certificates $ openssl genrsa -out apiserver-key.pem 2048
    Generating RSA private key, 2048 bit long modulus
    .......................................................................................................+++
    .+++
    e is 65537 (0x10001)
    core@core-02 ~/share/certificates $ openssl req -new -key apiserver-key.pem -out apiserver.csr -subj "/CN=kube-apiserver" -config openssl.cnf
    core@core-02 ~/share/certificates $ openssl x509 -req -in apiserver.csr -CA ca.pem -CAkey ca-key.pem -CAcreateserial -out apiserver.pem -days 365 -extensions v3_req -extfile openssl.cnf
    Signature ok
    subject=/CN=kube-apiserver
    Getting CA Private Key
    
Then we generates a unique TLS certificate for every Kubernetes worker node, i.e core-03 & core-04. 

    core@core-02 ~/share/certificates $ vi worker-openssl.cnf
    [req]
    req_extensions = v3_req
    distinguished_name = req_distinguished_name
    [req_distinguished_name]
    [ v3_req ]
    basicConstraints = CA:FALSE
    keyUsage = nonRepudiation, digitalSignature, keyEncipherment
    subjectAltName = @alt_names
    [alt_names]
    IP.1 = $ENV::WORKER_IP
    core@core-02 ~/share/certificates $ openssl genrsa -out core-03-worker-key.pem 2048
    Generating RSA private key, 2048 bit long modulus
    ......................................................................+++
    .....................................+++
    e is 65537 (0x10001)
    core@core-02 ~/share/certificates $ WORKER_IP=172.17.8.103 openssl req -new -key core-03-worker-key.pem -out core-03-worker.csr -subj "/CN=core-03" -config worker-openssl.cnf
    core@core-02 ~/share/certificates $ WORKER_IP=172.17.8.103 openssl x509 -req -in core-03-worker.csr -CA ca.pem -CAkey ca-key.pem -CAcreateserial -out core-03-worker.pem -days 365 -extensions v3_req -extfile worker-openssl.cnf
    Signature ok
    subject=/CN=core-03
    Getting CA Private Key
    
    Repeat the above steps for core-04
    
Then we generate the Cluster Administrator Keypair, which will be used by the kubectl client to be set up in local MacPro host. 

    core@core-02 ~/share/certificates $ openssl genrsa -out admin-key.pem 2048
    Generating RSA private key, 2048 bit long modulus
    ............................+++
    ..................................................................................+++
    e is 65537 (0x10001)
    core@core-02 ~/share/certificates $ openssl req -new -key admin-key.pem -out admin.csr -subj "/CN=kube-admin"
    core@core-02 ~/share/certificates $ openssl x509 -req -in admin.csr -CA ca.pem -CAkey ca-key.pem -CAcreateserial -out admin.pem -days     365
    Signature ok
    subject=/CN=kube-admin
    Getting CA Private Key

### Step B4 - Deploy K8S Master Node Components

In this step, we deploy K8S master node on core-02. 

First we prepare TSL certificates/assets on core-02

    core@core-02 ~ $ sudo mkdir -p /etc/kubernetes/ssl
    core@core-02 ~ $ cd /etc/kubernetes/ssl
    core@core-02 /etc/kubernetes/ssl $ sudo cp /home/core/share/certificates/ca.pem .
    core@core-02 /etc/kubernetes/ssl $ sudo cp /home/core/share/certificates/apiserver.pem .
    core@core-02 /etc/kubernetes/ssl $ sudo cp /home/core/share/certificates/apiserver-key.pem .
    core@core-02 /etc/kubernetes/ssl $ sudo chmod 600 *-key.pem

In order for flannel to manage the pod network in the cluster, Docker needs to be configured to use flannel. So we need to do three things here: 

    a. use systend drop-in to configure Docker starts after flanneld
    b. create Docker CNI (Containter Network Interface) options file
    c. set up flannel CNI configuration file (Please note, we choose to use Flannel instead of Calico for container networking)

    core@core-03 ~ $ systemctl status docker
    (Before change, docker service is not active)
    ● docker.service - Docker Application Container Engine
       Loaded: loaded (/run/torcx/unpack/docker/lib/systemd/system/docker.service; linked; vendor preset: disabled)
       Active: inactive (dead)
         Docs: http://docs.docker.com
    core@core-02 ~ $ sudo mkdir -p /etc/systemd/system/docker.service.d
    core@core-02 ~ $ cd /etc/systemd/system/docker.service.d
    core@core-02 /etc/systemd/system/docker.service.d $ sudo vi 40-flannel.conf
    (Add the following lines)
    [Unit]
    Requires=flanneld.service
    After=flanneld.service
    [Service]
    EnvironmentFile=/etc/kubernetes/cni/docker_opts_cni.env
    core@core-02 ~ $ sudo mkdir /etc/kubernetes/cni
    core@core-02 ~ $ cd /etc/kubernetes/cni/
    core@core-02 /etc/kubernetes/cni $ sudo vi docker_opts_cni.env
    (Add the following lines)
    DOCKER_OPT_BIP=""
    DOCKER_OPT_IPMASQ=""
    core@core-02 /etc/kubernetes/cni $ sudo mkdir net.d
    core@core-02 /etc/kubernetes/cni $ cd net.d/
    core@core-02 /etc/kubernetes/cni/net.d $ sudo vi 10-flannel.conf
    (Add the following lines)
    {
        "name": "podnet",
        "type": "flannel",
        "delegate": {
            "isDefaultGateway": true
        }
    }
    core@core-02 ~ $ sudo systemctl daemon-reload
    core@core-02 ~ $ sudo systemctl stop docker
    core@core-02 ~ $ sudo systemctl stop docker-tcp.socket
    core@core-02 ~ $ sudo systemctl start docker-tcp.socket
    core@core-02 ~ $ sudo systemctl start docker

Now we create kubelet unit on K8S master. The kubelet is the agent on each machine that starts and stops Pods and other machine-level tasks. The kubelet communicates with the API server (also running on the master nodes) with the TLS certificates we placed on disk earlier.

On the master node, the kubelet is configured to communicate with the API server, but not register for cluster work, as shown in the --register-schedulable=false line in the YAML excerpt below. This prevents user pods being scheduled on the master nodes, and ensures cluster work is routed only to task-specific worker nodes.Note that the kubelet running on a master node may log repeated attempts to post its status to the API server. These warnings are expected behavior and can be ignored. 

The following kubelet service unit file uses the following environment variables: 

    ${ADVERTISE_IP} = 172.17.8.102
    ${DNS_SERVICE_IP} = 10.3.0.10
    ${K8S_VER} =  v1.6.1_coreos.0
    
    core@core-02 ~ $ cd /etc/systemd/system
    core@core-02 /etc/systemd/system $ sudo vi kubelet.service
    (Add the following lines)
    [Service]
    Environment=KUBELET_IMAGE_TAG=v1.6.1_coreos.0
    Environment="RKT_RUN_ARGS=--uuid-file-save=/var/run/kubelet-pod.uuid \
      --volume var-log,kind=host,source=/var/log \
      --mount volume=var-log,target=/var/log \
      --volume dns,kind=host,source=/etc/resolv.conf \
      --mount volume=dns,target=/etc/resolv.conf"
    ExecStartPre=/usr/bin/mkdir -p /etc/kubernetes/manifests
    ExecStartPre=/usr/bin/mkdir -p /var/log/containers
    ExecStartPre=-/usr/bin/rkt rm --uuid-file=/var/run/kubelet-pod.uuid
    ExecStart=/usr/lib/coreos/kubelet-wrapper \
      --api-servers=http://127.0.0.1:8080 \
      --register-schedulable=false \
      --cni-conf-dir=/etc/kubernetes/cni/net.d \
      --network-plugin=cni \
      --container-runtime=docker \
      --allow-privileged=true \
      --pod-manifest-path=/etc/kubernetes/manifests \
      --hostname-override=172.17.8.102 \
      --cluster_dns=10.3.0.10 \
      --cluster_domain=cluster.local
    ExecStop=-/usr/bin/rkt stop --uuid-file=/var/run/kubelet-pod.uuid
    Restart=always
    RestartSec=10

    [Install]
    WantedBy=multi-user.target

Now we set up the kube-apiserver Pod. The API server is where most of the magic happens. It is stateless by design and takes in API requests, processes them and stores the result in etcd if needed, and then returns the result of the request.

We're going to use a unique feature of the kubelet to launch a Pod that runs the API server. Above we configured the kubelet to watch a local directory for pods to run with the --pod-manifest-path=/etc/kubernetes/manifests flag. All we need to do is place our Pod manifest in that location, and the kubelet will make sure it stays running, just as if the Pod was submitted via the API. The cool trick here is that we don't have an API running yet, but the Pod will function the exact same way, which simplifies troubleshooting later on.

The following YAML file for api-service POD uses the following environment variables;

    ${ETCD_ENDPOINTS} = http://172.17.8.101:2379 
    ${SERVICE_IP_RANGE} = 10.3.0.0/24
    ${ADVERTISE_IP} = 172.17.8.102
    ${K8S_VER} =  v1.6.1_coreos.0
    
    core@core-02 ~ $ sudo mkdir /etc/kubernetes/manifests
    core@core-02 ~ $ cd /etc/kubernetes/manifests
    core@core-02 /etc/kubernetes/manifests $ sudo vi kube-apiserver.yaml
    apiVersion: v1
    kind: Pod
    metadata:
      name: kube-apiserver
      namespace: kube-system
    spec:
      hostNetwork: true
      containers:
      - name: kube-apiserver
        image: quay.io/coreos/hyperkube:v1.6.1_coreos.0
        command:
        - /hyperkube
        - apiserver
        - --bind-address=0.0.0.0
        - --etcd-servers=http://172.17.8.101:2379
        - --allow-privileged=true
        - --service-cluster-ip-range=10.3.0.0/24
        - --secure-port=443
        - --advertise-address=172.17.8.102
        - --admission-control=NamespaceLifecycle,LimitRanger,ServiceAccount,DefaultStorageClass,ResourceQuota
        - --tls-cert-file=/etc/kubernetes/ssl/apiserver.pem
        - --tls-private-key-file=/etc/kubernetes/ssl/apiserver-key.pem
        - --client-ca-file=/etc/kubernetes/ssl/ca.pem
        - --service-account-key-file=/etc/kubernetes/ssl/apiserver-key.pem
        - --runtime-config=extensions/v1beta1/networkpolicies=true
        - --anonymous-auth=false
        livenessProbe:
          httpGet:
            host: 127.0.0.1
            port: 8080
            path: /healthz
          initialDelaySeconds: 15
          timeoutSeconds: 15
        ports:
        - containerPort: 443
          hostPort: 443
          name: https
        - containerPort: 8080
          hostPort: 8080
          name: local
        volumeMounts:
        - mountPath: /etc/kubernetes/ssl
          name: ssl-certs-kubernetes
          readOnly: true
        - mountPath: /etc/ssl/certs
          name: ssl-certs-host
          readOnly: true
      volumes:
      - hostPath:
          path: /etc/kubernetes/ssl
        name: ssl-certs-kubernetes
      - hostPath:
          path: /usr/share/ca-certificates
        name: ssl-certs-host

Similarly now we set up kube-proxy Pod. The proxy is responsible for directing traffic destined for specific services and pods to the correct location. The proxy communicates with the API server periodically to keep up to date. Both the master and worker nodes in K8S cluster will run the proxy. 

The following YAML file for kub-proxy POD uses the following environment variable;

    ${K8S_VER} =  v1.6.1_coreos.0
    
    core@core-02 ~ $ cd /etc/kubernetes/manifests
    core@core-02 /etc/kubernetes/manifests $ sudo vi kube-proxy.yaml
    apiVersion: v1
    kind: Pod
    metadata:
      name: kube-proxy
      namespace: kube-system
    spec:
      hostNetwork: true
      containers:
      - name: kube-proxy
        image: quay.io/coreos/hyperkube:v1.6.1_coreos.0
        command:
        - /hyperkube
        - proxy
        - --master=http://127.0.0.1:8080
        securityContext:
          privileged: true
        volumeMounts:
        - mountPath: /etc/ssl/certs
          name: ssl-certs-host
          readOnly: true
      volumes:
      - hostPath:
          path: /usr/share/ca-certificates
        name: ssl-certs-host

Next we set up POD YMAL file for kube-controller-manager. The controller manager is responsible for reconciling any required actions based on changes to Replication Controllers.

    ${K8S_VER} =  v1.6.1_coreos.0
    
    core@core-02 ~ $ cd /etc/kubernetes/manifests
    core@core-02 /etc/kubernetes/manifests $ sudo vi kube-controller-manager.yaml
    apiVersion: v1
    kind: Pod
    metadata:
      name: kube-controller-manager
      namespace: kube-system
    spec:
      hostNetwork: true
      containers:
      - name: kube-controller-manager
        image: quay.io/coreos/hyperkube:v1.6.1_coreos.0
        command:
        - /hyperkube
        - controller-manager
        - --master=http://127.0.0.1:8080
        - --leader-elect=true
        - --service-account-private-key-file=/etc/kubernetes/ssl/apiserver-key.pem
        - --root-ca-file=/etc/kubernetes/ssl/ca.pem
        resources:
          requests:
            cpu: 200m
        livenessProbe:
          httpGet:
            host: 127.0.0.1
            path: /healthz
            port: 10252
          initialDelaySeconds: 15
          timeoutSeconds: 15
        volumeMounts:
        - mountPath: /etc/kubernetes/ssl
          name: ssl-certs-kubernetes
          readOnly: true
        - mountPath: /etc/ssl/certs
          name: ssl-certs-host
          readOnly: true
      volumes:
      - hostPath:
          path: /etc/kubernetes/ssl
        name: ssl-certs-kubernetes
      - hostPath:
          path: /usr/share/ca-certificates
        name: ssl-certs-host

Now we set up POD YAML file for kube-scheduler. The scheduler monitors the API for unscheduled pods, finds them a machine to run on, and communicates the decision back to the API.

    ${K8S_VER} =  v1.6.1_coreos.0
    
    core@core-02 ~ $ cd /etc/kubernetes/manifests
    core@core-02 /etc/kubernetes/manifests $ sudo vi kube-scheduler.yaml
    apiVersion: v1
    kind: Pod
    metadata:
      name: kube-scheduler
      namespace: kube-system
    spec:
      hostNetwork: true
      containers:
      - name: kube-scheduler
        image: quay.io/coreos/hyperkube:v1.6.1_coreos.0
        command:
        - /hyperkube
        - scheduler
        - --master=http://127.0.0.1:8080
        - --leader-elect=true
        resources:
          requests:
            cpu: 100m
        livenessProbe:
          httpGet:
            host: 127.0.0.1
            path: /healthz
            port: 10251
          initialDelaySeconds: 15
          timeoutSeconds: 15

Now that we've defined all of our units and written our TLS certificates to disk, we're ready to start the master components.

First, we need to tell systemd that we've changed units on disk and it needs to rescan & reload everything:

    core@core-02 /etc/kubernetes/manifests $ sudo systemctl daemon-reload
    
Now that everything is configured, we can start the kubelet, which will also start the Pod manifests for the API server, the controller manager, proxy and scheduler.

    core@core-02 ~ $ sudo systemctl start kubelet
    core@core-02 ~ $ sudo systemctl enable kubelet
    Created symlink /etc/systemd/system/multi-user.target.wants/kubelet.service → /etc/systemd/system/kubelet.service.

Now we can do the following basic health check about the K8S master components we set. Later we will set up kubectl client natively on the local Mac laptop which will connect to the API service running on core-02 to manage the K8S cluster. 

    First we check the kubelet service is started and running properly. (this could take a few minutes after starting the kubelet.service)
    
    core@core-02 ~ $ systemctl status kubelet
    ● kubelet.service
       Loaded: loaded (/etc/systemd/system/kubelet.service; enabled; vendor preset: disabled)
       Active: active (running) since Wed 2017-08-02 06:22:28 UTC; 9min ago
     Main PID: 3633 (kubelet)
        Tasks: 13 (limit: 32768)
       Memory: 93.8M
          CPU: 36.489s
       CGroup: /system.slice/kubelet.service
               ├─3633 /kubelet --api-servers=http://127.0.0.1:8080 --register-schedulable=false --cni-conf-dir=/etc/kubernetes/cni/net.d     --network-plugin= --container-runtime=docker --allow-privileged=true --
               └─3741 journalctl -k -f

    Aug 02 06:30:52 core-02 kubelet-wrapper[3633]: W0802 06:30:52.213861    3633 helpers.go:771] eviction manager: no observation found for
    ...
    
    Then we check Kube API service.
    
    core@core-02 ~ $ curl http://127.0.0.1:8080/version
    {
      "major": "1",
      "minor": "6",
      "gitVersion": "v1.6.1+coreos.0",
      "gitCommit": "9212f77ed8c169a0afa02e58dce87913c6387b3e",
      "gitTreeState": "clean",
      "buildDate": "2017-04-04T00:32:53Z",
      "goVersion": "go1.7.5",
      "compiler": "gc",
      "platform": "linux/amd64"
    }
    
    Next we check our Pods should be starting up and downloading their containers. Once the kubelet has started, you can check it's creating its pods via the metadata api. There should be four PODs, kube-apiserver, kube-controller-manager, kube-proxy and kube-scheduler. 
    
    core@core-02 ~ $ curl -s localhost:10255/pods | jq -r '.items[].metadata.name'
    kube-apiserver-172.17.8.102
    kube-controller-manager-172.17.8.102
    kube-proxy-172.17.8.102
    kube-scheduler-172.17.8.102
    
    core@core-02 /etc/systemd/system $ docker ps
    CONTAINER ID        IMAGE                                      COMMAND                  CREATED             STATUS              PORTS               NAMES
    d20f65d5eaac        quay.io/coreos/hyperkube                   "/hyperkube proxy ..."   6 minutes ago       Up 6 minutes                            k8s_kube-proxy_kube-proxy-172.17.8.102_kube-system_b746724e282bfb90131d57df30375f98_4
    fef5416eed3f        quay.io/coreos/hyperkube                   "/hyperkube apiser..."   6 minutes ago       Up 6 minutes                            k8s_kube-apiserver_kube-apiserver-172.17.8.102_kube-system_12327280fb3060d12bef125b7eec7345_4
    56649b7bcb32        quay.io/coreos/hyperkube                   "/hyperkube contro..."   6 minutes ago       Up 6 minutes                            k8s_kube-controller-manager_kube-controller-manager-172.17.8.102_kube-system_dd33222f8ef11e61dc945aea3f1da733_5
    c5c3a5ae943f        gcr.io/google_containers/pause-amd64:3.0   "/pause"                 6 minutes ago       Up 6 minutes                            k8s_POD_kube-controller-manager-172.17.8.102_kube-system_dd33222f8ef11e61dc945aea3f1da733_4
    a72a06d7589b        gcr.io/google_containers/pause-amd64:3.0   "/pause"                 6 minutes ago       Up 6 minutes                            k8s_POD_kube-proxy-172.17.8.102_kube-system_b746724e282bfb90131d57df30375f98_4
    a23b03cabb9a        gcr.io/google_containers/pause-amd64:3.0   "/pause"                 6 minutes ago       Up 6 minutes                            k8s_POD_kube-apiserver-172.17.8.102_kube-system_12327280fb3060d12bef125b7eec7345_4
    be440c4c9bda        quay.io/coreos/hyperkube                   "/hyperkube schedu..."   6 minutes ago       Up 6 minutes                            k8s_kube-scheduler_kube-scheduler-172.17.8.102_kube-system_fa3811c223367ac4d37eb181f83a8aac_5
    bcb25af8bd49        gcr.io/google_containers/pause-amd64:3.0   "/pause"                 6 minutes ago       Up 6 minutes                            k8s_POD_kube-scheduler-172.17.8.102_kube-system_fa3811c223367ac4d37eb181f83a8aac_4

At this point, we have successfully set up the K8S Master Node on core-02. Next we will set up Worker Node on core-03 & core-04. 

### Step B5 - Deploy K8S Worker Node Components

In this step, we deploy K8S Worker Node on both core-03 & core-4. The detailed steps are shown as be executed on core-03 and just need to logically repeat the same on core-04. 

First we prepare TSL certificates/assets on core-03 for the Worker Node component

    core@core-03 ~ $ sudo mkdir -p /etc/kubernetes/ssl
    core@core-03 ~ $ cd /etc/kubernetes/ssl
    core@core-03 /etc/kubernetes/ssl $ sudo cp /home/core/share/certificates/ca.pem .
    core@core-03 /etc/kubernetes/ssl $ sudo cp /home/core/share/certificates/core-03-worker.pem .
    core@core-03 /etc/kubernetes/ssl $ sudo cp /home/core/share/certificates/core-03-worker-key.pem .
    core@core-03 /etc/kubernetes/ssl $ sudo chmod 600 *-key.pem

Then create symlinks to the worker-specific certificate and key so that the remaining configurations on the workers do not have to be unique per worker.

    core@core-03 ~ $ cd /etc/kubernetes/ssl/
    core@core-03 /etc/kubernetes/ssl $ sudo ln -s core-03-worker.pem worker.pem
    core@core-03 /etc/kubernetes/ssl $ sudo ln -s core-03-worker-key.pem worker-key.pem

In order for flannel to manage the pod network in the cluster, Docker needs to be configured to use flannel. So we need to do three things here: 

    a. use systend drop-in to configure flanneld running prior to Docker starting
    b. create Docker CNI (Containter Network Interface) options file
    c. set up flannel CNI configuration file (Please note, we choose to use Flannel instead of Calico for container networking)

    core@core-03 ~ $ systemctl status docker
    (Before change, docker service is not active)
    ● docker.service - Docker Application Container Engine
       Loaded: loaded (/run/torcx/unpack/docker/lib/systemd/system/docker.service; linked; vendor preset: disabled)
       Active: inactive (dead)
         Docs: http://docs.docker.com
    core@core-03 ~ $ sudo mkdir -p /etc/systemd/system/docker.service.d
    core@core-03 ~ $ cd /etc/systemd/system/docker.service.d
    core@core-03 /etc/systemd/system/docker.service.d $ sudo vi 40-flannel.conf
    (Add the following lines)
    [Unit]
    Requires=flanneld.service
    After=flanneld.service
    [Service]
    EnvironmentFile=/etc/kubernetes/cni/docker_opts_cni.env
    core@core-03 ~ $ sudo mkdir /etc/kubernetes/cni
    core@core-03 ~ $ cd /etc/kubernetes/cni/
    core@core-03 /etc/kubernetes/cni $ sudo vi docker_opts_cni.env
    (Add the following lines)
    DOCKER_OPT_BIP=""
    DOCKER_OPT_IPMASQ=""
    core@core-03 /etc/kubernetes/cni $ sudo mkdir net.d
    core@core-03 /etc/kubernetes/cni $ cd net.d/
    core@core-03 /etc/kubernetes/cni/net.d $ sudo vi 10-flannel.conf
    (Add the following lines)
    {
        "name": "podnet",
        "type": "flannel",
        "delegate": {
            "isDefaultGateway": true
        }
    }
    core@core-03 ~ $ sudo systemctl daemon-reload
    core@core-03 ~ $ sudo systemctl stop docker
    core@core-03 ~ $ sudo systemctl stop docker-tcp.socket
    core@core-03 ~ $ sudo systemctl start docker-tcp.socket
    core@core-03 ~ $ sudo systemctl start docker

Now we create kubelet unit on workder node. The following kubelet service unit file uses the following environment variables. Apart from that, it's crutical to set --network-plugin=cni otherwise the docker containers spined up by Kubelet will not use Flannel for routing and it will fail the whole K8S cluster. 

    ${MASTER_HOST} = 172.17.8.102
    ${ADVERTISE_IP} = 172.17.8.103
    ${DNS_SERVICE_IP} = 10.3.0.10
    ${K8S_VER} =  v1.6.1_coreos.0
    
    core@core-03 ~ $ cd /etc/systemd/system
    core@core-03 /etc/systemd/system $ sudo vi kubelet.service
    (Add the following lines)
    [Service]
    Environment=KUBELET_IMAGE_TAG=v1.6.1_coreos.0
    Environment="RKT_RUN_ARGS=--uuid-file-save=/var/run/kubelet-pod.uuid \
      --volume dns,kind=host,source=/etc/resolv.conf \
      --mount volume=dns,target=/etc/resolv.conf \
      --volume var-log,kind=host,source=/var/log \
      --mount volume=var-log,target=/var/log"
    ExecStartPre=/usr/bin/mkdir -p /etc/kubernetes/manifests
    ExecStartPre=/usr/bin/mkdir -p /var/log/containers
    ExecStartPre=-/usr/bin/rkt rm --uuid-file=/var/run/kubelet-pod.uuid
    ExecStart=/usr/lib/coreos/kubelet-wrapper \
      --api-servers=https://172.17.8.102 \
      --cni-conf-dir=/etc/kubernetes/cni/net.d \
      --network-plugin=cni \
      --container-runtime=docker \
      --register-node=true \
      --allow-privileged=true \
      --pod-manifest-path=/etc/kubernetes/manifests \
      --hostname-override=172.17.8.103 \
      --cluster_dns=10.3.0.10 \
      --cluster_domain=cluster.local \
      --kubeconfig=/etc/kubernetes/worker-kubeconfig.yaml \
      --tls-cert-file=/etc/kubernetes/ssl/worker.pem \
      --tls-private-key-file=/etc/kubernetes/ssl/worker-key.pem
    ExecStop=-/usr/bin/rkt stop --uuid-file=/var/run/kubelet-pod.uuid
    Restart=always
    RestartSec=10
    
    [Install]
    WantedBy=multi-user.target

Now we set up kube-proxy Pod YAML file. 

The following YAML file for kub-proxy POD uses the following environment variable;

    ${MASTER_HOST} = 172.17.8.102
    ${K8S_VER} =  v1.6.1_coreos.0
    
    core@core-03 ~ $ sudo mkdir /etc/kubernetes/manifests
    core@core-02 ~ $ cd /etc/kubernetes/manifests
    core@core-02 /etc/kubernetes/manifests $ sudo vi kube-proxy.yaml
    apiVersion: v1
    kind: Pod
    metadata:
      name: kube-proxy
      namespace: kube-system
    spec:
      hostNetwork: true
      containers:
      - name: kube-proxy
        image: quay.io/coreos/hyperkube:v1.6.1_coreos.0
        command:
        - /hyperkube
        - proxy
        - --master=https://172.17.8.102
        - --kubeconfig=/etc/kubernetes/worker-kubeconfig.yaml
        securityContext:
          privileged: true
        volumeMounts:
        - mountPath: /etc/ssl/certs
          name: "ssl-certs"
        - mountPath: /etc/kubernetes/worker-kubeconfig.yaml
          name: "kubeconfig"
          readOnly: true
        - mountPath: /etc/kubernetes/ssl
          name: "etc-kube-ssl"
          readOnly: true
      volumes:
      - name: "ssl-certs"
        hostPath:
          path: "/usr/share/ca-certificates"
      - name: "kubeconfig"
        hostPath:
          path: "/etc/kubernetes/worker-kubeconfig.yaml"
      - name: "etc-kube-ssl"
        hostPath:
          path: "/etc/kubernetes/ssl"

In order to facilitate secure communication between Kubernetes components, kubeconfig can be used to define authentication settings. In this case, the kubelet and proxy are reading this configuration to communicate with the API.

    core@core-03 ~ $ cd /etc/kubernetes/
    core@core-03 /etc/kubernetes $ sudo vi worker-kubeconfig.yaml
    (Add the following lines into this new file)
    apiVersion: v1
    kind: Config
    clusters:
    - name: local
      cluster:
        certificate-authority: /etc/kubernetes/ssl/ca.pem
    users:
    - name: kubelet
      user:
        client-certificate: /etc/kubernetes/ssl/worker.pem
        client-key: /etc/kubernetes/ssl/worker-key.pem
    contexts:
    - context:
        cluster: local
        user: kubelet
      name: kubelet-context
    current-context: kubelet-context

Now we can start the Worker services.

    core@core-03 ~ $ sudo systemctl daemon-reload
    core@core-03 ~ $ sudo systemctl start kubelet
    core@core-03 ~ $ sudo systemctl enable flanneld
    core@core-03 ~ $ sudo systemctl enable kubelet
    Created symlink /etc/systemd/system/multi-user.target.wants/kubelet.service → /etc/systemd/system/kubelet.service.


Verify kubelet started and kube proxy also started. 

    core@core-03 ~ $ systemctl status kubelet
    ● kubelet.service
       Loaded: loaded (/etc/systemd/system/kubelet.service; disabled; vendor preset: disabled)
       Active: active (running) since Fri 2017-08-04 10:11:23 UTC; 8min ago
      Process: 1461 ExecStartPre=/usr/bin/rkt rm --uuid-file=/var/run/kubelet-pod.uuid (code=exited, status=254)
      Process: 1459 ExecStartPre=/usr/bin/mkdir -p /var/log/containers (code=exited, status=0/SUCCESS)
      Process: 1456 ExecStartPre=/usr/bin/mkdir -p /etc/kubernetes/manifests (code=exited, status=0/SUCCESS)
     Main PID: 1466 (kubelet)
        Tasks: 13 (limit: 32768)
       Memory: 157.0M
    ...
    (Please note it takes while for kubelet to download ACI before it's fully started)
    core@core-03 ~ $ curl -s localhost:10255/pods | jq -r '.items[].metadata.name'
    kube-proxy-172.17.8.103
    core@core-03 ~ $ docker ps
    CONTAINER ID        IMAGE                                      COMMAND                  CREATED             STATUS              PORTS               NAMES
    3a6b49755f98        quay.io/coreos/hyperkube                   "/hyperkube proxy ..."   9 minutes ago       Up 9 minutes                            k8s_kube-proxy_kube-proxy-172.17.8.103_kube-system_16f5df290df73a44cb4049674da09067_0
    bfa49796eac8        gcr.io/google_containers/pause-amd64:3.0   "/pause"                 10 minutes ago      Up 10 minutes                           k8s_POD_kube-proxy-172.17.8.103_kube-system_16f5df290df73a44cb4049674da09067_0

Repeat the above on another Worker Node -core-04. 

### Step B6 - Set Native Kubectl Client on MacPro

In this step, we will set up native Kubectl client on MacPro which connects to the API Server running on K8S Master Node core-01 to manage K8S cluster. 

In the terminal of the local MacPro, execute the following steps to download kubectl binary for MacOS and set it up. 

    $ curl -O https://storage.googleapis.com/kubernetes-release/release/v1.6.1/bin/darwin/amd64/kubectl
    $ chmod +x kubectl
    $ mv kubectl /usr/local/bin/kubectl

Now we configure kubectl to connect to the target cluster using the following commands. The following environment variables are used in those commands.

    ${MASTER_HOST}=172.17.8.102
    ${CA_CERT}=/Users/jaswang/k8s/coreos-vagrant/certificates/ca.pem
    ${ADMIN_KEY}=/Users/jaswang/k8s/coreos-vagrant/certificates/admin-key.pem
    ${ADMIN_CERT}=/Users/jaswang/k8s/coreos-vagrant/certificates/admin.pem

    MacBook-Pro:~ jaswang$ kubectl config set-cluster default-cluster --server=https://172.17.8.102 --certificate-authority=/Users/jaswang/k8s/coreos-vagrant/certificates/ca.pem
    Cluster "default-cluster" set.
    MacBook-Pro:bin jaswang$ kubectl config set-credentials default-admin --certificate-authority=/Users/jaswang/k8s/coreos-vagrant/certificates/ca.pem --client-key=/Users/jaswang/k8s/coreos-vagrant/certificates/admin-key.pem --client-certificate=/Users/jaswang/k8s/coreos-vagrant/certificates/admin.pem 
    User "default-admin" set.
    MacBook-Pro:bin jaswang$ kubectl config set-context default-system --cluster=default-cluster --user=default-admin
    Context "default-system" created.
    MacBook-Pro:bin jaswang$ kubectl config use-context default-system
    Switched to context "default-system".


Now check that the client is configured properly by using kubectl to inspect the cluster:
    
    MacBook-Pro:bin jaswang$ kubectl get nodes
    NAME           STATUS                     AGE       VERSION
    172.17.8.102   Ready,SchedulingDisabled   2d        v1.7.2+coreos.0
    172.17.8.103   Ready                      16h       v1.7.2+coreos.0
    172.17.8.104   Ready                      12m       v1.7.2+coreos.0
    MacBook-Pro:coreos-vagrant jaswang$ kubectl get pods --all-namespaces
    NAMESPACE     NAME                                   READY     STATUS    RESTARTS   AGE
    kube-system   kube-apiserver-172.17.8.102            1/1       Running   7          3d
    kube-system   kube-controller-manager-172.17.8.102   1/1       Running   8          3d
    kube-system   kube-proxy-172.17.8.102                1/1       Running   7          3d
    kube-system   kube-proxy-172.17.8.103                1/1       Running   1          1d
    kube-system   kube-proxy-172.17.8.104                1/1       Running   1          8h
    kube-system   kube-scheduler-172.17.8.102            1/1       Running   8          3d

Run the following command against each pod above and make sure each pod is working fine. 

    MacBook-Pro:coreos-vagrant jaswang$ kubectl logs kube-proxy-172.17.8.102 --namespace=kube-system
    I0810 12:57:14.474604       1 server.go:225] Using iptables Proxier.
    W0810 12:57:14.475453       1 server.go:469] Failed to retrieve node info: Get http://127.0.0.1:8080/api/v1/nodes/core-02: dial tcp     127.0.0.1:8080: getsockopt: connection refused
    W0810 12:57:14.475537       1 proxier.go:304] invalid nodeIP, initializing kube-proxy with 127.0.0.1 as nodeIP
    I0810 12:57:14.475564       1 server.go:249] Tearing down userspace rules.
    E0810 12:57:14.496793       1 reflector.go:201] k8s.io/kubernetes/pkg/proxy/config/api.go:49: Failed to list *api.Endpoints: Get http://127.0.0.1:8080/api/v1/endpoints?resourceVersion=0: dial tcp 127.0.0.1:8080: getsockopt: connection refused
    E0810 12:57:14.497234       1 reflector.go:201] k8s.io/kubernetes/pkg/proxy/config/api.go:46: Failed to list *api.Service: Get http://127.0.0.1:8080/api/v1/services?resourceVersion=0: dial tcp 127.0.0.1:8080: getsockopt: connection refused
    I0810 12:57:15.595380       1 conntrack.go:81] Set sysctl 'net/netfilter/nf_conntrack_max' to 131072
    I0810 12:57:15.595725       1 conntrack.go:66] Setting conntrack hashsize to 32768
    I0810 12:57:15.595841       1 conntrack.go:81] Set sysctl 'net/netfilter/nf_conntrack_tcp_timeout_established' to 86400
    I0810 12:57:15.595878       1 conntrack.go:81] Set sysctl 'net/netfilter/nf_conntrack_tcp_timeout_close_wait' to 3600

### Step B7 - Deploy Kube-DNS into K8S Cluster

In this step, we will deploy Kubernetes DNS service into K8S cluster. The Kube-DNS add-on allows your services to have a DNS name in addition to an IP address. This is helpful for simplified service discovery between applications. 

First we create the file of dns-addon.yml on the local MacPro laptop and then use kubectl client on MacPro to deploy it into K8S cluster. The YAML definition is based on the upstream DNS addon in the Kubernetes addon folder.

The file below use the following environment variable
    
    ${DNS_SERVICE_IP}=10.3.0.10
    ${DOMAIN_NAME}=cluster.local
```
MacBook-Pro:~ jaswang$ cd /Users/jaswang/k8s/coreos-vagrant
MacBook-Pro:coreos-vagrant jaswang$ mkdir add-ons
MacBook-Pro:coreos-vagrant jaswang$ cd add-ons/
MacBook-Pro:add-ons jaswang$ vi dns-addon.yml
(Add the following lines into this new file)
apiVersion: v1
kind: Service
metadata:
  name: kube-dns
  namespace: kube-system
  labels:
    k8s-app: kube-dns
    kubernetes.io/cluster-service: "true"
    kubernetes.io/name: "KubeDNS"
spec:
  selector:
    k8s-app: kube-dns
  clusterIP: 10.3.0.10
  ports:
  - name: dns
    port: 53
    protocol: UDP
  - name: dns-tcp
    port: 53
    protocol: TCP


---

apiVersion: extensions/v1beta1
kind: Deployment
metadata:
  name: kube-dns
  namespace: kube-system
  labels:
    k8s-app: kube-dns
    kubernetes.io/cluster-service: "true"
spec:
  # replicas: not specified here:
  # 1. In order to make Addon Manager do not reconcile this replicas parameter.
  # 2. Default is 1.
  # 3. Will be tuned in real time if DNS horizontal auto-scaling is turned on.
  replicas: 1
  strategy:
    rollingUpdate:
      maxSurge: 10%
      maxUnavailable: 0
  selector:
    matchLabels:
      k8s-app: kube-dns
  template:
    metadata:
      labels:
        k8s-app: kube-dns
      annotations:
        scheduler.alpha.kubernetes.io/critical-pod: ''
        scheduler.alpha.kubernetes.io/tolerations: '[{"key":"CriticalAddonsOnly", "operator":"Exists"}]'
    spec:
      containers:
      - name: kubedns
        image: gcr.io/google_containers/kubedns-amd64:1.9
        resources:
          # TODO: Set memory limits when we've profiled the container for large
          # clusters, then set request = limit to keep this container in
          # guaranteed class. Currently, this container falls into the
          # "burstable" category so the kubelet doesn't backoff from restarting it.
          limits:
            memory: 170Mi
          requests:
            cpu: 100m
            memory: 70Mi
        livenessProbe:
          httpGet:
            path: /healthz-kubedns
            port: 8080
            scheme: HTTP
          initialDelaySeconds: 60
          timeoutSeconds: 5
          successThreshold: 1
          failureThreshold: 5
        readinessProbe:
          httpGet:
            path: /readiness
            port: 8081
            scheme: HTTP
          # we poll on pod startup for the Kubernetes master service and
          # only setup the /readiness HTTP server once that's available.
          initialDelaySeconds: 3
          timeoutSeconds: 5
        args:
        - --domain=cluster.local.
        - --dns-port=10053
        - --config-map=kube-dns
        # This should be set to v=2 only after the new image (cut from 1.5) has
        # been released, otherwise we will flood the logs.
        - --v=0
        env:
        - name: PROMETHEUS_PORT
          value: "10055"
        ports:
        - containerPort: 10053
          name: dns-local
          protocol: UDP
        - containerPort: 10053
          name: dns-tcp-local
          protocol: TCP
        - containerPort: 10055
          name: metrics
          protocol: TCP
      - name: dnsmasq
        image: gcr.io/google_containers/kube-dnsmasq-amd64:1.4
        livenessProbe:
          httpGet:
            path: /healthz-dnsmasq
            port: 8080
            scheme: HTTP
          initialDelaySeconds: 60
          timeoutSeconds: 5
          successThreshold: 1
          failureThreshold: 5
        args:
        - --cache-size=1000
        - --no-resolv
        - --server=127.0.0.1#10053
        - --log-facility=-
        ports:
        - containerPort: 53
          name: dns
          protocol: UDP
        - containerPort: 53
          name: dns-tcp
          protocol: TCP
        # see: https://github.com/kubernetes/kubernetes/issues/29055 for details
        resources:
          requests:
            cpu: 150m
            memory: 10Mi
      - name: dnsmasq-metrics
        image: gcr.io/google_containers/dnsmasq-metrics-amd64:1.0
        livenessProbe:
          httpGet:
            path: /metrics
            port: 10054
            scheme: HTTP
          initialDelaySeconds: 60
          timeoutSeconds: 5
          successThreshold: 1
          failureThreshold: 5
        args:
        - --v=2
        - --logtostderr
        ports:
        - containerPort: 10054
          name: metrics
          protocol: TCP
        resources:
          requests:
            memory: 10Mi
      - name: healthz
        image: gcr.io/google_containers/exechealthz-amd64:1.2
        resources:
          limits:
            memory: 50Mi
          requests:
            cpu: 10m
            # Note that this container shouldn't really need 50Mi of memory. The
            # limits are set higher than expected pending investigation on #29688.
            # The extra memory was stolen from the kubedns container to keep the
            # net memory requested by the pod constant.
            memory: 50Mi
        args:
        - --cmd=nslookup kubernetes.default.svc.cluster.local 127.0.0.1 >/dev/null
        - --url=/healthz-dnsmasq
        - --cmd=nslookup kubernetes.default.svc.cluster.local 127.0.0.1:10053 >/dev/null
        - --url=/healthz-kubedns
        - --port=8080
        - --quiet
        ports:
        - containerPort: 8080
          protocol: TCP
      dnsPolicy: Default # Don't use cluster DNS.
```

Now verify all 4 containers of the kube-dns POD are started successfully and the deployment is in the desired state. Also they are assigned to a proper IP within the range of POD_NETWORK=10.2.0.0/16 which can be routed by Flanneld 

```
MacBook-Pro:coreos-vagrant jaswang$ kubectl get pods --all-namespaces
NAMESPACE     NAME                                   READY     STATUS    RESTARTS   AGE
...
kube-system   kube-dns-v20-xnw3r                     3/3       Running   0          14s
...
MacBook-Pro:coreos-vagrant jaswang$ kubectl get deployment --all-namespaces
NAMESPACE     NAME       DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
kube-system   kube-dns   1         1         1            1           40m
MacBook-Pro:coreos-vagrant jaswang$ kubectl logs kube-dns-321336704-h1vq3 kubedns --namespace=kube-system
I0811 15:24:01.942811       1 dns.go:42] version: v1.6.0-alpha.0.680+3872cb93abf948-dirty
I0811 15:24:01.943334       1 server.go:107] Using https://10.3.0.1:443 for kubernetes master, kubernetes API: <nil>
I0811 15:24:01.943671       1 server.go:68] Using configuration read from ConfigMap: kube-system:kube-dns
I0811 15:24:01.943695       1 server.go:113] FLAG: --alsologtostderr="false"
I0811 15:24:01.943702       1 server.go:113] FLAG: --config-map="kube-dns"
I0811 15:24:01.943707       1 server.go:113] FLAG: --config-map-namespace="kube-system"
I0811 15:24:01.943709       1 server.go:113] FLAG: --dns-bind-address="0.0.0.0"
I0811 15:24:01.943712       1 server.go:113] FLAG: --dns-port="10053"
I0811 15:24:01.943716       1 server.go:113] FLAG: --domain="cluster.local."
I0811 15:24:01.943720       1 server.go:113] FLAG: --federations=""
I0811 15:24:01.943724       1 server.go:113] FLAG: --healthz-port="8081"
I0811 15:24:01.943726       1 server.go:113] FLAG: --kube-master-url=""
I0811 15:24:01.943730       1 server.go:113] FLAG: --kubecfg-file=""
I0811 15:24:01.943732       1 server.go:113] FLAG: --log-backtrace-at=":0"
I0811 15:24:01.943735       1 server.go:113] FLAG: --log-dir=""
I0811 15:24:01.943738       1 server.go:113] FLAG: --log-flush-frequency="5s"
I0811 15:24:01.943751       1 server.go:113] FLAG: --logtostderr="true"
I0811 15:24:01.943754       1 server.go:113] FLAG: --stderrthreshold="2"
I0811 15:24:01.943756       1 server.go:113] FLAG: --v="0"
I0811 15:24:01.943759       1 server.go:113] FLAG: --version="false"
I0811 15:24:01.943763       1 server.go:113] FLAG: --vmodule=""
I0811 15:24:01.943771       1 server.go:155] Starting SkyDNS server (0.0.0.0:10053)
I0811 15:24:02.056156       1 server.go:165] Skydns metrics enabled (/metrics:10055)
I0811 15:24:02.061828       1 logs.go:41] skydns: ready for queries on cluster.local. for tcp://0.0.0.0:10053 [rcache 0]
I0811 15:24:02.061951       1 logs.go:41] skydns: ready for queries on cluster.local. for udp://0.0.0.0:10053 [rcache 0]
E0811 15:24:32.057712       1 sync.go:105] Error getting ConfigMap kube-system:kube-dns err: Get https://10.3.0.1:443/api/v1/namespaces/kube-system/configmaps/kube-dns: dial tcp 10.3.0.1:443: i/o timeout
E0811 15:24:32.057748       1 dns.go:190] Error getting initial ConfigMap: Get https://10.3.0.1:443/api/v1/namespaces/kube-system/configmaps/kube-dns: dial tcp 10.3.0.1:443: i/o timeout, starting with default values
E0811 15:24:32.068880       1 reflector.go:199] pkg/dns/dns.go:145: Failed to list *api.Endpoints: Get https://10.3.0.1:443/api/v1/endpoints?resourceVersion=0: dial tcp 10.3.0.1:443: i/o timeout
E0811 15:24:32.068968       1 reflector.go:199] pkg/dns/dns.go:148: Failed to list *api.Service: Get https://10.3.0.1:443/api/v1/services?resourceVersion=0: dial tcp 10.3.0.1:443: i/o timeout
I0811 15:24:32.106557       1 server.go:126] Setting up Healthz Handler (/readiness)
I0811 15:24:32.106661       1 server.go:131] Setting up cache handler (/cache)
I0811 15:24:32.106678       1 server.go:120] Status HTTP port 8081
MacBook-Pro:coreos-vagrant jaswang$ kubectl describe pod kube-dns-321336704-h1vq3 kubedns --namespace=kube-system
Name:		kube-dns-321336704-h1vq3
Namespace:	kube-system
Node:		172.17.8.104/172.17.8.104
Start Time:	Sat, 12 Aug 2017 00:35:59 +1000
Labels:		k8s-app=kube-dns
		pod-template-hash=321336704
Annotations:	kubernetes.io/created-by={"kind":"SerializedReference","apiVersion":"v1","reference":{"kind":"ReplicaSet","namespace":"kube-system","name":"kube-dns-321336704","uid":"651ddba8-7ea2-11e7-a33c-080027b42...
		scheduler.alpha.kubernetes.io/critical-pod=
		scheduler.alpha.kubernetes.io/tolerations=[{"key":"CriticalAddonsOnly", "operator":"Exists"}]
Status:		Running
IP:		10.2.101.2   (Please note this POD IP must be in the same rage of the flannel & cni interfaces on that VM for routing to the POD)
Controllers:	ReplicaSet/kube-dns-321336704
...
MacBook-Pro:coreos-vagrant jaswang$ kubectl get ep kube-dns --namespace=kube-system

NAME       ENDPOINTS                     AGE
kube-dns   10.2.101.2:53,10.2.101.2:53   9h
```
In the check above, we found the DNS POD is assigned with the POD IP of 10.2.101.2, which is in the range of POD_NETWORK=10.2.0.0/16 and also consistent with the IPs of the flannel & cni interfaces on that VM as shown below. This must be true so that the traffic to that POD can be properly routed by flanneld. 
```
MacBook-Pro:coreos-vagrant jaswang$ vagrant ssh core-04
core@core-04 ~ $ ifconfig
cni0: flags=4163<UP,BROADCAST,RUNNING,MULTICAST>  mtu 1450
        inet 10.2.101.1  netmask 255.255.255.0  broadcast 0.0.0.0
...
flannel.1: flags=4163<UP,BROADCAST,RUNNING,MULTICAST>  mtu 1450
        inet 10.2.101.0  netmask 255.255.255.255  broadcast 0.0.0.0
...
```
Now we test the Kubernets DNS, first we create the busybox POD which will be used in the DSN test. 
```
MacBook-Pro:coreos-vagrant jaswang$ vi busybox.yaml
(add the following lines into this new file)
apiVersion: v1
kind: Pod
metadata:
  name: busybox
  namespace: default
spec:
  containers:
  - image: busybox
    command:
      - sleep
      - "3600"
    imagePullPolicy: IfNotPresent
    name: busybox
  restartPolicy: Always
MacBook-Pro:coreos-vagrant jaswang$ kubectl create -f ./busybox.yaml 
pod "busybox" created
```
Then we use busybox POD to test the Kube-DNS can resolve the K8S Services to K8S Service Cluster IP address. First we check out the current existing services. 
```
MacBook-Pro:coreos-vagrant jaswang$ kubectl get services --all-namespaces
NAMESPACE     NAME         CLUSTER-IP   EXTERNAL-IP   PORT(S)         AGE
default       kubernetes   10.3.0.1     <none>        443/TCP         5d
kube-system   kube-dns     10.3.0.10    <none>        53/UDP,53/TCP   45m
```
So currently the cluster has two K8S Services as above. So their Service DNS names are kubernetes.default and kube-dns.kube-system respectively and the IP mapping would be:

    kubernetes.default (or kubernetes.default.svc.cluster.local) ==> 10.3.0.1
    kube-dns.kube-system (or kube-dsn.kube-system.svc.cluster.local) ==> 10.3.0.10
    
Now we execute the DNS command to verify the kube-DNS servere
```
MacBook-Pro:coreos-vagrant jaswang$ kubectl exec -ti busybox -- nslookup kubernetes.default
Server:    10.3.0.10
Address 1: 10.3.0.10 kube-dns.kube-system.svc.cluster.local

Name:      kubernetes.default
Address 1: 10.3.0.1 kubernetes.default.svc.cluster.local
MacBook-Pro:coreos-vagrant jaswang$ kubectl exec -ti busybox -- nslookup 10.3.0.1
Server:    10.3.0.10
Address 1: 10.3.0.10 kube-dns.kube-system.svc.cluster.local

Name:      10.3.0.1
Address 1: 10.3.0.1 kubernetes.default.svc.cluster.local
MacBook-Pro:coreos-vagrant jaswang$ kubectl exec -ti busybox -- nslookup kube-dns.kube-system
Server:    10.3.0.10
Address 1: 10.3.0.10 kube-dns.kube-system.svc.cluster.local

Name:      kube-dns.kube-system
Address 1: 10.3.0.10 kube-dns.kube-system.svc.cluster.local
MacBook-Pro:coreos-vagrant jaswang$ kubectl exec -ti busybox -- nslookup 10.3.0.10
Server:    10.3.0.10
Address 1: 10.3.0.10 kube-dns.kube-system.svc.cluster.local

Name:      10.3.0.10
Address 1: 10.3.0.10 kube-dns.kube-system.svc.cluster.local
```
### Step B8 - Deploy Kubernetes Dashboard into K8S Cluster

In this step, we will deploy Kubernetes Dashboard add-on according to https://github.com/kubernetes/dashboard. Kubernetes Dashboard is a general purpose, web-based UI for Kubernetes clusters. It allows users to manage applications running in the cluster and troubleshoot. 

The deployment is very simple by kubectl as shown below

```
MacBook-Pro:coreos-vagrant jaswang$ kubectl create -f https://git.io/kube-dashboard
serviceaccount "kubernetes-dashboard" created
clusterrolebinding "kubernetes-dashboard" created
deployment "kubernetes-dashboard" created
service "kubernetes-dashboard" created
MacBook-Pro:coreos-vagrant jaswang$ kubectl proxy &
Starting to serve on 127.0.0.1:8001
```
On your local MacPro web browser, open url of "http://localhost:8001/ui", you will be able to see the Kubernetes Dashboard as below

![Kubernetes Dashboard](/Kubenetes_Dashboard.png?raw=true "Kubernetes Dashboard")

## Section C. Deploy Guestbook Example App into K8S Cluster

Now in this section, we deploy the typical Kubernetes example application - Guestbook into K8S cluster. To make it work, the K8S cluster might be set up and verified as per the steps above. The steps in this section are sourced from  https://github.com/kubernetes/kubernetes/blob/release-1.6/examples/guestbook/README.md. 

The example application consists of:

    A web frontend
    A redis master (for storage), and a replicated set of redis 'slaves'.

The web frontend interacts with the redis master via javascript redis API calls.

### Step C1 - Verify K8S Cluster

First we verify again the K8S cluster status
```
MacBook-Pro:heapster jaswang$ kubectl cluster-info
Kubernetes master is running at https://172.17.8.102
KubeDNS is running at https://172.17.8.102/api/v1/proxy/namespaces/kube-system/services/kube-dns
```
### Step C2 - Start Up Redis Master

To start the redis master, use the file redis-master-deployment.yaml, which describes a single pod running a redis key-value server in a container.

https://github.com/kubernetes/kubernetes/blob/release-1.6/examples/guestbook/redis-master-deployment.yaml

Although we have a single instance of our redis master, we are using a Deployment to enforce that exactly one pod keeps running. E.g., if the node were to go down, the Deployment will ensure that the redis master gets restarted on a healthy node. (In our simplified example, this could result in data loss.)

Also we define a Kubernetes Service for the redis master. This is done using the labels metadata that we defined in the redis-master pod above. As mentioned, we have only one redis master, but we nevertheless want to create a Service for it. Why? Because it gives us a deterministic way to route to the single master using an elastic IP. 

The service yaml file is:

https://github.com/kubernetes/kubernetes/blob/release-1.6/examples/guestbook/redis-master-service.yaml

According to the Kubernetes config best practices, we should create a Service before corresponding Deployments so that the scheduler can spread the pods comprising the Service. So we first create the Service by running:
```
MacBook-Pro:~ jaswang$ cd /Users/jaswang/k8s/coreos-vagrant
MacBook-Pro:coreos-vagrant jaswang$ git clone https://github.com/kubernetes/kubernetes.git -b release-1.6
MacBook-Pro:coreos-vagrant jaswang$ cd kubernetes/
MacBook-Pro:kubernetes jaswang$ kubectl create -f examples/guestbook/redis-master-service.yaml
service "redis-master" created
MacBook-Pro:kubernetes jaswang$ kubectl get services
NAME           CLUSTER-IP   EXTERNAL-IP   PORT(S)    AGE
kubernetes     10.3.0.1     <none>        443/TCP    6d
redis-master   10.3.0.60    <none>        6379/TCP   32s
```
Then we create the redis master pod in the Kubernetes cluster by running:
```
MacBook-Pro:kubernetes jaswang$ kubectl create -f examples/guestbook/redis-master-deployment.yaml 
deployment "redis-master" created
MacBook-Pro:kubernetes jaswang$ kubectl get deployments
NAME           DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
redis-master   1         1         1            1           8s
MacBook-Pro:kubernetes jaswang$ kubectl get pods
NAME                            READY     STATUS    RESTARTS   AGE
redis-master-1068406935-gtkdr   1/1       Running   0          43s
```

### Step C3 - Start Up Redis Slave

Now that the redis master is running, we can start up its 'read slaves'.

We'll define these as replicated pods as well, though this time — unlike for the redis master — we'll define the number of replicas to be 2. In Kubernetes, a Deployment is responsible for managing multiple instances of a replicated pod. The Deployment will automatically launch new pods if the number of replicas falls below the specified number. Its depoyment YAML file is https://github.com/kubernetes/kubernetes/blob/release-1.6/examples/guestbook/redis-slave-deployment.yaml.  

Just like the master, we want to have a Service to proxy connections to the redis slaves. In this case, in addition to discovery, the slave Service will provide transparent load balancing to web app clients. Its service YAML file is https://github.com/kubernetes/kubernetes/blob/release-1.6/examples/guestbook/redis-slave-service.yaml

Now we create the redis slave service & pods in the Kubernetes cluster by running:
```
MacBook-Pro:~ jaswang$ cd /Users/jaswang/k8s/coreos-vagrant
MacBook-Pro:coreos-vagrant jaswang$ kubectl create -f ./kubernetes/examples/guestbook/redis-slave-service.yaml 
service "redis-slave" created
MacBook-Pro:coreos-vagrant jaswang$ kubectl create -f ./kubernetes/examples/guestbook/redis-slave-deployment.yaml 
deployment "redis-slave" created
MacBook-Pro:coreos-vagrant jaswang$ kubectl get services -l "app=redis,tier=backend"
NAME           CLUSTER-IP   EXTERNAL-IP   PORT(S)    AGE
redis-master   10.3.0.60    <none>        6379/TCP   15h
redis-slave    10.3.0.197   <none>        6379/TCP   19m
MacBook-Pro:coreos-vagrant jaswang$ kubectl get deployments -l "app=redis,tier=backend"
NAME           DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
redis-master   1         1         1            1           15h
redis-slave    2         2         2            2           1m
MacBook-Pro:coreos-vagrant jaswang$ kubectl get pods -l "app=redis,tier=backend"
NAME                            READY     STATUS    RESTARTS   AGE
redis-master-1068406935-gtkdr   1/1       Running   0          15h
redis-slave-2005841000-kshxr    1/1       Running   0          1m
redis-slave-2005841000-nv5p8    1/1       Running   0          1m
```
### Step C4 - Start up the guestbook frontend

A frontend pod is a simple PHP server that is configured to talk to either the slave or master services, depending on whether the client request is a read or a write. It exposes a simple AJAX interface, and serves an Angular-based UX. Again we'll create a set of replicated frontend pods instantiated by a Deployment — this time, with three replicas. Its deployment YAML file is https://github.com/kubernetes/kubernetes/blob/release-1.6/examples/guestbook/frontend-deployment.yaml. 

Similarly to the other pods, we now want to create a Service to group the frontend pods. Its service YAML file is https://github.com/kubernetes/kubernetes/blob/release-1.6/examples/guestbook/frontend-service.yaml. 

```
MacBook-Pro:~ jaswang$ cd /Users/jaswang/k8s/coreos-vagrant
MacBook-Pro:coreos-vagrant jaswang$ vi kubernetes/examples/guestbook/frontend-service.yaml
(Add the line of "type: NodePort")
MacBook-Pro:coreos-vagrant jaswang$ kubectl create -f kubernetes/examples/guestbook/frontend-service.yaml
service "frontend" created
MacBook-Pro:coreos-vagrant jaswang$ kubectl create -f kubernetes/examples/guestbook/frontend-deployment.yaml 
deployment "frontend" created
MacBook-Pro:coreos-vagrant jaswang$ kubectl get services -l "app=guestbook"
NAME       CLUSTER-IP   EXTERNAL-IP   PORT(S)        AGE
frontend   10.3.0.139   <nodes>       80:30967/TCP   5m
MacBook-Pro:coreos-vagrant jaswang$ kubectl get deployments -l "app=guestbook"
NAME       DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
frontend   3         3         3            3           5m
MacBook-Pro:coreos-vagrant jaswang$ kubectl get pods -l "app=guestbook"
NAME                        READY     STATUS    RESTARTS   AGE
frontend-3823415956-1stps   1/1       Running   0          5m
frontend-3823415956-b0km6   1/1       Running   0          5m
frontend-3823415956-rmxbf   1/1       Running   0          5m
```

Then on the local MacPro laptop, open browser window of "http://<any_VM_IP>:30967", such as http://172.17.8.102:30967/, to access the Guestbook UI front as shown below: 

![Guestbook UI](/GuestBookUI.png?raw=true "Kubernetes App Example Guestbook")

This proves that the Kubernetes Example App of GuestBook has now beeen deployed to the K8S cluster successfully. 

By now we have successfully deployed a multi-nod eK8S cluster with Vagrant and Virtualbox and deployed an example App of GuestBook to prove the K8S cluster is ready to serve. 






















 
 



    
    


    



