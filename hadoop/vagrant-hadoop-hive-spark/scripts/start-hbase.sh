#!/bin/bash

source "/vagrant/scripts/common.sh"

function startHbase {
    echo "starting HBase"
    export HADOOP_HOME=${HADOOP_PREFIX}
    ${HBASE_PREFIX}/bin/start-hbase.sh
    echo "listing all Java processes"
    jps
}

startHbase
