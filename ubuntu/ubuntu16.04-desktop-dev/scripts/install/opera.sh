#!/bin/sh
. /tmp/vagrant/scripts/utils/isInstalled.sh

install_opera() {
	package=opera

	if isInstalled $package; then return; fi
	echo "Installing and configuring Opera (package: $package)..."

	curl -fsSL http://deb.opera.com/archive.key | apt-key add -
	echo "deb http://deb.opera.com/opera-stable/ stable non-free" >> /etc/apt/sources.list.d/opera.list

	# INSTALL
	apt-get update && apt-get install -y $package
}