#!/bin/bash


source "/vagrant/scripts/common.sh"

function setupEnvVars {
	. /etc/profile.d/spark.sh
}

function stopServices {
	echo "stop Spark history service"
	/usr/local/spark/sbin/stop-history-server.sh
}

echo "stopspark"

setupEnvVars
stopServices
jps
echo "spark stop complete"
