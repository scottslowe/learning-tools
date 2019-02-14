#!/bin/sh
. /tmp/vagrant/scripts/utils/isInstalled.sh

install_pip() {
	package=python-pip

	if isInstalled $package; then return; fi
	echo "Installing and configuring Pip (package: $package)..."

	# INSTALL
	apt-get install -y $package python-dev build-essential
}