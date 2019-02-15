#!/bin/bash

source "/vagrant/scripts/common.sh"

function stopDaemons {
    echo "stopping Hadoop daemons"
    $HADOOP_PREFIX/sbin/mr-jobhistory-daemon.sh stop historyserver --config $HADOOP_CONF_DIR
    $HADOOP_YARN_HOME/sbin/yarn-daemon.sh stop proxyserver --config $HADOOP_CONF_DIR
    $HADOOP_YARN_HOME/sbin/yarn-daemons.sh stop nodemanager --config $HADOOP_CONF_DIR 
    $HADOOP_YARN_HOME/sbin/yarn-daemon.sh stop resourcemanager --config $HADOOP_CONF_DIR 
    $HADOOP_PREFIX/sbin/hadoop-daemons.sh --script hdfs stop datanode --config $HADOOP_CONF_DIR 
    $HADOOP_PREFIX/sbin/hadoop-daemon.sh --script hdfs stop namenode --config $HADOOP_CONF_DIR 

    echo "listing all Java processes"
    jps
}

stopDaemons
