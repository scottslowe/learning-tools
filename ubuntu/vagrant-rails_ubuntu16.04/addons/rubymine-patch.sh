#!/bin/sh
vagrant ssh-config | sed 's/^Host default/Host 127.0.0.1/' >> ~/.ssh/config