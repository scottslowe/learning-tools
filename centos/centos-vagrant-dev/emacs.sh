#!/bin/bash
if ! hash emacs 2>/dev/null;
then
    echo Emacs not found in the current $PATH
    echo Installing Emacs

    if [ ! -d ~/.vagrant-installation/emacs ]
    then
        cd ~/.vagrant-installation
        git clone https://github.com/emacs-mirror/emacs
    fi

    cd ~/.vagrant-installation/emacs
    git checkout emacs-26
    git pull
    sudo make install
    make clean
    cd ~/
    echo Emacs installation complete.
fi
