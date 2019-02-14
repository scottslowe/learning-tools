#!/bin/sh
. /tmp/vagrant/scripts/utils/isInstalled.sh

install_guake() {
	package=guake

	if isInstalled $package; then return; fi
	echo "Installing and configuring Guake Terminal (package: $package)...";
	
	# INSTALL
	apt-get install -y $package
}