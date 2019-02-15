#!/bin/bash
if [ ! -d ~/.vagrant-installation/spacemacs/ ]
then
    echo Installing Spacemacs.
    cd ~/.vagrant-installation/
    git clone https://github.com/syl20bnr/spacemacs
fi

if [ ! -d ~/.emacs.d/ ]
then
    echo Linking .spacemacs to .emacs.d
    ln -s ~/.vagrant-installation/spacemacs/ ~/.emacs.d
fi
