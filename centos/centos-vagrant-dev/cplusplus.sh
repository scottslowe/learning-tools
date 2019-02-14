#!/bin/bash
if [  ! -f /home/vagrant/.vagrant-installation/cplusplus.installed ]
then
	echo Installing C/C++ dependencies
    yum -y install clang
    yum -y install cmake
    yum -y install valgrind
    touch /home/vagrant/.vagrant-installation/cplusplus.installed
fi
