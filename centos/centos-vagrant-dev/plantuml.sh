#!/bin/bash
if [ ! -d ~/.vagrant-installation/plantuml/ ]
then
    mkdir ~/.vagrant-installation/plantuml
fi

if [ ! -f  ~/.vagrant-installation/plantuml/plantuml.jar ]
then
    echo Installing PlantUML.
    cd ~/.vagrant-installation/plantuml
    wget c --progress=dot:giga http://sourceforge.net/projects/plantuml/files/plantuml.jar
    chmod 755 ~/.vagrant-installation/plantuml/plantuml.jar
    ln -s ~/.vagrant-installation/plantuml/plantuml.jar ~/.vagrant-installation/plantuml.jar
    cd ~/
fi
