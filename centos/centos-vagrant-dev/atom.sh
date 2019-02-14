#!/bin/bash
if [ ! -d /home/vagrant/.vagrant-installation/atom ]
then
    echo Installing Atom.
    mkdir /home/vagrant/.vagrant-installation/atom
    cd /home/vagrant/.vagrant-installation/atom
    wget -q -c --progress=dot:giga https://github.com/atom/atom/releases/download/v1.16.0/atom.x86_64.rpm

    sudo yum -y install atom.x86_64.rpm

    cd ~/.vagrant-installation/atom
    apm install language-plantuml
    apm install plantuml-preview
    # apm install markdown-writer
    # apm install markdown-scroll-sync
    apm install language-gfm-enhanced
    apm install markdown-preview-enhanced
    apm install language-restructuredtext
    apm install rst-preview-pandoc
    cd ~/
fi
