#!/bin/bash

source "/vagrant/scripts/common.sh"
echo "$FLUME_MIRROR_DOWNLOAD"

function installLocalFlume {
	echo "install FLume from local file"
	FILE=/vagrant/resources/$FLUME_ARCHIVE
	tar -xzf $FILE -C /usr/local
}

function installRemoteFlume {
	echo "install Flume from remote file"
	echo $FLUME_MIRROR_DOWNLOAD
	curl ${CURL_OPTS} -o /vagrant/resources/$FLUME_ARCHIVE -O -L $FLUME_MIRROR_DOWNLOAD
	tar -xzf /vagrant/resources/$FLUME_ARCHIVE -C /usr/local
}

function setupFlume {
	echo "creating Flume environment variables"
	cp -f $FLUME_RES_DIR/flume-env.sh /etc/profile.d/flume-env.sh
	. /etc/profile.d/flume-env.sh
}

function installFlume {
	if resourceExists $FLUME_ARCHIVE; then
		installLocalFlume
	else
		installRemoteFlume
	fi
	ln -s /usr/local/$FLUME_RELEASE /usr/local/flume
}


echo "setup Flume"

installFlume
setupFlume

echo "Flume setup complete"
