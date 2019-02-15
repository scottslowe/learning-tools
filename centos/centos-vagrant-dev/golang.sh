#!/bin/bash
if [  ! -f /home/vagrant/.vagrant-installation/golang.installed ]
then
    echo Installing Golang
    touch /home/vagrant/.vagrant-installation/golang.installed

    cd ~/.vagrant-installation
    wget https://dl.google.com/go/go1.11.linux-amd64.tar.gz
    sudo tar -C /usr/local -xzf go1.11.linux-amd64.tar.gz

    echo 'PATH=$PATH:/usr/local/go/bin:~/go/bin' >> /home/vagrant/.bash_profile
    echo 'GOPATH=~/Documents/go' >> /home/vagrant/.bash_profile
    echo 'PATH=$PATH:$GOPATH' >> /home/vagrant/.bash_profile

    echo 'export GOPATH' >> /home/vagrant/.bash_profile
    echo 'export PATH' >> /home/vagrant/.bash_profile

    # echo 'export PATH=$PATH:/usr/local/go/bin' >> /home/vagrant/.bash_profile
    # echo 'export GOPATH=~/go/bin' >> /home/vagrant/.bash_profile
    # echo 'export PATH=$PATH:$GOPATH' >> /home/vagrant/.bash_profile

    # echo 'export PATH=$PATH:/usr/local/go/bin'
    # echo 'export GOPATH=~/go/bin'
    # echo 'export PATH=$PATH:$GOPATH'

    /usr/local/go/bin/go get -u -v github.com/nsf/gocode
    /usr/local/go/bin/go get -u -v github.com/rogpeppe/godef
    /usr/local/go/bin/go get -u -v golang.org/x/tools/cmd/guru
    /usr/local/go/bin/go get -u -v golang.org/x/tools/cmd/gorename
    /usr/local/go/bin/go get -u -v golang.org/x/tools/cmd/goimports
    /usr/local/go/bin/go get -u -v github.com/alecthomas/gometalinter
    ~/go/bin/gometalinter --install --update
    cd ~/
fi
