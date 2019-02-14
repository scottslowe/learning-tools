#!/bin/bash
echo "  _         _          ___ _ _ _             _  " 
echo " | |   __ _| |__  ___ / __(_) | |__  ___ _ _| |_ "
echo " | |__/ _\` | '_ \\/ _ \\ (_ | | | '_ \\/ -_) '_|  _|"
echo " |____\\__,_|_.__/\\___/\\___|_|_|_.__/\\___|_|  \\__|"
echo " Vagrant Box - Ubuntu 16.04 LTS"  
echo " "                                             
echo "-----------------------------------------------------------------------------"
echo "Provisioning System tools..."
echo "-----------------------------------------------------------------------------"
echo " "
echo "[1/5] --== Updating Box Image ==--"
apt-get update -y >> /vagrant/log/provision_system.log 2>&1

echo "[2/5] --== Upgrading Box Image ==--"
apt-get upgrade -y >> /vagrant/log/provision_system.log 2>&1
apt-get dist-upgrade -y >> /vagrant/log/provision_system.log 2>&1

echo "[3/5] --== Installing Language Pack ==--"
apt-get install language-pack-fr -y >> /vagrant/log/provision_system.log 2>&1

echo "[4/5] --== Essential System Tools ==--"
apt-get install -y zsh curl git-core git git-flow python-software-properties build-essential >> /vagrant/log/provision_system.log 2>&1
apt-get install -y sqlite3 libsqlite3-dev libssl-dev libreadline-dev zlib1g-dev # For RBENV / ruby build

echo "[5/5] --== Installing NodeJS/NPM ==--"
curl -sL https://deb.nodesource.com/setup_8.x | sudo -E bash -
apt-get install -y nodejs >> /vagrant/log/provision_system.log 2>&1