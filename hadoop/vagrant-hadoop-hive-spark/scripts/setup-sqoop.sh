#!/bin/bash

source "/vagrant/scripts/common.sh"

function installLocalSqoop {
	echo "install sqoop from local file"
	FILE=/vagrant/resources/$SQOOP_ARCHIVE
	tar -xzf $FILE -C /usr/local
}

function installRemoteSqoop {
	echo "install sqoop from remote file"
	curl ${CURL_OPTS} -o /vagrant/resources/$SQOOP_ARCHIVE -O -L $SQOOP_MIRROR_DOWNLOAD
	tar -xzf /vagrant/resources/$SQOOP_ARCHIVE -C /usr/local
}

function setupEnvVars {
	echo "creating sqoop environment variables"
	cp -f $SQOOP_RES_DIR/sqoop.sh /etc/profile.d/sqoop.sh
	. /etc/profile.d/sqoop.sh
}

function installSqoop {
	if resourceExists $SQOOP_ARCHIVE; then
		installLocalSqoop
	else
		installRemoteSqoop
	fi
	ln -s /usr/local/$SQOOP_RELEASE /usr/local/sqoop
}

echo "setup sqoop"

installSqoop
setupEnvVars

echo "sqoop setup complete"
