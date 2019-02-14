#!/bin/sh
. /tmp/vagrant/scripts/utils/isInstalled.sh

install_docker() {
	package=docker-ce

	if isInstalled $package; then return; fi
	echo "Installing and configuring Docker (package: $package)..."

	# CLEANUP
	apt-get remove -y docker docker-engine docker.io

	# CONFIGURE
	apt-get install -y apt-transport-https ca-certificates software-properties-common
	curl -fsSL https://download.docker.com/linux/ubuntu/gpg | apt-key add -
	apt-key fingerprint 0EBFCD88
	add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"

	# INSTALL
	apt-get update && apt-get install -y $package docker-compose

	# CONFIGURE
	usermod -aG docker vagrant
}