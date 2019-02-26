awslambda = boto3.client('lambda')

response = awslambda.update_function_code(
    FunctionName='anotherGreetingsOnDemand',
    ZipFile=b'bytes',
    S3Bucket='danilop-functions',
    S3Key='code/greetingsOnDemand-v2.zip',
    Publish=True
)
