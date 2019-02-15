#!/bin/bash

source "/vagrant/scripts/common.sh"

function stopHbase {
    echo "stop HBase"
    export HADOOP_HOME=${HADOOP_PREFIX}
    ${HBASE_PREFIX}/bin/stop-hbase.sh
}

stopHbase

