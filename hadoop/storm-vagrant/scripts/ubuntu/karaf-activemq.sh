service karaf stop

/usr/local/karaf/bin/start

sleep 60

/usr/local/karaf/bin/client feature:repo-add mvn:org.apache.activemq/activemq-karaf/5.14.5/xml/features

/usr/local/karaf/bin/client feature:install activemq
/usr/local/karaf/bin/client feature:install activemq-broker

/usr/local/karaf/bin/client feature:start activemq
/usr/local/karaf/bin/client feature:start activemq-broker

/usr/local/karaf/bin/stop

service karaf start