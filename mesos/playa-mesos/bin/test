#!/usr/bin/env bash

# get the root directory of the project
declare -r pm_root="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"

# assert.sh unit test framework
. "${pm_root}/lib/assert.sh/assert.sh"

# Requirements
declare -r min_vbox_ver=4.2
declare -r min_vagrant_ver=1.3

# Ensure required commands are available
assert_raises "hash vagrant" 0
assert_raises "hash VBoxManage" 0
assert_raises "hash bc" 0
assert_raises "hash sed" 0

# Test bc
assert bc "0" "1<0"
assert bc "1" "1>0"

# Check VirtualBox Version
declare -r vbox_ver=$(VBoxManage --version|sed 's/\([0-9]*\.[0-9]*\).*/\1/')
echo "VirtualBox version discovered: ${vbox_ver}"
assert bc "1" "${vbox_ver}>=${min_vbox_ver}"

# Check Vagrant Version
declare -r vagrant_ver=$(vagrant --version|sed 's/Vagrant \([0-9]*\.[0-9]*\).*/\1/')
echo "Vagrant version discovered: ${vagrant_ver}"
assert bc "1" "${vagrant_ver}>=${min_vagrant_ver}"

assert_end playa-mesos
