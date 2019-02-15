#!/bin/bash
if [ ! -f /home/vagrant/.vagrant-installation/eclipse ]
then
    echo Installing Eclipse.
#    mkdir /home/vagrant/.vagrant-installation/eclipse
    touch /home/vagrant/.vagrant-installation/eclipse

    # https://www.softwarecollections.org/en/scls/rhscl/rh-eclipse46/
    # 1. Install a package with repository for your system:
    # On CentOS, install package centos-release-scl available in CentOS repository:
    sudo yum -y install centos-release-scl

    # On RHEL, enable RHSCL repository for you system:
    sudo yum-config-manager --enable rhel-server-rhscl-7-rpms

    # 2. Install the collection:
    sudo yum -y install rh-eclipse46

    # 3. Start using the software collection:
    scl enable rh-eclipse46 bash
fi
