#!/bin/sh

# install Cloud Foundry client
cd /opt && curl -L "https://cli.run.pivotal.io/stable?release=linux64-binary&source=github" | tar -zx
sudo ln -s /opt/cf /usr/bin/cf
