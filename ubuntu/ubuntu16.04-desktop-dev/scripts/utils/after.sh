#!/bin/sh

after() {	
	echo "Nothing to do after"
	#echo "Executing apt-get upgrade -y >>>>>>>>>>>>>>>>>>>>>>>"
    #DEBIAN_FRONTEND=noninteractive
    #apt-get -y -o Dpkg::Options::="--force-confdef" -o Dpkg::Options::="--force-confold" upgrade

    #systemctl enable apt-daily.service
    #systemctl enable apt-daily.timer
    #systemctl enable unattended-upgrades.service
}