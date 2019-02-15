cd /usr/local

wget http://apache.claz.org/karaf/4.1.5/apache-karaf-4.1.5.tar.gz
tar -xvzf apache-karaf-4.1.5.tar.gz
ln -s apache-karaf-4.1.5 karaf

/usr/local/karaf/bin/start

sleep 60

/usr/local/karaf/bin/client feature:install wrapper

/usr/local/karaf/bin/client wrapper:install

/usr/local/karaf/bin/stop

ln -s /usr/local/karaf/bin/karaf-service /etc/init.d/karaf

update-rc.d karaf defaults

service karaf start

echo export PATH=$PATH:/usr/local/karaf/bin >> /etc/environment

source /etc/environment