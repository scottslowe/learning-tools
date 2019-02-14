#!/bin/bash
echo " "
echo "-----------------------------------------------------------------------------"
echo "Provisioning Passenger"
echo "-----------------------------------------------------------------------------"
echo " "
echo "[1/3] --== Passenger - Ajour du dépot ==--"
apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 561F9B9CAC40B2F7 >> /vagrant/log/provision_passenger.log 2>&1
apt-get install -y apt-transport-https ca-certificates >> /vagrant/log/provision_passenger.log 2>&1
sh -c 'echo deb https://oss-binaries.phusionpassenger.com/apt/passenger xenial main > /etc/apt/sources.list.d/passenger.list' >> /vagrant/log/provision_passenger.log 2>&1
apt-get update >> /vagrant/log/provision_passenger.log 2>&1

echo "[2/3] --== Passenger - Installation ==--"
apt-get install -y passenger >> /vagrant/log/provision_passenger.log 2>&1

echo "[3/3] --== Passenger - Patching nginx.conf ==--"
sed -i 's/\# include \/etc\/nginx\/passenger\.conf;/\include \/etc\/nginx\/passenger\.conf;/g' /etc/nginx/nginx.conf >> /vagrant/log/provision-passenger.log 2>&1
