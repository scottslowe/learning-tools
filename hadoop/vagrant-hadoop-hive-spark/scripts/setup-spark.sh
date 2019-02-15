#!/bin/bash

# http://www.cloudera.com/content/cloudera/en/documentation/core/v5-2-x/topics/cdh_ig_spark_configure.html

source "/vagrant/scripts/common.sh"

function installLocalSpark {
	echo "install spark from local file"
	FILE=/vagrant/resources/$SPARK_ARCHIVE
	tar -xzf $FILE -C /usr/local
}

function installRemoteSpark {
	echo "install spark from remote file"
	curl ${CURL_OPTS} -o /vagrant/resources/$SPARK_ARCHIVE -O -L $SPARK_MIRROR_DOWNLOAD
	tar -xzf /vagrant/resources/$SPARK_ARCHIVE -C /usr/local
}

function setupSpark {
	echo "setup spark"
	cp -f /vagrant/resources/spark/slaves ${SPARK_CONF}
	cp -f /vagrant/resources/spark/spark-env.sh ${SPARK_CONF}
	cp -f /vagrant/resources/spark/spark-defaults.conf ${SPARK_CONF}
	ln -s $HADOOP_CONF/yarn-site.xml ${SPARK_CONF}/yarn-site.xml
	ln -s $HADOOP_CONF/core-site.xml ${SPARK_CONF}/core-site.xml
	ln -s $HADOOP_CONF/hdfs-site.xml ${SPARK_CONF}/hdfs-site.xml
	ln -s $HIVE_CONF/hive-site.xml   ${SPARK_CONF}/hive-site.xml
	. /etc/profile.d/hadoop.sh
        hdfs dfs -mkdir -p /user/spark/applicationHistory
        hdfs dfs -chmod -R 777 /user/spark
	jar cv0f /tmp/spark-libs.jar -C $SPARK_HOME/jars/ .
	hdfs dfs -put /tmp/spark-libs.jar /user/spark/.
}

function setupEnvVars {
	echo "creating spark environment variables"
	cp -f $SPARK_RES_DIR/spark.sh /etc/profile.d/spark.sh
	. /etc/profile.d/spark.sh
}


function installSpark {
	if resourceExists $SPARK_ARCHIVE; then
		installLocalSpark
	else
		installRemoteSpark
	fi
	ln -s /usr/local/$SPARK_VERSION-bin-hadoop2.7 ${SPARK_HOME}
	mkdir -p ${SPARK_HOME}/logs/history
}

function startServices {
	echo "starting Spark history service"
	${SPARK_HOME}/sbin/start-history-server.sh
}

echo "setup spark"

installSpark
setupSpark
setupEnvVars
startServices

echo "spark setup complete"
