#!/bin/bash
if [ ! -f /home/vagrant/.vagrant-installation/docker.installed ]
then
    echo Downloading and Installing Docker
#    mkdir /home/vagrant/.vagrant-installation/docker
    touch /home/vagrant/.vagrant-installation/docker.installed

    # cd /home/vagrant/.vagrant-installation/docker
    # wget -q -c --progress=dot:giga https://download.docker.com/linux/centos/7/x86_64/stable/Packages/docker-ce-17.03.1.ce-1.el7.centos.x86_64.rpm
    # sudo yum -y install /home/vagrant/.vagrant-installation/docker/docker-ce-17.03.1.ce-1.el7.centos.x86_64.rpm
    # cd ~/

    sudo yum install -y yum-utils
    sudo yum-config-manager \
         --add-repo \
         https://download.docker.com/linux/centos/docker-ce.repo
    #sudo yum-config-manager --enable docker-ce-edge
    sudo yum-config-manager --disable docker-ce-edge

    sudo yum -y install docker-ce

    sudo systemctl enable docker
    sudo systemctl start docker
    sudo docker run hello-world

    sudo groupadd docker
    sudo usermod -aG docker $USER
fi
