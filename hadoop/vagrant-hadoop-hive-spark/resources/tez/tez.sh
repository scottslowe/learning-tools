#!/bin/sh
export TEZ_HOME=/usr/local/tez
export TEZ_CONF_DIR=${TEZ_HOME}/conf
export TEZ_JARS=${TEZ_HOME}

# For enabling Tez on Hive
if [ -z "$HIVE_AUX_JARS_PATH" ]; then
	export HIVE_AUX_JARS_PATH="${TEZ_JARS}"
else
	export HIVE_AUX_JARS_PATH="${HIVE_AUX_JARS_PATH}:${TEZ_JARS}"
fi
if [ -z "${HADOOP_CLASSPATH}" ]; then
	export HADOOP_CLASSPATH=${TEZ_CONF_DIR}:${TEZ_JARS}/*:${TEZ_JARS}/lib/*
else
	export HADOOP_CLASSPATH=${TEZ_CONF_DIR}:${TEZ_JARS}/*:${TEZ_JARS}/lib/*:${HADOOP_CLASSPATH}
fi
