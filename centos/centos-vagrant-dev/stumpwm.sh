#!/bin/bash
if [ ! -f ~/.vagrant-installation/sbcl.installed ]
then
    echo Installing SBCL
    touch ~/.vagrant-installation/sbcl.installed
    sudo yum -y install sbcl
fi

if [ ! -d ~/.vagrant-installation/stumpwm ]
then
    echo Installing StumpWM.
    cd ~/.vagrant-installation/

    git clone https://github.com/stumpwm/stumpwm.git
    git clone https://github.com/stumpwm/stumpwm-contrib.git
    # wget -q -c --progress=dot:giga https://github.com/stumpwm/stumpwm/archive/v1.0.0.tar.gz
    # tar xvzf v1.0.0.tar.gz
    # cd stumpwm-1.0.0
    cd stumpwm
    ln -s ~/.vagrant-installation/stumpwm-contrib ~/.vagrant-installation/stumpwm/contrib 

    curl -O https://beta.quicklisp.org/quicklisp.lisp

    # sbcl --load quicklisp.lisp
    # (quicklisp-quickstart:install)
    # (ql:add-to-init-file)
    # (ql:quickload "clx")
    # (ql:quickload "cl-ppcre")
    # (ql:quickload "alexandria")
    # (quit)

    # autoconf
    # ./configure
    # make

    tee -a ~/.xinitrc <<EOF
~/.vagrant-installation/stumpwm/stumpwm
EOF

    cd ~/
fi
