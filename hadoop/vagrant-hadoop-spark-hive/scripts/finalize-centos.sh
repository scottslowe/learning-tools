#!/bin/bash


function setupUtilities {
    # update the locate database
    updatedb
}

echo "finalize centos"
setupUtilities
