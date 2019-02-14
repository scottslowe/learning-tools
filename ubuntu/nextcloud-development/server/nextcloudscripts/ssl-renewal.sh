####
# renew your letsencrypt ssl certificates weekly
######################################################################
# PLEASE SUBSTITUTE username PROPERLY, e.g. nextcloud
# PLEASE SUBSTITUTE Your Name PROPERLY, e.g. Carsten Rieger
# PLEASE SUBSTITUTE your@email.com PROPERLY, e.g. info@nextcloud.com
######################################################################
# create a cronjob, e.g.:
# crontab -e
# @weekly /usr/local/src/install-nextcloud/ssl-renewal.sh > /home/username/ssl-renewal.txt
####

#!/bin/bash
CURRENT_TIME_FORMATE="%d.%m.%Y"
cd /etc/letsencrypt
echo "-------------------------------------"
echo "Renewal:"
letsencrypt renew
result=$(find /etc/letsencrypt/live/ -type l -mtime -1 )
if [ -n "$result" ]; then
        /usr/sbin/service nginx stop
        /usr/sbin/service mysql restart
        /usr/sbin/service redis-server restart
        /usr/sbin/service php7.2-fpm restart
        /usr/sbin/service nginx restart
fi
mail -s "Renewal - $(date +$CURRENT_TIME_FORMATE)" -a "FROM: Your Name <your@email.com>" your@email.com < /home/username/ssl-renewal.txt
exit 0
