#!/bin/bash
echo " "
echo "-----------------------------------------------------------------------------"
echo "Provisioning Nginx & PHP"
echo "-----------------------------------------------------------------------------"
echo " "
echo "[1/4] --== Installing nginx ==--"
apt-get install -y nginx >> /vagrant/log/provition-nginx-php71.log 2>&1
apt-get install -y nginx-full  >> /vagrant/log/provition-nginx-php71.log 2>&1 
apt-get install -y nginx-extras  >> /vagrant/log/provition-nginx-php71.log 2>&1

echo "[2/4] --== Updating PHP repository ==--"
add-apt-repository ppa:ondrej/php -y >> /vagrant/log/provition-nginx-php71.log 2>&1
apt-get update -y >> /vagrant/log/provition-nginx-php71.log 2>&1

echo "[3/4] --== Installing PHP & Extensions ==--"
apt-get install -y php7.1-fpm >> /vagrant/log/provition-nginx-php71.log 2>&1
apt-get install -y php-curl >> /vagrant/log/provition-nginx-php71.log 2>&1
apt-get install -y php-gd >> /vagrant/log/provition-nginx-php71.log 2>&1
apt-get install -y php-gettext >> /vagrant/log/provition-nginx-php71.log 2>&1
apt-get install -y php-xdebug >> /vagrant/log/provition-nginx-php71.log 2>&1
#apt-get install -y php-bcmath >> /vagrant/log/provition-nginx-php71.log 2>&1

echo "[4/4] --== Setting vhosts ==--"
cp -f /vagrant/provision/nginx/*.conf /etc/nginx/sites-available/
rm -f /etc/nginx/sites-enabled/*.conf
for filepath in /etc/nginx/sites-available/*.conf
do
  filename=`basename $filepath`
  echo "  +--> Enabling VHOST $filename"
  ln -s /etc/nginx/sites-available/$filename /etc/nginx/sites-enabled/$filename 
done