#!/bin/bash

# Simple script deletes a box from Hashicorp Atlas
# Needs
# ATLAS_USER - your atlas username
# ATLAS_TOKEN - your atlas auth token
# $1 - box to remove

for VAR in ATLAS_USER ATLAS_TOKEN
do
  if [[ -z $(eval "echo -n \"\$${VAR}\"") ]]; then
    echo -e "You must set environment variable ${VAR} before running.\n"
    FAILED_ENV="true"
  fi
done
if [[ "${FAILED_ENV}" == "true" ]]; then
  exit 1
fi

#
# Functions for manipulating the Hashicorp ATLAS API
# I found this the most reliable way of making sure that my
# boxes are created publically accessible.
#

function check_atlas {
  set +e
  # check login to atlas...
  if ! curl --fail -k -s https://atlas.hashicorp.com/api/v1 \
    -X GET  \
    -H "X-Atlas-Token: ${ATLAS_TOKEN}" >/dev/null
  then
    echo -e "\nSorry, I could not connect to Hashicorp Atlas - are you username and token correct?\n"
    set -e
    return 1
  fi
  set -e
  return 0
}

function atlas_call {
  local VERB=$1
  local CALL_PATH=$2
  local EXTRAS=$3

  local CALL="curl --fail -k -s \"${CALL_PATH}\" -X ${VERB} -H \"X-Atlas-Token: ${ATLAS_TOKEN}\" ${EXTRAS} >/dev/null"
  echo "Calling $CALL"
  if ! eval $CALL
  then
    echo "ATLAS API CALL FAILED!"
    return 1
  else
    return 0
  fi
}

function atlas_box_exists {
  local BOX=$1

  atlas_call GET https://atlas.hashicorp.com/api/v1/box/${BOX}
}

function atlas_box_create {
  local BOX=$1

  echo -e "Creating atlas box ${BOX}"
  if ! atlas_call POST https://atlas.hashicorp.com/api/v1/boxes "-d box[name]=\"${BOX}\" -d box[is_private]='false'"
  then
    echo -e "Sorry couldn't create the new box ${BOX}"
    return 1
  fi
  return 0
}

function atlas_box_delete {
  local USER=$1
  local BOX=$2

  echo -e "Deleting atlas box ${BOX}"
  if ! atlas_call DELETE https://atlas.hashicorp.com/api/v1/box/${BOX}
  then
    echo -e "Sorry couldn't delete ${USER}'s box ${BOX}"
    return 1
  fi
  return 0
}

check_atlas || exit 1
if atlas_box_exists $1
then
  atlas_box_delete ${ATLAS_USER} $1
fi
