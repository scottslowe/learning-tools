#!/usr/bin/env bash
# Check arguments
#if [ -z "$2" ]
#then
#    echo 'Usage:'
#    echo './install-nextcloud.sh user@example.com cloud.example.com'
#    exit 1
#fi

if [ `hostname -s` != "nextcloud-app" ]; then
	echo "Please run this script in the VM"
	exit 0
fi

# Define constants
EMAIL='user@example.com'
NCDOMAIN='cloud.example.com'
NCPATH='/var/www/nextcloud'
NCREPO='https://download.nextcloud.com/server/releases'
NCVERSION=$(curl -s -m 900 $NCREPO/ | sed --silent 's/.*href="nextcloud-\([^"]\+\).zip.asc".*/\1/p' | sort --version-sort | tail -1)
STABLEVERSION="nextcloud-$NCVERSION"
HTML='/var/www'
HTUSER='www-data'
HTGROUP='www-data'
NGINX_CONF='/etc/nginx/sites-available/nextcloud'
PHP_INI='/etc/php/7.0/fpm/php.ini'
PHP_CONF='/etc/php/7.0/fpm/pool.d/www.conf'
PGSQL_PASSWORD=$(tr -dc "a-zA-Z0-9" < /dev/urandom | fold -w "64" | head -n 1)
REDIS_CONF='/etc/redis/redis.conf'
REDIS_SOCK='/var/run/redis/redis.sock'

# Download Nextcloud
sudo wget -q --show-progress -T 10 -t 2 "${NCREPO}/${STABLEVERSION}.tar.bz2" -P "$HTML"
sudo tar -xjf "${HTML}/${STABLEVERSION}.tar.bz2" -C "${HTML}"
sudo rm "${HTML}/${STABLEVERSION}.tar.bz2"

# Update permissions
sudo chown -R ${HTUSER}:${HTGROUP} ${NCPATH} -R

# Stop services
sudo service nginx stop
sudo service php7.0-fpm stop

# Configure OpenSSL
sudo mkdir -p /etc/ssl/nginx/
sudo openssl req -x509 -nodes -days 365 -newkey rsa:4096 -keyout /etc/ssl/nginx/${NCDOMAIN}.key -out /etc/ssl/nginx/${NCDOMAIN}.crt
sudo openssl dhparam -out /etc/ssl/nginx/${NCDOMAIN}.pem 4096

# Configure nginx
TEMP=$(mktemp)
cat <<CONFIG_NGINX > ${TEMP}
upstream php-handler {
    #server 127.0.0.1:9000;
    server unix:/run/php/php7.0-fpm.sock;
}

server {
    listen 80;
    listen [::]:80;
    server_name ${NCDOMAIN};
    # enforce https
    return 301 https://\$server_name\$request_uri;
}

