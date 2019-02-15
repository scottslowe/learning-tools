Vagrant Docker for Hadoop, Spark and Hive
=========================================

# Introduction

Vagrant project to spin up a single virtual machine running:

* Hadoop 
* Hive 
* HBase 
* Spark  
* Tez 

# Version Information
The versions of the above components that the VM is provisioned with are defined in the file `scripts/versions.sh`

The following version combinations are known to work: -

1. Spark-2.1.1 based on: -
    * Hadoop 2.7.3
    * Hive 1.2.2
    * Spark 2.1.1
    * Tez 0.8.5
    * Sqoop 1.4.6
    * Pig 0.17.0
    * flume 1.7.0
    * Zeppelin 0.8.0 (with Spark/scala, md, file and JDBC interpreters)
    
2. Spark-2.3.0 based on: -
    * Hadoop 2.7.6
    * Hive 2.3.3
    * Spark 2.3.0
    * Tez 0.9.1
    * Sqoop 1.4.6
    * Pig 0.17.0
    * flume 1.7.0
    * Zeppelin 0.8.0 (with Spark/scala, md, file and JDBC interpreters)

# Services
The virtual machine will be running the following services:

* HDFS NameNode + DataNode
* YARN ResourceManager/NodeManager + JobHistoryServer + ProxyServer
* Hive metastore and server2
* Spark history server
* Hbase server

# Getting Started

1. Download and install [VirtualBox](https://www.virtualbox.org/wiki/Downloads) and/or [Docker](https://www.docker.com/)
2. Download and install [Vagrant](http://www.vagrantup.com/downloads.html).
3. Clone this repo.
4. Check the `Vagrentfile` and comment/uncomment the optional components as required (Pig/Sqoop/HBase/Zeppelin/flume).
5. Check the `scripts/versions.sh` file for the versions of the components.
6. In your terminal change your directory into the project directory (i.e. `cd vagrant-hadoop-spark-hive`).
7. Run `vagrant up --provider=virtualbox` to create the VM using virtualbox as a provider. Or run `vagrant up --provider=docker` to use docker as a provider. (**NOTE** *This will take a while the first time as many dependencies are downloaded - subsequent deployments will be quicker as dependencies are cached in the `resources` directory*).
8. Execute ```vagrant ssh``` to login to the VM.

# Work out the ip-address of the docker container
To access the web user interfaces of the various services from your host machine, you need to work out which ip address to connect to. To determine this run the following docker commands 
on the host: -

```
docker container ls

CONTAINER ID        IMAGE                                      COMMAND               CREATED             STATUS              PORTS                                                                                                                    NAMES
a44ca8ded5b8        nishidayuya/docker-vagrant-ubuntu:xenial   "/usr/sbin/sshd -D"   About an hour ago   Up About an hour    0.0.0.0:4040->4040/tcp, 0.0.0.0:8080->8080/tcp, 0.0.0.0:8088->8088/tcp, 0.0.0.0:9083->9083/tcp, 127.0.0.1:2222->22/tcp   vagrant-hadoop-hive-spark_node1_1539427474
```

then

```
docker inspect a44ca8ded5b8 | grep -i ipaddress
            "SecondaryIPAddresses": null,
            "IPAddress": "172.17.0.2",
                    "IPAddress": "172.17.0.2",
```

So, in the case above the container's ip address is 172.17.0.2 - you can substitute this address if 'node1' does not work.

# Work out the ip-address of the virtualbox VM
The ip address of the virtualbox machine should be `10.211.55.101`

# Map Reduce - Tez
By default map reduce jobs will be executed via Tez to change this to standard MR, change the following parameter in $HADOOP_CONF/mapred-site.xml from: -

```xml
    <property>
        <name>mapreduce.framework.name</name>
        <value>yarn-tez</value>
    </property>
```

to

```xml
    <property>
        <name>mapreduce.framework.name</name>
        <value>yarn</value>
    </property>
```

# Web user interfaces

Here are some useful links to navigate to various UI's:

* YARN resource manager:  (http://node1:8088)
* HBase: (http://node1:16010)
* Job history:  (http://node1:19888/jobhistory/)
* HDFS: (http://node1:50070/dfshealth.html)
* Spark history server: (http://node1:18080)
* Spark context UI (if a Spark context is running): (http://node1:4040)

Substitute the ip address of the container or virtualbox VM for `node1` if necessary.

# Shared Folder

Vagrant automatically mounts the folder containing the Vagrant file from the host machine into
the guest machine as `/vagrant` inside the guest.


# Validating your virtual machine setup

To test out the virtual machine setup, and for examples of how to run
MapReduce, Hive and Spark, head on over to [VALIDATING.md](VALIDATING.md).


# Managment of Vagrant VM

To stop the VM and preserve all setup/data within the VM: -

```
vagrant halt
```

or

```
vagrant suspend
```

Issue a `vagrant up` command again to restart the VM from where you left off.

To completely **wipe** the VM so that `vagrant up` command gives you a fresh machine: -

```
vagrant destroy
```

Then issue `vagrant up` command as usual.

# To shutdown services cleanly

```
$ vagrant ssh
$ sudo -sE
$ /vagrant/scripts/stop-spark.sh
$ /vagrant/scripts/stop-hbase.sh
$ /vagrant/scripts/stop-hadoop.sh

```

# Swapspace - Memory

Spark in particular needs quite a bit of memory to run - to work around this a `swapspace` daemon is also configured and
started that uses normal disk to dynamically allocate swapspace when memory is low.

# Problems
Sometimes the Spark UI is not available from the host machine when running with virtualbox. Setting: -

```bash
 export SPARK_LOCAL_IP=10.211.55.101
 spark-shell .....
```
Seems to solve this.

# More advanced setup

If you'd like to learn more about working and optimizing Vagrant then
take a look at [ADVANCED.md](ADVANCED.md).

# For developers

The file [DEVELOP.md](DEVELOP.md) contains some tips for developers.

# Credits

Thanks to [Alex Holmes](https://github.com/alexholmes) for the great work at
(https://github.com/alexholmes/vagrant-hadoop-spark-hive)

[Matheus Cunha](https://github.com/matheuscunha)
