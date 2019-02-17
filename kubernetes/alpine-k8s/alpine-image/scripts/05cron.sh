set -ux

echo "Adding a more regular 1min cron category"
echo "*       *       *       *       *       run-parts /etc/periodic/1min" >>/etc/crontabs/root
mkdir -p /etc/periodic/1min
