#!/bin/bash
if [  ! -d /home/vagrant/.vagrant-installation/lein ]
then
    echo Installing Leiningen - https://leiningen.org.
    mkdir /home/vagrant/.vagrant-installation/lein
    cd /home/vagrant/.vagrant-installation/lein
    wget -q -c --progress=dot:giga https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein
    ln -s ~/.vagrant-installation/lein/lein ~/bin/lein
    chmod a+x ~/bin/lein
    lein
    cd ~/
fi
