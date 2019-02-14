#!/bin/sh
. /tmp/vagrant/scripts/utils/isInstalled.sh

install_angular-cli() {
	package=@angular/cli

	if [ -f "/usr/bin/$package" ]; then return; fi
	echo "Installing and configuring Angular CLI..."

	# INSTALL
	npm install -g $package
}