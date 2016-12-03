#!/bin/bash

# Set some variables for later
VER="1.20.0"
URL="https://github.com/coreos/rkt/releases/download"
FILE="rkt-v$VER.tar.gz"
DIR="rkt-v$VER"

# Install curl if needed
if [[ ! -e /usr/bin/curl ]]; then
  apt-get update
  apt-get -yqq install curl
fi

# Download the rkt tarball if it doesn't already exist
if [[ ! -e /home/vagrant/$FILE ]]; then
  # Download files for rkt
  curl -LO $URL/v$VER/rkt-v$VER.tar.gz
fi

# If the rkt binary is in /usr/local/bin, assume the system is provisioned
if [[ ! -e /usr/local/bin/rkt ]]; then
  # Unpack the downloaded file
  tar xzvf $FILE

  # Move rkt to /usr/local/bin and ensure it is executable
  cd $DIR
  sudo mv rkt /usr/local/bin/
  sudo chmod a+x /usr/local/bin/rkt

  # Move ACI files to /usr/local/bin
  sudo mv stage1-coreos.aci /usr/local/bin/
  sudo mv stage1-fly.aci /usr/local/bin/
  sudo mv stage1-kvm.aci /usr/local/bin/

  # Create working directory for rkt
  sudo mkdir -p /var/lib/rkt

  # Create rkt group if it doesn't already exist
  [ $(getent group rkt) ] || sudo groupadd rkt
  
  # Remove directory created by unpacking tarball
  # Leave tarball in place (indicates provisioned system)
  cd ..
  rm -rf rkt-v$VER
fi
