#!/usr/bin/env bash

if [ `hostname -s` != "nextcloud-app" ]; then
	echo "Please run this script in the VM"
	exit 0
fi


cat <<EOF > /etc/apt/sources.list
deb http://mirrors.aliyun.com/ubuntu/ xenial main
deb-src http://mirrors.aliyun.com/ubuntu/ xenial main

deb http://mirrors.aliyun.com/ubuntu/ xenial-updates main
deb-src http://mirrors.aliyun.com/ubuntu/ xenial-updates main

deb http://mirrors.aliyun.com/ubuntu/ xenial universe
deb-src http://mirrors.aliyun.com/ubuntu/ xenial universe
deb http://mirrors.aliyun.com/ubuntu/ xenial-updates universe
deb-src http://mirrors.aliyun.com/ubuntu/ xenial-updates universe

deb http://mirrors.aliyun.com/ubuntu/ xenial-security main
deb-src http://mirrors.aliyun.com/ubuntu/ xenial-security main
deb http://mirrors.aliyun.com/ubuntu/ xenial-security universe
deb-src http://mirrors.aliyun.com/ubuntu/ xenial-security universe
EOF

echo '==================================================================================================='
echo "Setting up Linux Server"
echo '==================================================================================================='
sudo apt-get update -y
sudo apt-get upgrade -y

sudo apt-get install -y git
sudo apt-get install -y mcrypt
sudo apt-get install -y wget
sudo apt-get install -y curl
sudo apt-get install -y memcached
sudo apt-get install -y zip unzip
sudo apt-get install -y python-software-properties


echo '==================================================================================================='
echo "Installing PHP"
echo '==================================================================================================='
sudo add-apt-repository ppa:ondrej/php
sudo apt-get update

# Install PHP7.0
sudo apt-get -y install php7.0 php7.0-cgi php7.0-cli php7.0-common php7.0-curl \
    php7.0-dev php7.0-gd php7.0-gmp php7.0-json php7.0-ldap php7.0-mysql php7.0-odbc php7.0-opcache \
    php7.0-pgsql php7.0-pspell php7.0-readline php7.0-recode php7.0-sqlite3 php7.0-tidy php7.0-xml \
    php7.0-xmlrpc libphp7.0-embed php7.0-bcmath php7.0-bz2 php7.0-enchant php7.0-fpm php7.0-imap \
    php7.0-interbase php7.0-intl php7.0-mbstring php7.0-mcrypt php7.0-phpdbg php7.0-soap php7.0-sybase \
    php7.0-xsl php7.0-zip php7.0-dba php-redis php7.0-memcached
sudo apt-get install php-apcu php-apcu-bc memcached


sudo sed -i 's/;cgi.fix_pathinfo=1/cgi.fix_pathinfo=1/' /etc/php/7.0/fpm/php.ini
sudo sed -i 's/user = www-data/user = vagrant/' /etc/php/7.0/fpm/pool.d/www.conf
sudo sed -i 's/group = www-data/group = vagrant/' /etc/php/7.0/fpm/pool.d/www.conf
sudo sed -i 's/listen.owner = www-data/listen.owner = vagrant/' /etc/php/7.0/fpm/pool.d/www.conf
sudo sed -i 's/listen.group = www-data/listen.group = vagrant/' /etc/php/7.0/fpm/pool.d/www.conf



sed -i 's/^\(;\)\(date\.timezone\s*=\).*$/\2 \"Asia\/Shanghai\"/' /etc/php/7.0/fpm/php.ini
sed -i 's/^\(display_errors\s*=\).*$/\1 On/' /etc/php/7.0/fpm/php.ini

## Enable Opcache
sed -i 's/^\(;\)\(opcache\.validate_timestamps\s*=\).*$/\20/' /etc/php/7.0/fpm/php.ini
sed -i 's/^\(;\)\(opcache\.enable\s*=\).*$/\21/' /etc/php/7.0/fpm/php.ini
sed -i 's/^\(;\)\(opcache\.enable_cli\s*=\).*$/\21/' /etc/php/7.0/fpm/php.ini
sed -i 's/^\(;\)\(opcache\.interned_strings_buffer\s*=\).*$/\28/' /etc/php/7.0/fpm/php.ini
sed -i 's/^\(;\)\(opcache\.memory_consumption\s*=\).*$/\2128/' /etc/php/7.0/fpm/php.ini
sed -i 's/^\(;\)\(opcache\.max_accelerated_files\s*=\).*$/\210000/' /etc/php/7.0/fpm/php.ini
sed -i 's/^\(;\)\(opcache\.save_comments\s*=\).*$/\21/' /etc/php/7.0/fpm/php.ini
sed -i 's/^\(;\)\(opcache\.revalidate_freq\s*=\).*$/\21/' /etc/php/7.0/fpm/php.ini
sudo systemctl start memcached

sudo systemctl enable memcached

sudo systemctl start php7.0-fpm

sudo systemctl enable php7.0-fpm


echo '==================================================================================================='
echo "Installing XDEBUG"
echo '==================================================================================================='

