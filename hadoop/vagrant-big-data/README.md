# vagrant-big-data

Vagrantfiles for development in big data

# Install

git clone this project to your local computer and cd to one of the directory, then run "vagrant up".

# Features

Allow user to create different devops environment locally using Vagrantfiles

* Zookeeper cluster
* Zookeeper + Kafka
* Storm with Zookeeper + Kafka
* Spark with Zookeeper + Kafka
* Spark with Mesos + Zookeeper + Kafka 

# Usage

### Zookeeper Cluster

cd to the directory "zookeeper" under the root directory and run "vagrant up". This will start a multi-machine vagrant 
setup in which a zookeeper cluster will be auto-started on 3 VMs. The three ubuntu VMs have the following ip address 
by default (hostname:ip-address):

* zoo1: 192.168.10.12
* zoo2: 192.168.10.13
* zoo3: 192.168.10.14

To login to one of the VMs, say zoo1, run "vagrant ssh zoo1".

To check the status of the zookeeper cluster, run the following command (e.g., from within the zoo1 VM):

```bash
echo stat | nc zoo1 2181
echo stat | nc zoo2 2181
echo stat | nc zoo3 2181
```

The log of the zookeeper is located in the VMs at "/var/log/zookeeper/zookeeper.log"
The zoo.cfg for configuration is located in the VMs at "/etc/zookeeper/conf/zoo.cfg"

Run "vagrant suspend" or "vagrant resume" to stop or restart the zookeeper cluster.

For the zookeeper VMs, the zookeeper is auto started when the VM is up or resumed.

### Zookeeper+Kafka Cluster

cd to the directory "zookeeper+kafka" under the root directory and run "vagrant up". This will start a multi-machine vagrant 
setup in which a zookeeper cluster and a kafka server will be auto-started in 4 VMs. The 4 ubuntu VMs have the following ip address 
by default (hostname:ip-address):

* zoo1: 192.168.10.12
* zoo2: 192.168.10.13
* zoo3: 192.168.10.14
* kafka1: 192.168.10.15

To login to one of the VMs, say kafka1, run "vagrant ssh kafka1".

For the zookeeper configuration, refers to the earlier section.

For the kafka1, the installation directory is in /opt/kafka,the configuration server.properties can be found in /opt/kafka/config/

For the kafka1, the kafka is auto started when the VM is up or resumed.

Within the VM, the kafka service can be managed by issuing command such as "service kafka start/stop/restart/status"

To check if kafka is running, run the following command in kafka1:

```bash
service kafka status
```

To test the kafka producer, create a topic and send a message to the topic using the following command in kafka1:

```bash
echo "Hello, World" | /opt/kafka/bin/kafka-console-producer.sh --broker-list kafka1:9092 --topic TutorialTopic > /dev/null
```

To test the kafka consumer, run the following command:

```bash 
/opt/kafka/bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic TutorialTopic --from-beginning
```

### Storm Cluster (with Zookeeper + Kafka)

cd to the directory "storm" under the root directory and run "vagrant up". This will start a multi-machine vagrant setup in which a 
storm cluster will be auto-started. The following VMs will be setup to run:

* a zookeeper cluster at the following hostname:ips:

    * zoo1:192.168.10.12
    * zoo2:192.168.10.13
    * zoo3:192.168.10.14

* a kafka server at the following hostname:ip (which use the zookeeper cluster above):

    * kafka1:192.168.10.15
    
* a storm cluster at the following hostname:ips (which uses the zookeeper cluster above):

    * stormnimbus1:192.168.10.17
    * stormslave1:192.168.10.18
    * stormslave2:192.168.10.19
    * stormslave3:192.168.10.20
    
For each of the storm VMs, the storm is installed at /opt/storm, and the configuration is at /opt/storm/conf/storm.yaml. 

Within stormnimbus1 VM, the storm nimbus and storm ui is auto started when the VM is up or resumed

Within the stormnimbus1 VM, you can issue command such as as "service storm-nimbus start/stop/restart/status" 
and "service storm-ui start/stop/restart/status"

Within stormslave[x] VM, the storm supervisor is auto started when the VM is up.

Within the stormslave[x] VMs, you can issue command such as "service storm-supervisor start/stop/restart/status"

Once the vagrant VMs are up and running, you can go to your host computer and enter 
the url http://192.168.10.17:8080 to your browser on the host computer. This will show the storm UI.

### Hadoop Cluster

cd to the directory "hadoop" under the root directory and run "vagrant up". This will start a multi-machine vagrant setup in which a 
hdfs cluster will be auto-started. The following VMs will be setup to run:



### Spark Cluster

cd to the directory "spark" under the root directory and run "vagrant up". This will start a multi-machine vagrant setup in which a 
spark cluster will be auto-started. The following VMs will be setup to run:
    
* a spark cluster at the following hostname:ips (which uses the zookeeper cluster above):

    * sparkmaster:192.168.10.21
    * sparkslave1:192.168.10.22
    * sparkslave2:192.168.10.23
    * sparkslave3:192.168.10.24
    
For each of the spark VMs, the spark is installed at /opt/spark, and the configuration is at /opt/spark/conf/slaves. 

Within sparkmaster1 VM, the spark cluster is auto started when the VM is up or resumed

Within the sparkmaster1 VM, you can issue command such as as "service spark start/stop/restart/status" 

Once the vagrant VMs are up and running, you can go to your host computer and enter 
the url http://192.168.10.21:4040 to your browser on the host computer. This will show the spark UI.

### Spark Cluster (with Zookeeper + Kafka)

cd to the directory "spark+zookeeper+kafka" under the root directory and run "vagrant up". This will start a multi-machine vagrant setup in which a 
spark cluster will be auto-started. The following VMs will be setup to run:

* a zookeeper cluster at the following hostname:ips:

    * zoo1:192.168.10.12
    * zoo2:192.168.10.13
    * zoo3:192.168.10.14

* a kafka server at the following hostname:ip (which use the zookeeper cluster above):

    * kafka1:192.168.10.15
    
* a spark cluster at the following hostname:ips (which uses the zookeeper cluster above):

    * sparkmaster:192.168.10.21
    * sparkslave1:192.168.10.22
    * sparkslave2:192.168.10.23
    * sparkslave3:192.168.10.24
    
For each of the spark VMs, the spark is installed at /opt/spark, and the configuration is at /opt/spark/conf/slaves. 

Within sparkmaster1 VM, the spark cluster is auto started when the VM is up or resumed

Within the sparkmaster1 VM, you can issue command such as as "service spark start/stop/restart/status" 

Once the vagrant VMs are up and running, you can go to your host computer and enter 
the url http://192.168.10.21:4040 to your browser on the host computer. This will show the spark UI.



