#!/bin/sh

# Check for Homebrew
command -v brew 2>&1 > /dev/null || { echo "ERROR: Homebrew Not Installed" 1>&2; exit 1;}
brew update
# Install Packages
brew tap Homebrew/bundle
brew bundle --verbose

# Complete Python Installation
brew linkapps python
pip install --upgrade pip setuptools
# Install Ansible
sudo -H pip install ansible
