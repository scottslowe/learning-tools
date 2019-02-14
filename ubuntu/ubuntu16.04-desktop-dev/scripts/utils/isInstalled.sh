#!/bin/sh

isInstalled() { 
    package=$1
    
    echo "Checking if $package is installed..."
    result=$(dpkg-query --show --showformat='${db:Status-Status}' $package)

    if [ "$result" = "installed" ]; 
        then 
	        echo "$package is already installed!";
            return 0;
        else
            echo "$package is not installed!";
            return 1;
    fi
}
