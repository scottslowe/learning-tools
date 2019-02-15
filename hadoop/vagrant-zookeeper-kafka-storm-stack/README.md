# Virtual Zookeeper-Kafka-Storm cluster environment

## Cluster specs

- System: 6x Ubuntu 14.04 server
- Memory: 1024 MB each host
- IP address: 192.168.5.100-140

You can easily customize the cluster parameters in the following files:
- `Vagrantfile` contains parameters for the system, memory, IP addresses and hostnames

*Note regarding the cluster size*: It is recommended to run the cluster on an odd number of hosts (3, 5, etc). Zookeeper is designed to survive failure of minority of hosts. Zookeeper running on 3 or 4 hosts can survive failure of 1 host while Zookeper running on 5 hosts can survive failure of 2 hosts.

## 1. Install Vagrant, VirtualBox and Ansible on your machine

1. Install [VirtualBox](https://www.virtualbox.org/)
2. Install [Vagrant](https://www.vagrantup.com/)
3. Install [Ansible](https://www.ansible.com/)
4. Clone this repository
5. Enter cloned repository
6. Execute `vagrant provision && vagrant up`


# Zookeeper

This script creates a virtual 3-node [Apache ZooKeeper](http://zookeeper.apache.org/)
cluster on your local machine using [Vagrant](https://www.vagrantup.com/), [VirtualBox](https://www.virtualbox.org/) and [Ansible](http://www.ansible.com/home).

The Zookeeper service will run in a truly replicated mode over several machines, so you can experiment with host failures, client connections, misc cluster configurations, etc.

Also, if you want to create a virtual cluster for other services built on top of Zookeeper (e.g. Apache Kafka, HBase, Solr, Neo4j), this script might help as a starting point.

__Note__: This is a proof of concept and test installation only. Do ot use in production.

## Specs

- IP address: 192.168.5.100 - 192.168.5.102
- Hostnames: `zookeeper-node-[x]` with `x` have values 1, 2 or 3
- Zookeeper version: 3.4.11
- JVM: Oracle Java-8

## Installation

Execute `vagrant provision && vagrant up`

### Shotdown and restart

With

- `vagrant halt`

you stop the cluster and can restart the cluster with

- `vagrant reload`


### Test if the Zookeeper is running

Each VM should now have the Zookeeper running on port 2181. Test that the service is running in non-error state by:
```
echo ruok | nc  192.168.5.100 2181
```

The server should respond with `imok`.

*If interested, checkout the [list of all our-letter commands](http://zookeeper.apache.org/doc/current/zookeeperAdmin.html#sc_zkCommands) supported by the Zookeeper service*


## First steps with distributed Zookeeper

The easiest way to experiment with Zookeeper is to log into one of the machines
and play with the [command-line tool](http://zookeeper.apache.org/doc/current/zookeeperStarted.html#sc_ConnectingToZooKeeper) shipped with the Zookeeper:

### 1. Log into one of the host machines, e.g. `zookeeper-node-1`
```
vagrant ssh zookeeper-node-1
```

### 2. Connect to Zookeeper running on localhost with the `zkCli.sh` command-line tool
```
vagrant@zookeeper-node-1:~$ cd /opt/zookeeper-3.4.11/bin/
vagrant@zookeeper-node-1:/opt/zookeeper-3.4.11/bin$ ./zkCli.sh -server localhost:2181
```

### 3. Create a test znode in the Zookeeper console
```
[zk: localhost:2181(CONNECTED) 3] create /zk_test my_data
Created /zk_test
```

### 4. Exit the console and connect to Zookeeper running on `zookeeper-node-1` (which runs on `192.168.5.101`)
```
vagrant@zookeeper-node-2:/opt/zookeeper-3.4.11/bin$ ./zkCli.sh -server 192.168.5.101:2181
```

### 5. Check if the znode `zk_test` is seen on `zookeeper-node-1`
```
[zk: 192.168.5.101:2181(CONNECTED) 0] ls /
[zookeeper, zk_test]
[zk: 192.168.5.101:2181(CONNECTED) 1] get /zk_test
my_data
```

## Shotdown and restart

Stop the cluster with

- `vagrant halt`

Restart the cluster with 

- `vagrant reload`


### Restart ZooKeeper

__Note__: After restart you must also start the ZooKeeper server manual. Automatic restart is yet not configured. I'll implement this in a further version.

Steps to restart Zookepper:

1. Login to **node 1**: `vagrant ssh zookeeper-node-1`
2. Start ZooKeeper: `sudo service zookeeper start`
3. Quit **node-1**: `exit`

Repeat with **node-2**.


# Apache Kafka

Creates a 1 node environment for Apache Kafka

__Note__: This is a proof of concept and test installation only. Do ot use in production.

# Specs

- IP address: 192.168.5.110, 192.168.5.111
- Hostnames: `kafka-node-1` 
- [Kafka](http://kafka.apache.org/) version: 1.0.0
- JVM: Oracle Java-8

# Installation

Execute `vagrant provision && vagrant up`

## Shotdown and restart

With

- `vagrant halt`

you stop the cluster and can restart the cluster with

- `vagrant reload`

# Test setup

1. Connect to Kafka service with: `vagrant ssh kafka-node-1`
2. Change to Kafka `bin` directory: `cd /opt/kafka_2.11-1.0.0/bin
3. List existing topics: `./kafka-topics.sh  --list --zookeeper 192.168.5.100:2181`
4. Create new topic: `./kafka-topics.sh --create --topic web2kafka --zookeeper 192.168.5.100:2181 --partitions 1 --replication-factor 1`<br />Output: `Created topic "firsttopic".`
5. Show created topic: `./kafka-topics.sh  --list --zookeeper 192.168.5.100:2181`<br />Output: `web2kafka`

List all entries in a topic:

1. `./kafka-console-consumer.sh --zookeeper 192.168.5.100:2181 --topic web2kafka --from-beginning`


# Apache Storm

Creates 2 [Storm](http://storm.apache.org/) instances. One Nimbus with UI and one supervisor node.

## Specs

- IP address: 192.168.5.120 -> Nimbus
- IP address: 192.168.5.130 -> Supervisor
- IP address: 192.168.5.140 -> UI
- Hostnames: `storm-nimbus-node-1` 
- Hostnames: `storm-supervisor-node-1` 
- Hostnames: `storm-ui-node-1` 
- Zookeeper version: 3.4.11
- JVM: Oracle Java-8

## Note

At this time of contribution, there is no autostart for the Storm services. Start the services with the following steps

1. Login to Nimbus with `vagrant ssh storm-nimbus-node-1`
2. Get root user with `su`. Password: `vagrant`
3. Change to director `/opt/apache-storm-1.1.1/bin`
4. Execute `./storm nimbus &`
5. Exit host

Repeat with `storm-supervisor-node-1`

Instead of executing `./storm nimbus &` execute `./storm supervisor &`

Run UI on the Nimbus node by executing `./storm ui &`

## Storm Topology

Find a Storm topology to read from Kafka (topic: `web2kafka`) under `client/storm`.
Build the project with `mvn package` and deploy the artifact on nimbus with
`./storm jar /PATHTOJAR/kafka2storm.jar de.speexx.experimental.storm.Topology`.

