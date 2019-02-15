#!/bin/sh
export PIG_HOME=/usr/local/pig
export PATH=${PATH}:${PIG_HOME}/bin
## Setup hcatalog support for Pig...
export PIG_CLASSPATH=$HCAT_HOME/share/hcatalog/hive-hcatalog-core-*.jar:$HCAT_HOME/share/hcatalog/hive-hcatalog-pig-apapter-*.jar:$HIVE_HOME/lib/hive-metastore-*.jar:$HIVE_HOME/lib/libthrift-*.jar:$HIVE_HOME/lib/hive-exec-*.jar:$HIVE_HOME/lib/libfb303-*.jar:$HIVE_HOME/lib/jdo2-api-*-ec.jar:$HIVE_HOME/conf:$HADOOP_HOME/conf:$HIVE_HOME/lib/slf4j-api-*.jar
