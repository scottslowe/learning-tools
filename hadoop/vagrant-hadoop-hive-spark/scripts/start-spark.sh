#!/bin/bash


source "/vagrant/scripts/common.sh"

function setupEnvVars {
	. /etc/profile.d/spark.sh
}

function startServices {
	echo "starting Spark history service"
	/usr/local/spark/sbin/stop-history-server.sh
	/usr/local/spark/sbin/start-history-server.sh
}

echo "setup spark"

setupEnvVars
startServices
jps
echo "spark start complete"