server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name ${NCDOMAIN};

    ssl_certificate /etc/ssl/nginx/${NCDOMAIN}.crt;
    ssl_certificate_key /etc/ssl/nginx/${NCDOMAIN}.key;
    #ssl_certificate /etc/letsencrypt/live/${NCDOMAIN}/fullchain.pem;
    #ssl_certificate_key /etc/letsencrypt/live/${NCDOMAIN}/privkey.pem;
    #ssl_trusted_certificate /etc/letsencrypt/live/${NCDOMAIN}/chain.pem;

    # Add headers to serve security related headers
    # Before enabling Strict-Transport-Security headers please read into this
    # topic first.
    add_header Strict-Transport-Security "max-age=15768000; includeSubDomains; preload;";
    #
    # WARNING: Only add the preload option once you read about
    # the consequences in https://hstspreload.org/. This option
    # will add the domain to a hardcoded list that is shipped
    # in all major browsers and getting removed from this list
    # could take several months.
    add_header X-Content-Type-Options nosniff;
    add_header X-XSS-Protection "1; mode=block";
    add_header X-Robots-Tag none;
    add_header X-Download-Options noopen;
    add_header X-Permitted-Cross-Domain-Policies none;

    # Cipherli.st strong ciphers
    ssl_protocols TLSv1.2;# Requires nginx >= 1.13.0 else use TLSv1.2
    ssl_prefer_server_ciphers on;
    ssl_dhparam /etc/ssl/nginx/${NCDOMAIN}.pem; # openssl dhparam -out /etc/nginx/dhparam.pem 4096
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512:ECDHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-SHA384;
    ssl_ecdh_curve secp384r1; # Requires nginx >= 1.1.0
    ssl_session_timeout  10m;
    ssl_session_cache shared:SSL:10m;
    ssl_session_tickets off; # Requires nginx >= 1.5.9
    ssl_stapling on; # Requires nginx >= 1.3.7
    ssl_stapling_verify on; # Requires nginx => 1.3.7

    # Path to the root of your installation
    root /var/www/nextcloud/;

    location = /robots.txt {
        allow all;
        log_not_found off;
        access_log off;
    }

    # The following 2 rules are only needed for the user_webfinger app.
    # Uncomment it if you're planning to use this app.
    #rewrite ^/.well-known/host-meta /public.php?service=host-meta last;
    #rewrite ^/.well-known/host-meta.json /public.php?service=host-meta-json
    # last;

    location = /.well-known/carddav {
      return 301 \$scheme://\$host/remote.php/dav;
    }
    location = /.well-known/caldav {
      return 301 \$scheme://\$host/remote.php/dav;
    }

    # Let's Encrypt
    location ~ /.well-known/acme-challenge {
      allow all;
    }

    # set max upload size
    client_max_body_size 512M;
    fastcgi_buffers 64 4K;

    # Enable gzip but do not remove ETag headers
    gzip on;
    gzip_vary on;
    gzip_comp_level 4;
    gzip_min_length 256;
    gzip_proxied expired no-cache no-store private no_last_modified no_etag auth;
    gzip_types application/atom+xml application/javascript application/json application/ld+json application/manifest+json application/rss+xml application/vnd.geo+json application/vnd.ms-fontobject application/x-font-ttf application/x-web-app-manifest+json application/xhtml+xml application/xml font/opentype image/bmp image/svg+xml image/x-icon text/cache-manifest text/css text/plain text/vcard text/vnd.rim.location.xloc text/vtt text/x-component text/x-cross-domain-policy;

    # Uncomment if your server is build with the ngx_pagespeed module
    # This module is currently not supported.
    #pagespeed off;

    location / {
        rewrite ^ /index.php\$uri;
    }

    location ~ ^/(?:build|tests|config|lib|3rdparty|templates|data)/ {
        deny all;
    }
    location ~ ^/(?:\.|autotest|occ|issue|indie|db_|console) {
        deny all;
    }

    location ~ ^/(?:index|remote|public|cron|core/ajax/update|status|ocs/v[12]|updater/.+|ocs-provider/.+)\.php(?:\$|/) {
        fastcgi_split_path_info ^(.+\.php)(/.*)\$;
        include fastcgi_params;
        fastcgi_param SCRIPT_FILENAME \$document_root\$fastcgi_script_name;
        fastcgi_param PATH_INFO \$fastcgi_path_info;
        fastcgi_param HTTPS on;
        #Avoid sending the security headers twice
        fastcgi_param modHeadersAvailable true;
        fastcgi_param front_controller_active true;
        fastcgi_pass php-handler;
        fastcgi_intercept_errors on;
        fastcgi_request_buffering off;
    }

    location ~ ^/(?:updater|ocs-provider)(?:\$|/) {
        try_files \$uri/ =404;
        index index.php;
    }

    # Adding the cache control header for js and css files
    # Make sure it is BELOW the PHP block
    location ~ \.(?:css|js|woff|svg|gif)\$ {
        try_files \$uri /index.php\$uri\$is_args\$args;
        add_header Cache-Control "public, max-age=15778463";
        # Add headers to serve security related headers (It is intended to
        # have those duplicated to the ones above)
        # Before enabling Strict-Transport-Security headers please read into
        # this topic first.
        add_header Strict-Transport-Security "max-age=15768000; includeSubDomains; preload;";
        #
        # WARNING: Only add the preload option once you read about
        # the consequences in https://hstspreload.org/. This option
        # will add the domain to a hardcoded list that is shipped
        # in all major browsers and getting removed from this list
        # could take several months.
        add_header X-Content-Type-Options nosniff;
        add_header X-XSS-Protection "1; mode=block";
        add_header X-Robots-Tag none;
        add_header X-Download-Options noopen;
        add_header X-Permitted-Cross-Domain-Policies none;
        # Optional: Don't log access to assets
        access_log off;
    }

    location ~ \.(?:png|html|ttf|ico|jpg|jpeg)\$ {
        try_files \$uri /index.php\$uri\$is_args\$args;
        # Optional: Don't log access to other assets
        access_log off;
    }
}
CONFIG_NGINX
sudo cp ${TEMP} ${NGINX_CONF}
sudo chmod 644 ${NGINX_CONF}
rm -f ${TEMP}
unset TEMP
sudo rm -f /etc/nginx/sites-enabled/default
sudo ln -s ${NGINX_CONF} /etc/nginx/sites-enabled/nextcloud

