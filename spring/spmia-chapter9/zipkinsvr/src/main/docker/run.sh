#!/bin/sh

echo "********************************************************"
echo "Starting the Zipkin Server"
echo "********************************************************"
java -Djava.security.egd=file:/dev/./urandom -Dserver.port=$SERVER_PORT   \
     -Dspring.profiles.active=$PROFILE                          \
      -Dspring.zipkin.baseUrl=$ZIPKIN_URI                       \
     -jar /usr/local/zipkinserver/@project.build.finalName@.jar
