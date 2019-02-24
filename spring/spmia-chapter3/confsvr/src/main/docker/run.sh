#!/bin/sh
echo "********************************************************"
echo "Starting Configuration Server"
echo "********************************************************"
java -jar /usr/local/configserver/@project.build.finalName@.jar
