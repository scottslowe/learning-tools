#!/bin/bash

source "/vagrant/scripts/common.sh"

function startDaemons {
    echo "starting Hadoop daemons"
    $HADOOP_PREFIX/sbin/hadoop-daemon.sh --config $HADOOP_CONF --script hdfs start namenode
    $HADOOP_PREFIX/sbin/hadoop-daemon.sh --config $HADOOP_CONF --script hdfs start datanode
    $HADOOP_YARN_HOME/sbin/yarn-daemon.sh start resourcemanager --config $HADOOP_CONF 
    $HADOOP_YARN_HOME/sbin/yarn-daemons.sh start nodemanager --config $HADOOP_CONF 
    $HADOOP_YARN_HOME/sbin/yarn-daemon.sh start proxyserver --config $HADOOP_CONF
    $HADOOP_PREFIX/sbin/mr-jobhistory-daemon.sh start historyserver --config $HADOOP_CONF

    echo "waiting for HDFS to come up"
    # loop until at least HDFS is up
    cmd="$HADOOP_PREFIX/bin/hdfs dfs -ls /"
    NEXT_WAIT_TIME=0
    up=0
    while [  $NEXT_WAIT_TIME -ne 4 ] ; do
        $cmd
        rc=$?
        if [[ $rc == 0 ]]; then
            up=1
            break
        fi
       sleep $(( NEXT_WAIT_TIME++ ))
    done

    if [[ $up != 1 ]]; then
        echo "HDFS doesn't seem to be up; exiting"
        exit $rc
    fi

    echo "listing all Java processes"
    jps
}

startDaemons
