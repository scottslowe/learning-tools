#!/bin/bash

if [  ! -f /home/vagrant/.vagrant-installation/gnome.installed ]
then
    echo Installing GNOME Desktop
    #    mkdir /home/vagrant/.vagrant-installation/gnome
    touch /home/vagrant/.vagrant-installation/gnome.installed
    yum -y groupinstall "GNOME Desktop"

    yum -y remove tracker
fi
