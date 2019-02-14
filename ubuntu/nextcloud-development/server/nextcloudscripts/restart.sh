###
# restart all services related to your Nextcloud instance
# issue by e.g.: /usr/local/src/install-nextcloud/restart.sh
###
#!/bin/bash
/usr/sbin/service nginx stop
/usr/sbin/service mysql restart
/usr/sbin/service redis-server restart
/usr/sbin/service php7.2-fpm restart
/usr/sbin/service nginx restart
exit 0
