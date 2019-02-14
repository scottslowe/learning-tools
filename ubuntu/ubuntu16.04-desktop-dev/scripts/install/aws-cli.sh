#!/bin/sh
. /tmp/vagrant/scripts/utils/isInstalled.sh

install_aws-cli() {
	package=aws

	if [ -f "/usr/bin/$package" ]; then return; fi
	echo "Installing and configuring AWS CLI..."

	# INSTALL
	pip install awscli --upgrade
}