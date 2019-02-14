#!/bin/sh
. /tmp/vagrant/scripts/utils/isInstalled.sh

install_git() {
	package=git-all

	if isInstalled $package; then return; fi
	echo "Installing and configuring Git (package: $package)..."

	# CLEANUP
	apt-get purge -y git

	# INSTALL
	apt-get install -y git-daemon-sysvinit git-all && \

	# CONFIGURE - this should be done by the box user on the first run
	# git config --global user.name "FirstName LastName"
	# git config --global user.email "email@example.com"

	su - vagrant -c "git config --global alias.co checkout"
	su - vagrant -c "git config --global alias.br branch"
	su - vagrant -c "git config --global alias.ci commit"
	su - vagrant -c "git config --global alias.st status"
	su - vagrant -c "git config --global alias.unstage 'reset HEAD --'"
	su - vagrant -c "git config --global alias.last 'log -1 HEAD'"
	su - vagrant -c "git config --global push.default simple"
}