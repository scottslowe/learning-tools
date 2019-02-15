service karaf stop

/usr/local/karaf/bin/start

sleep 60

/usr/local/karaf/bin/client feature:repo-add mvn:io.hawt/hawtio-karaf/1.5.0/xml/features

/usr/local/karaf/bin/client feature:install hawtio

/usr/local/karaf/bin/client feature:start hawtio

/usr/local/karaf/bin/stop

service karaf start