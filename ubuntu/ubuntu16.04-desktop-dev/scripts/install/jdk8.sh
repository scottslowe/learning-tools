#!/bin/sh
. /tmp/vagrant/scripts/utils/isInstalled.sh

install_jdk8() {
	package=openjdk-8-jdk

	if isInstalled $package; then return; fi
	echo "Installing and configuring Open JDK 8 (package: $package)...";
	
	# INSTALL
	apt-get install -y $package
}