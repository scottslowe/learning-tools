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
	curl -sS -o /vagrant/resources/$SPARK_ARCHIVE -O -L $SPARK_MIRROR_DOWNLOAD
	tar -xzf /vagrant/resources/$SPARK_ARCHIVE -C /usr/local
}

function setupSpark {
	echo "setup spark"
	cp -f /vagrant/resources/spark/slaves /usr/local/spark/conf
	cp -f /vagrant/resources/spark/spark-env.sh /usr/local/spark/conf
	cp -f /vagrant/resources/spark/spark-defaults.conf /usr/local/spark/conf
}

function setupEnvVars {
	echo "creating spark environment variables"
	cp -f $SPARK_RES_DIR/spark.sh /etc/profile.d/spark.sh
	. /etc/profile.d/spark.sh
}

function setupHistoryServer {
	echo "setup history server"
	. /etc/profile.d/hadoop.sh
    hdfs dfs -mkdir -p /user/spark/applicationHistory
    hdfs dfs -chmod -R 777 /user/spark
}

function installSpark {
	if resourceExists $SPARK_ARCHIVE; then
		installLocalSpark
	else
		installRemoteSpark
	fi
	ln -s /usr/local/$SPARK_VERSION-bin-hadoop2.6 /usr/local/spark
	mkdir -p /usr/local/spark/logs/history
}

function startServices {
	echo "starting Spark history service"
	/usr/local/spark/sbin/start-history-server.sh
}

echo "setup spark"

installSpark
setupSpark
setupEnvVars
setupHistoryServer
startServices

echo "spark setup complete"
