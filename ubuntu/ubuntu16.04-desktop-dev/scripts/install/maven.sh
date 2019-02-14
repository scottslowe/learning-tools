#!/bin/sh
. /tmp/vagrant/scripts/utils/isInstalled.sh

install_maven() {
	package=maven

    if isInstalled $package; then return; fi
    echo "Installing and configuring Maven (package: $package)..."

    # INSTALL
    apt-get install -y $package
}