#!/bin/sh
. /tmp/vagrant/scripts/utils/isInstalled.sh

install_sublime-text-3() {
	package=sublime-text

	if isInstalled $package; then return; fi
	echo "Installing and configuring Sublime Text 3 (package: $package)..."

	# PREPARE
	wget -qO - https://download.sublimetext.com/sublimehq-pub.gpg | apt-key add -
	echo "deb https://download.sublimetext.com/ apt/stable/" | tee /etc/apt/sources.list.d/sublime-text.list

	# INSTALL
	apt-get update && apt-get install -y $package
}