sudo apt-get install -y php-xdebug

envsubst \$ERROR_PATH < "/vagrant/server/conf/30-xdebug.ini.template" > "/etc/php/7.0/fpm/conf.d/30-xdebug.ini"

sudo service php7.0-fpm restart

echo '==================================================================================================='
echo "Installing nginx"
echo '==================================================================================================='
sudo apt-get install -y nginx
sudo rm -rf /etc/nginx/sites-available/*
sudo rm -rf /etc/nginx/sites-enabled/*
envsubst \$NGINX_HOST},\$NGINX_ROOT,\$ERROR_PATH < "/vagrant/server/virtualhosts/default7.conf.template" > "/etc/nginx/sites-enabled/default.conf"

# Stop services
sudo service nginx stop
sudo service php7.0-fpm stop

sudo sed -i 's/user www-data;/user vagrant;/' /etc/nginx/nginx.conf


# Configure OpenSSL
sudo mkdir -p /etc/ssl/nginx/
sudo openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout /etc/ssl/nginx/${NGINX_HOST}.key -out /etc/ssl/nginx/${NGINX_HOST}.crt -subj "/C=CN/ST=GD/L=SZ/O=chenhan/OU=dev/CN=chenhan.com/emailAddress=huangzhaorongit@gmail.com"
sudo openssl dhparam -out /etc/ssl/nginx/${NGINX_HOST}.pem 2048


echo '==================================================================================================='
echo "Installing webgrind"
echo '==================================================================================================='

git -C / clone https://github.com/jokkedk/webgrind
envsubst \$NGINX_HOST},\$NGINX_ROOT < "/vagrant/server/virtualhosts/webgrind.conf.template" > "/etc/nginx/sites-enabled/webgrind.conf"


sudo service nginx restart

sudo service php7.0-fpm restart


sudo apt-get update
sudo debconf-set-selections <<< 'mysql-server mysql-server/root_password password root'
sudo debconf-set-selections <<< 'mysql-server mysql-server/root_password_again password root'
sudo apt-get install -y vim curl python-software-properties
sudo apt-get update
sudo apt-get -y install mysql-server

sed -i "s/bind-address\s*=\s*127.0.0.1/bind-address = 0.0.0.0/" /etc/mysql/mysql.conf.d/mysqld.cnf

mysql -u root -proot -e "GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY 'root' WITH GRANT OPTION; FLUSH PRIVILEGES; SET GLOBAL max_connect_errors=10000;"

mysql -u root -proot -e " CREATE DATABASE nextcloud; GRANT ALL PRIVILEGES ON nextcloud.* TO 'nextcloud'@'%' IDENTIFIED BY '$DB_PASS' WITH GRANT OPTION; FLUSH PRIVILEGES; SET GLOBAL max_connect_errors=10000;"
sudo /etc/init.d/mysql restart

echo '==================================================================================================='
echo "Installing Composer"
echo '==================================================================================================='
#curl -sS https://getcomposer.org/installer | php
#curl -sS https://packagist.laravel-china.org/installer | php
#sudo mv composer.phar /usr/local/bin/composer

sudo wget https://dl.laravel-china.org/composer.phar -O /usr/local/bin/composer
sudo chmod a+x /usr/local/bin/composer

sudo /bin/dd if=/dev/zero of=/var/swap.1 bs=1M count=1024
sudo /sbin/mkswap /var/swap.1
sudo /sbin/swapon /var/swap.1


# Configure Nextcloud
## Install application
sudo -u vagrant /usr/bin/php /app/nextcloud/occ maintenance:install --database=mysql --database-name=nextcloud --database-user=nextcloud --database-pass=$DB_PASS --admin-user=$ADMIN_USER --admin-pass=$ADMIN_PASSWORD
sudo chmod 0770 /app/nextcloud/data/
## Tweak config
sed -i '$i\ \ '\''memcache.local'\'' => '\''\\OC\\Memcache\\APCu'\'',' /app/nextcloud/config/config.php
sed -i '$i\ \ '\''memcache.distributed'\'' => '\''\\OC\\Memcache\\Memcached'\'',' /app/nextcloud/config/config.php
sed -i '$i\ \ '\''memcached_servers'\'' => array\(array\('\''localhost'\'', 11211\),\),' /app/nextcloud/config/config.php
sed -i '$i\ \ '\''datadirectory'\'' => '\''/app/nextcloud/data'\'','  /app/nextcloud/config/config.php

sudo -u vagrant /usr/bin/php /app/nextcloud/occ config:system:set trusted_domains 1 --value=${NGINX_HOST}
sudo -u vagrant /usr/bin/php /app/nextcloud/occ background:cron

## Add cronjob
echo '
# nextcloud
*/15  *  *  *  * /usr/bin/php -f /app/nextcloud/nextcloud/cron.php' > /var/spool/cron/crontabs/vagrant


echo "export PATH=$PATH:vendor/bin" | sudo tee -a ~/.bashrc > /dev/null

source ~/.bashrc

sudo service ufw stop

sudo service nginx restart
