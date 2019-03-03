# Module 5: Capturing User Behavior

![Architecture](/images/module-5/architecture-module-5.png)

**Time to complete:** 30 minutes

**Services used:**
* [AWS CloudFormation](https://aws.amazon.com/cloudformation/)
* [AWS Kinesis Data Firehose](https://aws.amazon.com/kinesis/data-firehose/)
* [Amazon S3](https://aws.amazon.com/s3/)
* [Amazon API Gateway](https://aws.amazon.com/api-gateway/)
* [AWS Lambda](https://aws.amazon.com/lambda/)
* [AWS CodeCommit](https://aws.amazon.com/codecommit/)
* [AWS Serverless Appliation Model (AWS SAM)](https://github.com/awslabs/serverless-application-model)
* [AWS SAM Command Line Interface (SAM CLI)](https://github.com/awslabs/aws-sam-cli)

### Overview
Now that your Mythical Mysfits site is up and running, let's create a way to better understand how users are interacting with the website and its Mysfits.  It would be very easy for us to analyze user actions taken on the website that lead to data changes in our backend - when mysfits are adopted or liked.  But understanding the actions your users are taking on the website *before* a decision to like or adopt a mysfit could help you design a better user experience in the future that leads to mysfits getting adopted even faster.  To help us gather these insights, we will implement the ability for the website frontend to submit a tiny request, each time a mysfit profile is clicked by a user, to a new microservice API we'll create. Those records will be processed in real-time by a serverless code function, aggregated, and stored for any future analysis that you may want to perform.

Modern application design principles prefer focused, decoupled, and modular services.  So rather than add additional methods and capabilities within the existing Mysfits service that you have been working with so far, we will create a new and decoupled service for the purpose of receiving user click events from the Mysfits website.  This full stack has been represented using a provided CloudFormation template.

The serverless real-time processing service stack you are creating includes the following AWS resources:
* An [**AWS Kinesis Data Firehose delivery stream**](https://aws.amazon.com/kinesis/data-firehose/): Kinesis Firehose is a highly available and managed real-time streaming service that accepts data records and automatically ingests them into several possible storage destinations within AWS, examples including an Amazon S3 bucket, or an Amazon Redshift data warehouse cluster. Kinesis Firehose also enables all of the records received by the stream to be automatically delivered to a serverless function created with **AWS Lambda** This means that code you've written can perform any additional processing or transformations of the records before they are aggregated and stored in the configured destination.
* An [**Amazon S3 bucket**](https://aws.amazon.com/s3/): A new bucket will be created in S3 where all of the processed click event records are aggregated into files and stored as objects.
* An [**AWS Lambda function**](https://aws.amazon.com/lambda/): AWS Lambda enables developers to write code functions that only contain what their logic requires and have their code be deployed, invoked, made highly reliable, and scale without having to manage infrastructure whatsoever. Here, a Serverless code function is defined using AWS SAM. It will be deployed to AWS Lambda, written in Python, and then process and enrich the click records that are received by the delivery stream.  The code we've written is very simple and the enriching it does could have been accomplished on the website frontend without any subsequent processing  at all.  The function retrieves additional attributes about the clicked on Mysfit to make the click record more meaningful (data that was already retrieved by the website frontend).  But, for the purpose of this workshop, the code is meant to demonstrate the architectural possibilities of including a serverless code function to perform any additional processing or transformation required, in real-time, before records are stored.  Once the Lambda function is created and the Kinesis Firehose delivery stream is configured as an event source for the function, the delivery stream will automatically deliver click records as events to code function we've created, receive the responses that our code returns, and deliver the updated records to the configured Amazon S3 bucket.
* An [**Amazon API Gateway REST API**](https://aws.amazon.com/api-gateway/): AWS Kinesis Firehose provides a service API just like other AWS services, and in this case we are using its PutRecord operation to put user click event records into the delivery stream. But, we don't want our website frontend to have to directly integrate with the Kinesis Firehose PutRecord API.  Doing so would require us to manage AWS credentials within our frontend code to authorize those API requests to the PutRecord API, and it would expose to users the direct AWS API that is being depended on (which may encourage malicious site visitors to attempt to add records to the delivery stream that are malformed, or harmful to our goal of understanding real user behavior).  So instead, we will use Amazon API Gateway to create an **AWS Service Proxy** to the PutRecord API of Kinesis Firehose.  This allows us to craft our own public RESTful endpoint that does not require AWS credential management on the frontend for requests. Also, we will use a request **mapping template** in API Gateway as well, which will let us define our own request payload structure that will restrict requests to our expected structure and then transform those well-formed requests into the structure that the Kinesis Firehose PutRecord API requires.
* [**IAM Roles**](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_roles.html): Kinesis Firehose requires a service role that allows it to deliver received records as events to the created Lambda function as well as the processed records to the destination S3 bucket. The Amazon API Gateway API also requires a new role that permits the API to invoke the PutRecord API within Kinesis Firehose for each received API request.

Before we launch the CloudFormaiton template described above, we need to update and modify the Lambda function code it will deploy.

### Copy the Streaming Service Code

#### Create a new CodeCommit Repository

This new stack you will deploy using CloudFormation will not only contain the infrastructure environment resources, but the application code itself that AWS Lambda will execute to process streaming events.  To bundle the creation of our infrastructure and code together in one deployment, we are going to use another AWS tool that comes pre-installed in the AWS Cloud9 IDE -  **AWS SAM CLI**.  Code for AWS Lambda functions is delivered to the service by uploading the function code in a .zip package to an Amazon S3 bucket.  The SAM CLI automates that process for us.  Using it, we can create a CloudFormation template that references locally in the filesystem where all of the code for our Lambda function is stored.  Then, the SAM CLI will package it into a .zip file, upload it to a configured Amazon S3 bucket, and create a new CloudFormation template that indicates the location in S3 where the created .zip package has been uploaded for deployment to AWS Lambda.  We can then deploy that SAM CLI-generated CloudFormation template to AWS and watch the environment be created along with the Lambda function that uses the SAM CLI-uploaded code package.  

First, let's create a new CodeCommit repository where the streaming service code will live:
```
aws codecommit create-repository --repository-name MythicalMysfitsStreamingService-Repository
```

In the response to that command, copy the value for `"cloneUrlHttp"`.  It should be of the form:
`https://git-codecommit.REPLACE_ME_REGION.amazonaws.com/v1/repos/MythicalMysfitsStreamingService-Repository`

Next, let's clone that new and empty repository into our IDE:
```
cd ~/environment/
```

```
git clone REPLACE_ME_WITH_ABOVE_CLONE_URL
```

#### Copy the Streaming Service Code Base

Now, let's move our working directory into this new repository:
```
cd ~/environment/MythicalMysfitsStreamingService-Repository/
```

Then, copy the module-5 application components into this new repository directory:
```
cp -r ~/environment/aws-modern-application-workshop/module-5/app/streaming/* .
```

And let's copy the CloudFormation template for this module as well.

```
cp ~/environment/aws-modern-application-workshop/module-5/cfn/* .
```

### Update the Lambda Function Package and Code

#### Use pip to Intall Lambda Function Dependencies
Now, we have the repository directory set with all of the provided artifacts:
* A CFN template for creating the full stack.
* A Python file that contains the code for our Lambda function: `streamProcessor.py`

This is a common approach that AWS customers take - to store their CloudFormation templates alongside their application code in a repository. That way, you have a single place where all changes to application and it's environment can be tracked together.

But, if you look at the code inside the `streamProcessor.py` file, you'll notice that it's using the `requests` Python package to make an API requset to the Mythical Mysfits service you created previously.  External libraries are not automatically included in the AWS Lambda runtime environment, because different AWS customers may depend on different versions of various libraries, etc.  You will need to package all of your library dependencies together with your Lambda code function prior to it being uploaded to the Lambda service.  We will use the Python package manager `pip` to accomplish this.  In the Cloud9 terminal, run the following command to install the `requests` package and it's dependencies locally alongside your function code:

```
pip install requests -t .
```

Once this command completes, you will see several additional python package folders stored within your repository directory.  

#### Update the Lambda Function Code
Next, we have one code change to make prior to our Lambda function code being completely ready for deployment.  There is a line within the `streamProcessor.py` file that needs to be replaced with the ApiEndpoint for your Mysfits service API - the same service ApiEndpoint that you created in module-4 and used on the website frontend.  Be sure to update the file you have copied into the new StreamingService repository directory.

![replace me](/images/module-5/replace-api-endpoint.png)

That service is responsible for integrating with the MysfitsTable in DynamoDB, so even though we could write a Lambda function that directly integrated with the DynamoDB table as well, doing so would intrude upon the purpose of the first microservice and leave us with multiple/separate code bases that integrated with the same table.  Instead, we will integrate with that table through the existing service and have a much more decoupled and modular application architecture.

#### Push Your Code into CodeCommit
Let's commit our code changes to the new repository so that they're saved in CodeCommit:

```
git add .
```

```
git commit -m "New stream processing service."
```

```
git push
```

### Creating the Streaming Service Stack


#### Create an S3 Bucket for Lambda Function Code Packages
With that line changed in the Python file, and our code committed, we are ready to use the AWS SAM CLI to package all of our function code, upload it to S3, and create the deployable CloudFormation template to create our streaming stack.

First, use the AWS CLI to create a new S3 bucket where our Lambda function code packages will be uploaded to.  S3 bucket names need to be globally unique among all AWS customers, so replace the end of this bucket name with a string that's unique to you:

```
aws s3 mb s3://REPLACE_ME_YOUR_BUCKET_NAME/
```

#### Use the SAM CLI to Package your Code for Lambda

With our bucket created, we are ready to use the SAM CLI to package and upload our code and transform the CloudFormation template, be sure to replace the last command parameter with the bucket name you just created above (this command also assumes your terminal is still in the repository working directory):

```
sam package --template-file ./real-time-streaming.yml --output-template-file ./transformed-streaming.yml --s3-bucket REPLACE_ME_YOUR_BUCKET_NAME
```

If successful, you will see the newly created `transformed-streaming.yml` file exist within the `./MythicalMysfitsStreamingService-Repository/` directory, if you look in its contents, you'll see that the CodeUri parameter of the serverless Lambda function has been updated with the object location where the SAM CLI has uploaded your packaged code.

#### Deploy the Stack using AWS CloudFormation

Also returned by the SAM CLI command is the CloudFormation command needed to be executed to create our new full stack.  But because our stack creates IAM resources, you'll need to add one additional parameter to the command.  Execute the following command to deploy the streaming stack:

```
aws cloudformation deploy --template-file /home/ec2-user/environment/MythicalMysfitsStreamingService-Repository/transformed-streaming.yml --stack-name MythicalMysfitsStreamingStack --capabilities CAPABILITY_IAM
```

Once this stack creation is complete, the full real-time processing microservice will be created.  

In future scenarios where only code changes have been made to your Lambda function, and the rest of your CloudFormation stack remains unchanged, you can repeat the same AWS SAM CLI and CloudFormation commands as above. This will result in the infrastructure environment remaining unchanged, but a code deployment occurring to your Lambda function.

### Sending Mysfit Profile Clicks to the Service

#### Update the Website Content
With the streaming stack up and running, we now need to publish a new version of our Mythical Mysfits frontend that includes the JavaScript that sends events to our service whenever a mysfit profile is clicked by a user.

The new index.html file is included at: `~/environment/aws-modern-application-workshop/module-5/web/index.html`

This file contains the same placeholders as module-4 that need to be updated, as well as an additional placeholder for the new stream processing service endpoint you just created.  For the previous variable values, you can refer to the previous `index.html` file you updated as part of module-4.

Perform the following command for the new streaming stack to retrieve the new API Gateway endpoint for your stream processing service:

```
aws cloudformation describe-stacks --stack-name MythicalMysfitsStreamingStack
```

#### Push the New Site Version to S3
Replace the final value within `index.html` with the streamingApiEndpoint and you are ready to publish your final Mythical Mysfits home page update:

```
aws s3 cp ~/environment/aws-modern-application-workshop/module-5/web/index.html s3://YOUR-S3-BUCKET/
```

Refresh your Mythical Mysfits website in the browser once more and you will now have a site that records and publishes each time a user clicks on a mysfits profile!

To view the records that have been processed, they will arrive in the destination S3 bucket created as part of your MythicalMysfitsStreamingStack.  Visit the S3 console here and explore the bucket you created for the streaming records (it will be prefixed with `mythicalmysfitsstreamings-clicksdestinationbucket`):
[Amazon S3 Console](https://s3.console.aws.amazon.com/s3/home)

This concludes Module 5.

### [Proceed to Module 6](/module-6)


#### [AWS Developer Center](https://developer.aws)
