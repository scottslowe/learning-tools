#!/bin/bash

# Check if the AWS CLI is in the PATH
found=$(which aws)
if [ -z "$found" ]; then
  echo "Please install the AWS CLI under your PATH: http://aws.amazon.com/cli/"
  exit 1
fi

# Check if jq is in the PATH
found=$(which jq)
if [ -z "$found" ]; then
  echo "Please install jq under your PATH: http://stedolan.github.io/jq/"
  exit 1
fi

# Read other configuration from config.json
AWS_ACCOUNT_ID=$(jq -r '.AWS_ACCOUNT_ID' config.json)
CLI_PROFILE=$(jq -er '.CLI_PROFILE' config.json)
# Get jq return code set by the -e option
CLI_PROFILE_RC=$?
REGION=$(jq -r '.REGION' config.json)
BUCKET=$(jq -r '.BUCKET' config.json)
MAX_AGE=$(jq -r '.MAX_AGE' config.json)
DDB_TABLE=$(jq -r '.DDB_TABLE' config.json)
IDENTITY_POOL_NAME=$(jq -r '.IDENTITY_POOL_NAME' config.json)
DEVELOPER_PROVIDER_NAME=$(jq -r '.DEVELOPER_PROVIDER_NAME' config.json)

#if a CLI Profile name is provided... use it.
if [[ $CLI_PROFILE_RC == 0 ]]; then
  echo "Setting session CLI profile to $CLI_PROFILE"
  export AWS_DEFAULT_PROFILE=$CLI_PROFILE
fi

# Create S3 Bucket
aws s3 mb s3://$BUCKET

# Create DynamoDB Tables
echo "Creating DynamoDB Table $DDB_TABLE begin..."
aws dynamodb create-table --table-name $DDB_TABLE \
    --attribute-definitions AttributeName=email,AttributeType=S \
    --key-schema AttributeName=email,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
		--region $REGION
echo "Creating DynamoDB Table $DDB_TABLE end (creation still in progress)"

# Create Cognito Identity Pool
IDENTITY_POOL_ID=$(aws cognito-identity list-identity-pools --max-results 1 \
	  --query 'IdentityPools[?IdentityPoolName == `'$IDENTITY_POOL_NAME'`].IdentityPoolId' \
	  --output text --region $REGION)
if [ -z "$IDENTITY_POOL_ID" ]; then
	echo "Creating Cognito Identity Pool $IDENTITY_POOL_NAME begin..."
	IDENTITY_POOL_ID=$(aws cognito-identity create-identity-pool --identity-pool-name $IDENTITY_POOL_NAME \
	    --allow-unauthenticated-identities --developer-provider-name $DEVELOPER_PROVIDER_NAME \
	    --query 'IdentityPoolId' --output text --region $REGION)
	echo "Identity Pool Id: $IDENTITY_POOL_ID"
	echo "Creating Cognito Identity Pool $IDENTITY_POOL_NAME end"
else
  echo "Using previous identity pool with name $IDENTITY_POOL_NAME and id $IDENTITY_POOL_ID"
fi

# Updating Cognito Identity Pool Id in the configuration file
mv config.json config.json.orig
jq '.IDENTITY_POOL_ID="'"$IDENTITY_POOL_ID"'"' config.json.orig > config.json
rm config.json.orig

cd iam
if [ -d "edit" ]; then
  rm edit/*
else
  mkdir edit
fi

# Create IAM Roles for Cognito
for f in $(ls -1 Policy_Trust_*); do
  echo "Editing trust from $f begin..."
  sed -e "s/<AWS_ACCOUNT_ID>/$AWS_ACCOUNT_ID/g" \
      -e "s/<DYNAMODB_TABLE>/$DDB_TABLE/g" \
      -e "s/<DYNAMODB_EMAIL_INDEX>/$DDB_EMAIL_INDEX/g" \
      -e "s/<REGION>/$REGION/g" \
      -e "s/<IDENTITY_POOL_ID>/$IDENTITY_POOL_ID/g" \
      -e "s/<REGION>/$REGION/g" \
      $f > edit/$f
  echo "Editing trust from $f end"
done
for f in $(ls -1 Policy_Cognito_*); do
  role="${f%.*}"
  echo "Creating role $role from $f begin..."
  sed -e "s/<AWS_ACCOUNT_ID>/$AWS_ACCOUNT_ID/g" \
      -e "s/<DYNAMODB_TABLE>/$DDB_TABLE/g" \
      -e "s/<DYNAMODB_EMAIL_INDEX>/$DDB_EMAIL_INDEX/g" \
      -e "s/<REGION>/$REGION/g" \
      -e "s/<IDENTITY_POOL_ID>/$IDENTITY_POOL_ID/g" \
      -e "s/<REGION>/$REGION/g" \
	      $f > edit/$f
  if [[ $f == *_Unauth_* ]]; then
    trust="Policy_Trust_Cognito_Unauth_Role.json"
    unauthRole="$role"
  else
    trust="Policy_Trust_Cognito_Auth_Role.json"
    authRole="$role"
  fi
  aws iam create-role --role-name $role --assume-role-policy-document file://edit/$trust
  aws iam update-assume-role-policy --role-name $role --policy-document file://edit/$trust
  aws iam put-role-policy --role-name $role --policy-name $role --policy-document file://edit/$f
  echo "Creating role $role end"
done
echo "Setting identity pool roles begin..."
roles='{"unauthenticated":"arn:aws:iam::'"$AWS_ACCOUNT_ID"':role/'"$unauthRole"'","authenticated":"arn:aws:iam::'"$AWS_ACCOUNT_ID"':role/'"$authRole"'"}'
echo "Roles: $roles"
aws cognito-identity set-identity-pool-roles \
  --identity-pool-id $IDENTITY_POOL_ID \
  --roles $roles \
  --region $REGION
echo "Setting identity pool roles end"

# Create IAM Roles for Lambda Function
for f in $(ls -1 sampleAuth*); do
  role="${f%.*}"
  echo "Creating role $role from $f begin..."
  sed -e "s/<AWS_ACCOUNT_ID>/$AWS_ACCOUNT_ID/g" \
      -e "s/<DYNAMODB_TABLE>/$DDB_TABLE/g" \
      -e "s/<DYNAMODB_EMAIL_INDEX>/$DDB_EMAIL_INDEX/g" \
      -e "s/<IDENTITY_POOL_ID>/$IDENTITY_POOL_ID/g" \
      -e "s/<REGION>/$REGION/g" \
      $f > edit/$f
	trust="Policy_Trust_Lambda.json"
  aws iam create-role --role-name $role --assume-role-policy-document file://edit/$trust
  aws iam update-assume-role-policy --role-name $role --policy-document file://edit/$trust
  aws iam put-role-policy --role-name $role --policy-name $role --policy-document file://edit/$f
  echo "Creating role $role end"
done

cd ..

cd fn

# Create Lambda Functions
for f in $(ls -1); do
  echo "Creating function $f begin..."
  cp ../config.json $f/
  cp -R ../lib $f/
  cd $f
  zip -r $f.zip index.js config.json lib/
  aws lambda create-function --function-name ${f} \
      --runtime nodejs4.3 \
      --role arn:aws:iam::"$AWS_ACCOUNT_ID":role/${f} \
      --handler index.handler \
      --zip-file fileb://${f}.zip \
	  	--region $REGION
	sleep 1 # To avoid errors
  cd ..
  echo "Creating function $f end"
done

cd ..

./deploy.sh
