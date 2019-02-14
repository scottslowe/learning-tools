#!/usr/bin/env bash

if [ `hostname -s` != "nextcloud-app" ]; then
	echo "Please run this script in the VM"
	exit 0
fi


EMAIL='user@example.com'
NGINXSSL_CONF='/etc/nginx/sites-enabled/default.conf'
#NGINX_CONF='/etc/nginx/sites-available/nextcloud'

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
sudo apt-get install -y zip unzip
sudo apt-get install -y python-software-properties


echo '==================================================================================================='
echo "Installing PHP"
echo '==================================================================================================='
sudo add-apt-repository ppa:ondrej/php
sudo apt-get update
sudo apt-get install -y php7.2-dev
sudo apt-get install -y php7.2-cli
sudo apt-get install -y php7.2-fpm
sudo apt-get install -y php7.2-mysql
sudo apt-get install -y php7.2-sqlite
sudo apt-get install -y php7.2-curl
sudo apt-get install -y php7.2-gd
sudo apt-get install -y php7.2-zip

sudo sed -i 's/;cgi.fix_pathinfo=1/cgi.fix_pathinfo=1/' /etc/php/7.2/fpm/php.ini
sudo sed -i 's/user = www-data/user = vagrant/' /etc/php/7.2/fpm/pool.d/www.conf
sudo sed -i 's/group = www-data/group = vagrant/' /etc/php/7.2/fpm/pool.d/www.conf
sudo sed -i 's/listen.owner = www-data/listen.owner = vagrant/' /etc/php/7.2/fpm/pool.d/www.conf
sudo sed -i 's/listen.group = www-data/listen.group = vagrant/' /etc/php/7.2/fpm/pool.d/www.conf


echo '==================================================================================================='
echo "Installing XDEBUG"
echo '==================================================================================================='

sudo apt-get install -y php-xdebug

envsubst \$ERROR_PATH < "/vagrant/server/conf/30-xdebug.ini.template" > "/etc/php/7.2/fpm/conf.d/30-xdebug.ini"

sudo service php7.2-fpm restart

echo '==================================================================================================='
echo "Installing nginx"
echo '==================================================================================================='
sudo apt-get install -y nginx
sudo rm -rf /etc/nginx/sites-available/*
sudo rm -rf /etc/nginx/sites-enabled/*
envsubst \$NGINX_HOST},\$NGINX_ROOT,\$ERROR_PATH < "/vagrant/server/virtualhosts/default.conf.template" > "/etc/nginx/sites-enabled/default.conf"



# Stop services
sudo service nginx stop
sudo service php7.2-fpm stop

sudo sed -i 's/user www-data;/user vagrant;/' /etc/nginx/nginx.conf

echo '==================================================================================================='
echo "Installing certbot"
echo '==================================================================================================='


sudo apt-get update
sudo apt-get install software-properties-common
sudo add-apt-repository ppa:certbot/certbot
sudo apt-get update
sudo apt-get install python-certbot-nginx


#envsubst \$NGINX_HOST},\$NGINX_ROOT,\$ERROR_PATH < "/vagrant/server/virtualhosts/nginxssl.conf.template" > "/etc/nginx/sites-enabled/nginxssl.conf"


sudo nginx certbot --nginx
sudo certbot --nginx certonly
sudo certbot renew --dry-run



echo '==================================================================================================='
echo "Installing webgrind"
echo '==================================================================================================='

git -C / clone https://github.com/jokkedk/webgrind
envsubst \$NGINX_HOST},\$NGINX_ROOT < "/vagrant/server/virtualhosts/webgrind.conf.template" > "/etc/nginx/sites-enabled/webgrind.conf"

sudo service nginx restart

sudo service php7.2-fpm restart

#echo '==================================================================================================='
#echo "Installing MariaDB"
#echo '==================================================================================================='
#export DEBIAN_FRONTEND=noninteractive
#debconf-set-selections <<< 'mariadb-server-10.0 mysql-server/root_password password root'
#debconf-set-selections <<< 'mariadb-server-10.0 mysql-server/root_password_again password root'
#
#sudo apt-get install -y mariadb-server
##UPDATE mysql.user SET password = PASSWORD('root') WHERE user = 'root'
#sudo mysql -e "UPDATE mysql.user SET password = PASSWORD('${DB_ROOT_PASS}') WHERE user = 'root'"
#sudo mysql -e "CREATE DATABASE ${DB_NAME} /*\!40100 DEFAULT CHARACTER SET utf8 */;"
##sudo mysql -e "CREATE USER ${DB_NAME}@localhost IDENTIFIED BY '${DB_PASS}';"
##sudo mysql -e "GRANT ALL PRIVILEGES ON ${DB_NAME}.* TO '${DB_NAME}'@'localhost';"
#
#sudo mysql -e "GRANT ALL PRIVILEGES ON ${DB_NAME}.* TO '${DB_NAME}'@'127.0.0.1' IDENTIFIED BY '${DB_PASS}' WITH GRANT OPTION;"
##sudo mysql -e "GRANT ALL PRIVILEGES ON ${DB_NAME}.* TO '${DB_NAME}'@'%';"
#sudo mysql -e "FLUSH PRIVILEGES;"



sudo apt-get update
sudo debconf-set-selections <<< 'mysql-server mysql-server/root_password password root'
sudo debconf-set-selections <<< 'mysql-server mysql-server/root_password_again password root'
sudo apt-get install -y vim curl python-software-properties
sudo apt-get update
sudo apt-get -y install mysql-server
#sed -i "s/^bind-address/#bind-address/" /etc/mysql/my.cnf

sed -i "s/bind-address\s*=\s*127.0.0.1/bind-address = 0.0.0.0/" /etc/mysql/mysql.conf.d/mysqld.cnf

mysql -u root -proot -e "GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY 'root' WITH GRANT OPTION; FLUSH PRIVILEGES; SET GLOBAL max_connect_errors=10000;"
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



## Nextcloud
#echo "CREATE DATABASE nextcloud;" | mysql -uroot
#echo "GRANT ALL PRIVILEGES ON nextcloud.* TO 'nextcloud'@'%' IDENTIFIED BY '$MYSQL_PASSWORD';" | mysql -uroot

# Configure Nextcloud
## Install application
#sudo -u www-data /usr/bin/php /var/www/nextcloud/occ maintenance:install --database=mysql --database-name=nextcloud --database-user=nextcloud --database-pass=$MYSQL_PASSWORD --admin-user=$ADMIN_USER --admin-pass=$ADMIN_PASSWORD

#sudo -u www-data /usr/bin/php /var/www/nextcloud/occ maintenance:install --database=mysql --database-name=nextcloud --database-user=nextcloud --database-pass=$DB_PASS --admin-user=$ADMIN_USER --admin-pass=$ADMIN_PASSWORD


echo "export PATH=$PATH:vendor/bin" | sudo tee -a ~/.bashrc > /dev/null

source ~/.bashrc

sudo service ufw stop
# echo '==================================================================================================='
# echo "Installing Node"
# echo '==================================================================================================='
# sudo apt-get install -y nodejs
# sudo apt-get install -y npm
# sudo npm install -g gulp