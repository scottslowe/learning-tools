#!/bin/bash
if [  ! -f /home/vagrant/.vagrant-installation/sphinx.installed ]
then
    echo Installing Sphinx-doc
#    mkdir /home/vagrant/.vagrant-installation/sphinx
    touch /home/vagrant/.vagrant-installation/sphinx.installed

    sudo yum -y install python-pip
    pip install --upgrade pip
    pip install -U Sphinx
    pip install -U setuptools
    pip install sphinxcontrib-plantuml

    cd ~/
fi
