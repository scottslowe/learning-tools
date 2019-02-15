cp /home/vagrant/supervisor/conf/nimbus.conf /etc/supervisor/conf.d/nimbus.conf

rm -rf /home/vagrant/supervisor

chown root:root /etc/supervisor/conf.d/nimbus.conf
chmod 755 /etc/supervisor/conf.d/nimbus.conf

supervisorctl reread
supervisorctl update

apt-get install -y git
apt-get install -y maven

cd /home/vagrant/share

git clone https://github.com/wellingWilliam/skyline.git

cd skyline

mvn clean install

cd ..

git clone https://github.com/wellingWilliam/storm-skyline.git

cd storm-skyline

mvn clean package
