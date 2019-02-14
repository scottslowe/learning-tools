#!/bin/bash
if [ ! -d ~/.vagrant-installation/reveal.js ]
then

    echo Installing Reveal.
    cd ~/.vagrant-installation/
    git clone https://github.com/hakimel/reveal.js.git
    ln -s ~/.vagrant-installation/reveal.js ~/.reveal.js

    cd ~/
fi
