#!/bin/sh
before() 
{	
    #systemctl disable apt-daily.service
    #systemctl disable apt-daily.timer
    #systemctl disable unattended-upgrades.service

	vagrant_home=/tmp/vagrant
	chmod +x $vagrant_home/scripts/utils/*.sh
	chmod +x $vagrant_home/scripts/install/*.sh

	apt-get install htop

	apt-get autoremove -y

	apt-get update
}