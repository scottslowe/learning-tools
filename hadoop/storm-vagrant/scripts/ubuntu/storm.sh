apt-get install -y supervisor

cd /usr/local

wget http://apache.claz.org/storm/apache-storm-1.2.1/apache-storm-1.2.1.tar.gz

tar -xvzf apache-storm-1.2.1.tar.gz

ln -s apache-storm-1.2.1 storm

mkdir /usr/local/storm/data

mv /home/vagrant/apache-storm/conf/storm.yaml /usr/local/storm/conf/storm.yaml

rm -rf /home/vagrant/apache-storm

chmod +x /usr/local/storm/bin/storm

mkdir /var/log/storm

echo export PATH=$PATH:/usr/local/storm/bin >> /etc/environment

source /etc/environment
