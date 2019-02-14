#!/bin/sh
. /tmp/vagrant/scripts/utils/isInstalled.sh

# see https://www.linuxbabe.com/ubuntu/install-google-chrome-ubuntu-16-04-lts
install_google-chrome() {
	package=google-chrome-stable

	if isInstalled $package; then return; fi
	echo "Installing and configuring Google Chrome (package: $package)..."

	# PREPARE
	echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list
	wget https://dl.google.com/linux/linux_signing_key.pub
	apt-key add linux_signing_key.pub

	# INSTALL
	apt-get update && apt-get install -y $package

	rm /etc/apt/sources.list.d/google-chrome.list
}