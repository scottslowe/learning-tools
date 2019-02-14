#!/bin/bash
echo "Installing the latest ruby"
rbenv install 2.4.1
rbenv rehash
rbenv global 2.4.1
exit