#!/bin/bash

source "/vagrant/scripts/common.sh"

function installLocalHbase {
	echo "install Hbase from local file"
	FILE=/vagrant/resources/$HBASE_ARCHIVE
	tar -xzf $FILE -C /usr/local
}

function installRemoteHbase {
	echo "install hbase from remote file"
	curl ${CURL_OPTS} -o /vagrant/resources/$HBASE_ARCHIVE -O -L $HBASE_MIRROR_DOWNLOAD
	tar -xzf /vagrant/resources/$HBASE_ARCHIVE -C /usr/local
}

function installHbase {
	if resourceExists $HBASE_ARCHIVE; then
		installLocalHbase
	else
		installRemoteHbase
	fi
	ln -s /usr/local/hbase-$HBASE_VERSION /usr/local/hbase
}

function setupHbase {
	echo "copying over hbase configuration "
	cp -f $HBASE_RES_DIR/* $HBASE_CONF
}

function setupEnvVars {
	echo "creating hbase environment variables"
	cp -f $HBASE_RES_DIR/hbase.sh /etc/profile.d/hbase.sh
	. /etc/profile.d/hbase.sh
}

echo "setup hbase"

installHbase
setupHbase
setupEnvVars

echo "hbase setup complete"
