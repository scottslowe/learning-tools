#!/bin/bash
if [  ! -f /home/vagrant/.vagrant-installation/pandoc.installed ]
then
    echo Installing Pandoc
#    mkdir /home/vagrant/.vagrant-installation/pandoc
    touch /home/vagrant/.vagrant-installation/pandoc.installed

    yum -y install pandoc

    cd ~/
fi
