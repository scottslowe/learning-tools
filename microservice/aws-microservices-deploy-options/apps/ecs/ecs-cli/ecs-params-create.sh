#!/bin/bash
function usage {
if [[ $# -eq 0 ]] ; then
    echo 'Usage: ./ecs-params-create.sh <SERVICENAME (Allowed values - name|webapp|greeting>'
    exit 0
fi
}

SERVICENAME="$1"

if ! [[ "$SERVICENAME" =~ ^(name|webapp|greeting)$ ]]; then usage ; fi

mkdir -p $SERVICENAME

source ecs-cluster.props

PARAM_FILE=ecs-params_"$SERVICENAME".yaml
cp ecs-params.template $PARAM_FILE  

perl -i -pe 's/ECSRole/'${ECSRole}'/g' $PARAM_FILE
perl -i -pe 's/servicename/'${SERVICENAME}'/g' $PARAM_FILE
if [ "$SERVICENAME" != "webapp" ]; then
	perl -i -pe 's/subnet1/'${PrivateSubnet1}'/g' $PARAM_FILE
	perl -i -pe 's/subnet2/'${PrivateSubnet2}'/g' $PARAM_FILE
else
        perl -i -pe 's/subnet1/'${PublicSubnet1}'/g' $PARAM_FILE
	perl -i -pe 's/subnet2/'${PublicSubnet2}'/g' $PARAM_FILE
fi
perl -i -pe 's/sg-replaceme/'${SecurityGroupWebapp}'/g' $PARAM_FILE


