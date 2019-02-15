#!/bin/bash

source "/vagrant/scripts/common.sh"

function installLocalTez {
	echo "install Tez from local file"
	FILE=/vagrant/resources/$TEZ_ARCHIVE
	tar -xzf $FILE -C /usr/local
}

function installRemoteTez {
	echo "install Tez from remote file"
	curl ${CURL_OPTS} -o /vagrant/resources/$TEZ_ARCHIVE -O -L $TEZ_MIRROR_DOWNLOAD
	tar -xzf /vagrant/resources/$TEZ_ARCHIVE -C /usr/local
}

function setupTez {
	echo "copy Tez to HDFS"
	hdfs dfs -put -f /usr/local/tez /user/.
	echo "Copy Tez configuration"
	cp -f $TEZ_RES_DIR/mapred-site.xml /usr/local/hadoop/etc/hadoop/.
	cp -f $TEZ_RES_DIR/tez-site.xml /usr/local/tez/conf/.
	hdfs dfs -put  -f ${HIVE_EXEC_JAR}  /user/tez/.
}

function setupEnvVars {
	echo "creating Tez environment variables"
	cp -f $TEZ_RES_DIR/tez.sh /etc/profile.d/tez.sh
	. /etc/profile.d/tez.sh
}

function installTez {
	if resourceExists $TEZ_ARCHIVE; then
		installLocalTez
	else
		installRemoteTez
	fi
	ln -s /usr/local/$TEZ_RELEASE /usr/local/tez
}


echo "setup Tez"

installTez
setupTez
setupEnvVars

echo "Tez setup complete"
