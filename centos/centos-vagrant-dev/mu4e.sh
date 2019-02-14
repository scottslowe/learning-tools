#!/bin/bash

if [  ! -f /home/vagrant/.vagrant-installation/mu4e.installed ]
then
    echo Installing mu/mu4e
    touch /home/vagrant/.vagrant-installation/mu4e.installed
    sudo yum install -y gmime-devel xapian-core-devel html2text xdg-utils
    sudo yum install -y webkitgtk3-devel

    cd ~/.vagrant-installation
    wget https://github.com/djcb/mu/releases/download/v1.0/mu-1.0.tar.xz

    xz -d mu-1.0.tar.xz
    tar xvf mu-1.0.tar

    cd ~/.vagrant-installation/mu-1.0
    ./configure && make
    # On the BSDs: use gmake instead of make
    sudo make clean
    sudo make install
    cd ~/

    echo mu/mu4e installation complete.
fi