# Configure PostgreSQL
sudo -u postgres psql -c "CREATE USER nextcloud WITH PASSWORD '${PGSQL_PASSWORD}';"
sudo -u postgres psql -c "CREATE DATABASE nextcloud TEMPLATE template0 ENCODING 'UNICODE';"
sudo -u postgres psql -c "ALTER DATABASE nextcloud OWNER TO nextcloud;"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE nextcloud TO nextcloud;"

# Configure PHP
sudo sed -i "s|;env|env|g" ${PHP_CONF}
sudo sed -i "s|;opcache.enable=0|opcache.enable=1|g" ${PHP_INI}
sudo sed -i "s|;opcache.enable_cli=0|opcache.enable_cli=1|g" ${PHP_INI}
sudo sed -i "s|;opcache.memory_consumption=64|opcache.memory_consumption=128|g" ${PHP_INI}
sudo sed -i "s|;opcache.interned_strings_buffer=4|opcache.interned_strings_buffer=8|g" ${PHP_INI}
sudo sed -i "s|;opcache.max_accelerated_files=2000|opcache.max_accelerated_files=10000|g" ${PHP_INI}
sudo sed -i "s|;opcache.revalidate_freq=2|opcache.revADD_TO_CONFIGalidate_freq=1|g" ${PHP_INI}
sudo sed -i "s|;opcache.save_comments=1|opcache.save_comments=1|g" ${PHP_INI}

# Configure Redis
sudo sed -i "s|# unixsocket|unixsocket|g" ${REDIS_CONF}
sudo sed -i "s|unixsocketperm .*|unixsocketperm 770|g" ${REDIS_CONF}
sudo sed -i "s|^port.*|port 0|g" ${REDIS_CONF}
sudo chown redis:root ${REDIS_CONF}
sudo chmod 600 ${REDIS_CONF}
sudo usermod -a -G redis ${HTUSER}
sudo service redis-server restart

# Start Nextcloud
sudo service php7.0-fpm start
sudo service nginx start

# Display database configuration information
echo "Configure the database"
echo "Database user: nextcloud"
echo "Database password: ${PGSQL_PASSWORD}"
echo "Database name: nextcloud"
echo

# Wait for Nextcloud web installation to complete
printf "Waiting for Nextcloud web installation to complete"
while ! sudo test -f ${NCPATH}/config/config.php
do
  printf "."
  sleep 6
done
sleep 10
printf "OK"

# Prompt user to press any key
read -n 1 -s -r -p "Press any key to continue"

# Stop services
sudo service nginx stop
sudo service php7.0-fpm stop
sudo service redis-server stop

# Update Nextcloud config
TEMP=$(mktemp)
sudo cp --no-preserve=mode,ownership ${NCPATH}/config/config.php ${TEMP}
sudo sed -i "s|);||g" ${TEMP}
cat <<UPDATE_NCCONFIG >> ${TEMP}
  'memcache.locking' => '\\OC\\Memcache\\Redis',
  'memcache.local' => '\\OC\\Memcache\\Redis',
  'redis' =>
  array (
    'host' => '${REDIS_SOCK}',
    'port' => 0,
  ),
);
UPDATE_NCCONFIG
sudo cp --no-preserve=mode,ownership ${TEMP} ${NCPATH}/config/config.php
sed -i '/^\s*$/d' ${NCPATH}/config/config.php

# Restart services
sudo systemctl enable redis-server
sudo service redis-server start
sudo service php7.0-fpm start
sudo service nginx start

# Let's Encrypt
sudo letsencrypt certonly --webroot --agree-tos --email ${EMAIL} -d ${NCDOMAIN} -w ${NCPATH}
sed -i "s|ssl_certificate /etc/ssl|#ssl_certificate /etc/ssl|g" ${NGINX_CONF}
sed -i "s|ssl_certificate_key /etc/ssl|#ssl_certificate_key /etc/ssl|g" ${NGINX_CONF}
sed -i "s|#ssl_certificate /etc/letsencrypt|ssl_certificate /etc/letsencrypt|g" ${NGINX_CONF}
sed -i "s|#ssl_certificate_key /etc/letsencrypt|ssl_certificate_key /etc/letsencrypt|g" ${NGINX_CONF}
sed -i "s|#ssl_trusted_certificate /etc/letsencrypt|ssl_trusted_certificate /etc/letsencrypt|g" ${NGINX_CONF}

# Restart services
sudo service nginx stop
sudo service php7.0-fpm stop
sudo service redis-server stop
sudo service redis-server start
sudo service php7.0-fpm start
sudo service nginx start
