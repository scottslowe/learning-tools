#!/bin/bash
# If you restart your VM then the Hadoop/Spark/Hive services will be started by this script.
# Due to the config "node.vm.provision :shell, path: "scripts/bootstrap.sh", run: 'always'" on Vagrantfile

#systemctl start mysql.service
/vagrant/scripts/start-hadoop.sh	# Starts the namenode/datanode plus yarn.
/vagrant/scripts/start-hive.sh		# Start hiveserver2 plus metastore service.
/vagrant/scripts/start-hbase.sh		# Start HBase
/vagrant/scripts/start-spark.sh		# Start Spark history server.
