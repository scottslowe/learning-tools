#!/bin/bash

# To remove a previous installation
# rm -rf /usr/local/texlive/2018
# New installations of texlive require a new texlive.profile
# which is created following a successful install of texlive.
# texlive.profile can be found in /usr/local/texlive/2018/tlpkg/texlive.profile
# and remember to change the path in ~/.bash_profile

if [ ! -d /usr/local/texlive/2018 ]
then
    echo Downloading TexLive.
    wget -q -c --progress=dot:giga http://muug.ca/mirror/ctan/systems/texlive/Images/texlive.iso

    echo Installing TexLive.
    mount -t iso9660 -o ro,loop,noauto texlive.iso /mnt

    cd /mnt
    ./install-tl -profile /home/vagrant/texlive.profile

    cd /home/vagrant/
    umount /mnt
    rm /home/vagrant/texlive.iso
    rm /home/vagrant/texlive.profile

    /usr/local/texlive/2018/bin/x86_64-linux/tlmgr path add

    echo Updating TexLive.
    /usr/local/texlive/2018/bin/x86_64-linux/tlmgr -repository http://mirror.ctan.org/systems/texlive/tlnet update --self
    /usr/local/texlive/2018/bin/x86_64-linux/tlmgr -repository http://mirror.ctan.org/systems/texlive/tlnet update --all

    echo Install Sphinx-Doc style dependencies.
    /usr/local/texlive/2018/bin/x86_64-linux/tlmgr -repository http://mirror.ctan.org/systems/texlive/tlnet install graphics-def
    /usr/local/texlive/2018/bin/x86_64-linux/tlmgr -repository http://mirror.ctan.org/systems/texlive/tlnet install tabulary
    /usr/local/texlive/2018/bin/x86_64-linux/tlmgr -repository http://mirror.ctan.org/systems/texlive/tlnet install fncychap
    /usr/local/texlive/2018/bin/x86_64-linux/tlmgr -repository http://mirror.ctan.org/systems/texlive/tlnet install framed
    /usr/local/texlive/2018/bin/x86_64-linux/tlmgr -repository http://mirror.ctan.org/systems/texlive/tlnet install threeparttable
    /usr/local/texlive/2018/bin/x86_64-linux/tlmgr -repository http://mirror.ctan.org/systems/texlive/tlnet install wrapfig
    /usr/local/texlive/2018/bin/x86_64-linux/tlmgr -repository http://mirror.ctan.org/systems/texlive/tlnet install capt-of
    /usr/local/texlive/2018/bin/x86_64-linux/tlmgr -repository http://mirror.ctan.org/systems/texlive/tlnet install multirow
    /usr/local/texlive/2018/bin/x86_64-linux/tlmgr -repository http://mirror.ctan.org/systems/texlive/tlnet install eqparbox
    /usr/local/texlive/2018/bin/x86_64-linux/tlmgr -repository http://mirror.ctan.org/systems/texlive/tlnet install environ
    /usr/local/texlive/2018/bin/x86_64-linux/tlmgr -repository http://mirror.ctan.org/systems/texlive/tlnet install trimspaces

    echo And latex fonts....
    /usr/local/texlive/2018/bin/x86_64-linux/tlmgr -repository http://mirror.ctan.org/systems/texlive/tlnet install collection-fontsrecommended
    /usr/local/texlive/2018/bin/x86_64-linux/tlmgr -repository http://mirror.ctan.org/systems/texlive/tlnet install collection-fontsextra

    /usr/local/texlive/2018/bin/x86_64-linux/tlmgr -repository http://mirror.ctan.org/systems/texlive/tlnet install epstopdf

    echo 'PATH=$PATH:/usr/local/texlive/2018/bin/x86_64-linux' >> /home/vagrant/.bash_profile
    #echo export PATH >> /home/vagrant/.bash_profile

    echo TexLive Installation Complete.
fi

if [ -d /usr/local/texlive/2018 ]
then
    echo Updating TexLive.
    /usr/local/texlive/2018/bin/x86_64-linux/tlmgr -repository http://mirror.ctan.org/systems/texlive/tlnet update --self
    /usr/local/texlive/2018/bin/x86_64-linux/tlmgr -repository http://mirror.ctan.org/systems/texlive/tlnet update --all
fi
