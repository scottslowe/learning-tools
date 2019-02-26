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
REGION=$(jq -r '.REGION' config.json)
CLI_PROFILE=$(jq -er '.CLI_PROFILE' config.json)
# Get jq return code set by the -e option
CLI_PROFILE_RC=$?
BUCKET=$(jq -r '.BUCKET' config.json)
MAX_AGE=$(jq -r '.MAX_AGE' config.json)
IDENTITY_POOL_ID=$(jq -r '.IDENTITY_POOL_ID' config.json)
DEVELOPER_PROVIDER_NAME=$(jq -r '.DEVELOPER_PROVIDER_NAME' config.json)

#if a CLI Profile name is provided... use it.
if [[ $CLI_PROFILE_RC == 0 ]]; then
  echo "Setting session CLI profile to $CLI_PROFILE"
  export AWS_DEFAULT_PROFILE=$CLI_PROFILE
fi

echo "Updating Lambda functions..."

cd fn

# Updating Lambda Functions
for f in $(ls -1); do
  echo "Updating function $f begin..."
  cp ../config.json $f/
  cp -R ../lib $f/
  cd $f
  zip -r $f.zip index.js config.json lib/
  aws lambda update-function-code --function-name ${f} \
      --zip-file fileb://${f}.zip \
	  	--region $REGION
  cd ..
  echo "Updating function $f end"
done

cd ..

echo "Updating www content begin..."

cd www
if [ -d "edit" ]; then
  rm -r edit/*
else
  mkdir edit
fi
mkdir edit/js

for f in $(ls -1 *.* js/*.*); do
  echo "Updating $f begin..."
  sed -e "s/<REGION>/$REGION/g" \
      -e "s/<IDENTITY_POOL_ID>/$IDENTITY_POOL_ID/g" \
      -e "s/<DEVELOPER_PROVIDER_NAME>/$DEVELOPER_PROVIDER_NAME/g" \
      $f > edit/$f
  echo "Updating $f end"
done
echo "Updating www content end"
echo "Sync www content with S3 bucket $BUCKET begin..."
cd edit
aws s3 sync . s3://$BUCKET --cache-control max-age="$MAX_AGE" --acl public-read
cd ../..
echo "Sync www content with S3 bucket $BUCKET end"
