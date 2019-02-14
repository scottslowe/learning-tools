#!/bin/sh

install-packages() { 
	PACKAGES=$1
	
    for PACKAGE in ${PACKAGES[@]}; do
        if [ -e "/var/lib/dpkg/lock" ];
            then
                count=10
                while [ -e "/var/lib/dpkg/lock" ]
                do
                    let "count -= 1";

                    echo "($count) Trying to install package $PACKAGE but apt or dpkg is still locked. Will continue after $count seconds...";
                    sleep 1;
                    
                    if [ $count = 1 ]; then
                        rm /var/lib/dpkg/lock
                    fi
                done

                echo "-----------------------------Executing script install_$PACKAGE.sh >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>";
                install_$PACKAGE;
            else
                echo "-----------------------------Executing script install_$PACKAGE.sh >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>";
                install_$PACKAGE;
        fi
    done
}
