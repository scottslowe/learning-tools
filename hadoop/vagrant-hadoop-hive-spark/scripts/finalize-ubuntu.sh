#!/bin/bash

source "/vagrant/scripts/common.sh"

function setupUtilities {
    # update the locate database
    updatedb
}

function setupVIM {
#    Use the full vim version 
    apt-get remove -y vim.tiny
    apt-get install -y vim
}

function setupNetStat {
#   Setup netstat (usful for debug)
    apt-get install -y net-tools
}
echo "finalize ubuntu"
setupUtilities
echo "setup vim"
setupVIM
echo "setup netstat"
setupNetStat
