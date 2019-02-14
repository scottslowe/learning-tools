#!/bin/sh
. /tmp/vagrant/scripts/utils/isInstalled.sh

install_node-and-npm() {
	package=nodejs

	if isInstalled $package; then return; fi
	echo "Installing and configuring Node JS (package: $package)..."

	# PREPARE
	curl -sL https://deb.nodesource.com/setup_6.x | sudo -E bash -
	apt-get install -y nodejs

	# INSTALL
	# apt-get update && apt-get install -y $package build-essential
}