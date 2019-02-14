#######################################################
# Carsten Rieger IT-Services
# SSL-CERTIFICATE.SH
# Version 1.1
# September, 20th, 2018
# Version 2.0
#######################################################
#!/bin/bash
###global function to update and cleanup the environment
function update_and_clean() {
apt update
apt upgrade -y
apt autoclean -y
apt autoremove -y
}
###global function to restart all cloud services
function restart_all_services() {
/usr/sbin/service nginx restart
/usr/sbin/service mysql restart
/usr/sbin/service redis-server restart
/usr/sbin/service php7.2-fpm restart
}
###global function to create backups of the effected files 
function copy4SSL() {
cp /etc/nginx/conf.d/nextcloud.conf /etc/nginx/conf.d/nextcloud.conf.orig
cp /etc/nginx/ssl.conf /etc/nginx/ssl.conf.orig
cp /var/www/nextcloud/config/config.php /var/www/nextcloud/config/config.php.orig
}
###global function to show an error message if the certificate request would fail
function errorSSL() {
clear
echo "*** ERROR while requeting your certificate(s) ***"
echo ""
echo "Verify that both ports (80 + 443) are forwarded to this server!"
echo "And verify, your dyndns points to your IP either!"
echo "Then retry..."
}
###add the letsencrypt repository to the server
add-apt-repository ppa:certbot/certbot -y
update_and_clean
###install letsencrypt
apt install letsencrypt -y
###ensure lower cases
declare -l DYNDNSNAME
declare -l YOURSERVERNAME
###read the current hostname
YOURSERVERNAME=$(hostname)
###ask the user what would be the domain name to request the certificate for
read -p "Your domain: " DYNDNSNAME
###request the certificate for the entered domain name
letsencrypt certonly -a webroot --webroot-path=/var/www/letsencrypt --rsa-key-size 4096 -d $DYNDNSNAME
###in case of any errors
if [ ! -d "/etc/letsencrypt/live" ]; then
errorSSL
###if no error appeared
else
copy4SSL
###remove the entry for the self signed certificates
sed -i '/ssl-cert-snakeoil/d' /etc/nginx/ssl.conf
###apply the new server/domain name to the NGINX vhost for Nextcloud
sed -i "s/server_name.*;/server_name $DYNDNSNAME;/" /etc/nginx/conf.d/nextcloud.conf
###change the placeholder values to the exact letsencrypt ones
sed -in 's/YOUR.DEDYN.IO/'$DYNDNSNAME'/' /etc/nginx/ssl.conf
###remove the '#' to enable these certificates/keys
sed -i s/\#\ssl/\ssl/g /etc/nginx/ssl.conf
###ensure that dhparam is commented as long it was not generated 
sed -i s/ssl_dhparam/\#ssl_dhparam/g /etc/nginx/ssl.conf
###adjust Nextclous config.php to the new domain name
sudo -u www-data php /var/www/nextcloud/occ config:system:set trusted_domains 1 --value=$DYNDNSNAME
sudo -u www-data php /var/www/nextcloud/occ config:system:set overwrite.cli.url --value=https://$DYNDNSNAME
#sudo -u www-data sed -in 's/'$YOURSERVERNAME'/'$DYNDNSNAME'/' /var/www/nextcloud/config/config.php
###restart the cloud environment
restart_all_services
clear
echo "++++++++++++++++++++++++++++++++++++++++++++++++++++"
echo ""
echo " Call: https://$DYNDNSNAME and enjoy your Nextcloud"
echo ""
echo "++++++++++++++++++++++++++++++++++++++++++++++++++++"
fi
history -c && history -w
exit 0
