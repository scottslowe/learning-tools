#!/bin/bash
if [ ! -d ~/.vagrant-installation/dot-emacs/ ]
then
  cd ~/.vagrant-installation/
  git clone https://github.com/Balooga/dot-emacs.git
fi

if [ ! -d ~/.spacemacs.d/ ]
then
    echo Linking .spacemacs.d
    ln -s ~/.vagrant-installation/dot-emacs/.spacemacs.d/ ~/.spacemacs.d
fi
