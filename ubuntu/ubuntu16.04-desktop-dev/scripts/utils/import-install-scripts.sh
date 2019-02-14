#!/bin/sh

import-install-scripts() { 
	IMPORTS=$1
	
    for IMPORT in ${IMPORTS[@]}; do
        . /tmp/vagrant/scripts/install/$IMPORT.sh;
        echo "Imported package $IMPORT.sh";
    done
}
