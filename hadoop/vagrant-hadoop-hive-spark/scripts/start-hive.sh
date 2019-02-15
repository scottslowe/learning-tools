#!/bin/bash

source "/vagrant/scripts/common.sh"

function startHive {
    echo "starting Hive daemons"
    export HADOOP_HOME=${HADOOP_PREFIX}
    HIVE_LOGPATH=/usr/local/hive/logs
    if [ -z $(pgrep -f HiveMetaStore) ]; then
        nohup ${HIVE_PREFIX}/bin/hive --service metastore < /dev/null > ${HIVE_LOGPATH}/hive_metastore_`date +"%Y%m%d%H%M%S"`.log 2>&1 </dev/null &
    fi
    if [ -z $(pgrep -f HiveServer2) ]; then
        nohup ${HIVE_PREFIX}/bin/hive --service hiveserver2 < /dev/null > ${HIVE_LOGPATH}/hive_server2_`date +"%Y%m%d%H%M%S"`.log 2>&1 </dev/null &
    fi
    echo "listing all Java processes"
    jps
}

startHive
