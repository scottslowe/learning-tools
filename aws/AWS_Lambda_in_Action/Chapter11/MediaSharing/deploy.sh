cd fn
for FN in $(ls -1 .)
do
  echo "Deploying function $FN ..."
  cd $FN
  zip -r ../$FN.zip .
  cd ..
  aws lambda update-function-code --function-name $FN --zip-file fileb://$FN.zip --region us-east-1
  rm $FN.zip
  echo "Done!"
done
cd ..
cd www
aws s3 sync . s3://danilop-media-sharing
cd ..
