mv /home/vagrant/supervisor/conf/supervisor.conf /etc/supervisor/conf.d/supervisor.conf

rm -rf /home/vagrant/supervisor

chown root:root /etc/supervisor/conf.d/supervisor.conf
chmod 755 /etc/supervisor/conf.d/supervisor.conf

supervisorctl reread
supervisorctl update
