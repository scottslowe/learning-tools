# c-rieger.de - install Nextcloud using a shell script
The initial script (install-nextcloud.sh) will install your self hosted Nextcloud within few minutes fully automated. Your server will be built of:

    Fail2Ban (Nextcloud and SSH jails)
    MariaDB 10.3
    Nextcloud 14.0.3
    NGINX 1.15.6 TLSv.1.3
    OpenSSL 1.1.1
    PHP 7.2
    Redis-Server
    self signed or Let's Encrypt SSL using the second script
    UFW (22, 80, 443)

The only precondition for the install-nextcloud.sh script is to utilize Ubuntu 18.04 LTS as your on-prem server OS. Optionally request your ssl certificate from Let's Encrypt by issuing the second script called "ssl-certificate.sh".

Ready to go? Let's start:

Find out more information: <a href="https://www.c-rieger.de/spawn-your-nextcloud-server-using-one-shell-script/" target='_blank'>Build your Nextcloud Server using shell scripts only</a>

Beyond that you will find additional scripts at /usr/local/src/install-nextcloud/

    fail2ban.sh
    optimize.sh
    restart.sh
    ssl-renewal.sh

to optimize and maintain your system easily.

Carsten Rieger IT-Services (c-rieger.de)
