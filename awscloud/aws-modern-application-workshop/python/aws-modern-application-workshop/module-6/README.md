# Module 6: Tracing Application Requests

![Architecture](/images/module-6/x-ray-arch-diagram.png)

**Time to complete:** 45 minutes

**Services used:**
* [AWS CloudFormation](https://aws.amazon.com/cloudformation/)
* [AWS X-Ray](https://aws.amazon.com/x-ray/)
* [Amazon DynamoDB](https://aws.amazon.com/dynamodb/)
* [Amazon Simple Notification Service (AWS SNS)](https://aws.amazon.com/sns/)
* [Amazon S3](https://aws.amazon.com/s3/)
* [Amazon API Gateway](https://aws.amazon.com/api-gateway/)
* [AWS Lambda](https://aws.amazon.com/lambda/)
* [AWS CodeCommit](https://aws.amazon.com/codecommit/)
* [AWS Serverless Appliation Model (AWS SAM)](https://github.com/awslabs/serverless-application-model)
* [AWS SAM Command Line Interface (SAM CLI)](https://github.com/awslabs/aws-sam-cli)

### Overview
Next, we will show you how to deeply inspect and analyze request behavior on new functionality for the Mythical Mysfits site, using [**AWS X-Ray**](https://aws.amazon.com/kinesis/data-firehose/).  The new functionality will enable users to contact the Mythical Mysfits staff, via a **Contact Us** button we'll place on the site.  Since much of the steps required to create a new microservice to handle receiving user questions mimics activities you've performed earlier in this workshop, we have provided a CloudFormation template that will programmatically create the new service using AWS SAM.

The CloudFormation template includes:
* An **API Gateway API**:  A new microservice will be created that has a single REST resource, `/questions`.  This API will receive the text of a user question and the email address for the user who submitted it.
* A **DynamoDB Table**: A new DynamoDB table where the user questions will be stored and persisted.  This DynamoDB table will be created with a [**DynamoDB Stream**](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Streams.html) enabled.  The stream will provide a real-time event stream for all of the new questions that are stored within the database so that they can be immediately processed.
* An **AWS SNS Topic**: AWS SNS allows applications to publish messages and to subscribe to message topics.  We will use a new topic as a way to send notifications to a subscribed email address for a email address.
* Two **AWS Lambda Functions**: One AWS Lambda function will be used as the service backend for the question API requests. The other AWS Lambda function will receive events from the questions DynamoDB table and publish a message for each of them to the above SNS topic.  If you view the CloudFormation resource definitions for these functions in the file `~/environment/aws-modern-application-workshop/module-6/app/cfn/customer-question.yml`, you'll see a Property listed that indicates `Tracing: Active`.  This means that all invocations of the Lambda function will automatically be traced by **AWS X-Ray**.
* **IAM Roles** required for each of the above resources and actions.

Bring your command line terminal back to your root Cloud9 environment directory so that our subsequent commands are executed from the same place:

```
cd ~/environment/
```

First, let's create another new **AWS CodeCommit** repository for the new microservice:

```
aws codecommit create-repository --repository-name MythicalMysfitsQuestionsService-Repository
```

Next, clone the new repository into your IDE using the `cloneUrlHttp` value taken from the response to the above `create-repository` command you just ran:

```
git clone REPLACE_ME_WITH_ABOVE_CLONE_URL
```

Then move your terminal to the new repository directory:
```
cd ~/environment/MythicalMysfitsQuestionsService-Repository
```

Now, copy the new QuestionsService application code into the repository directory, followed by the CloudFormation Template necessary to deploy the infrastructure required by the QuestionsService:

```
cp -r ~/environment/aws-modern-application-workshop/module-6/app/* .
```

```
 cp -r ~/environment/aws-modern-application-workshop/module-6/cfn/* .
```

For this new microservice, we have included all of the packages necessary for the AWS Lambda functions to be deployed and invoked. Before deploying it, you are required to create another S3 bucket to be used by AWS SAM as a destination for your packaged QuestionService code (remember all S3 bucket names need to be unique and have naming constraints):

```
aws s3 mb s3://REPLACE_ME_NEW_QUESTIONS_SERVICE_CODE_BUCKET_NAME
```
 and execute the following command to transform the SAM template into CloudFormation...

```
sam package --template-file ~/environment/MythicalMysfitsQuestionsService-Repository/customer-questions.yml --output-template-file ~/environment/MythicalMysfitsQuestionsService-Repository/transformed-questions.yml --s3-bucket REPLACE_ME_NEW_QUESTIONS_SERVICE_CODE_BUCKET_NAME
```

...and then deploy the CloudFormation stack. **Note: provide an email address you have access to as the REPLACE_ME_EMAIL_ADDRESS parameter (replace it prior to pasting this command, the stack creation will fail if you execute the command without providing a valid email address). This will be the email address that user questions are published to by the SNS topic**:

```
aws cloudformation deploy --template-file /home/ec2-user/environment/MythicalMysfitsQuestionsService-Repository/transformed-questions.yml --stack-name MythicalMysfitsQuestionsService-Stack --capabilities CAPABILITY_IAM --parameter-overrides AdministratorEmailAddress=REPLACE_ME_YOUR_EMAIL_ADDRESS
```

When this command completes, let's capture the output of the stack so that we can reference its values in subsequent steps (will create a file in your IDE called `questions-service-output.json`):

```
aws cloudformation describe-stacks --stack-name MythicalMysfitsQuestionsService-Stack > ~/environment/questions-service-output.json
```

Next, visit the email address provided and CONFIRM your subscription to the SNS topic:
![SNS Confirm](/images/module-6/confirm-sns.png)


Now, with the new backend service running, let's make the required changes to `index.html` so that the frontend can include the new *Contact Us* button.  Open `~/environment/aws-modern-application-workshop/module-6/web/index.html`  and insert the API endpoint for the new Questions API, retrieve the output value of `REPLACE_ME_QUESTIONS_API_ENDPOINT` from the above CloudFormation stack (located at `~/environment/questions-service-output.json`).  **Remember that you'll also need to paste the same values that you previously used prior to this module for the other Mysfits microservices endpoints and user pool.**



Once you've made the necessary change to `index.html` run the following command to copy it to your website S3 bucket.

```
aws s3 cp ~/environment/aws-modern-application-workshop/module-6/web/index.html s3://YOUR-S3-BUCKET/
```

Now that the new Contact Us functionality is deployed, visit the website and submit a question or two.  If you've confirmed the subscription to SNS in the step above, you'll start to see those questions arrive in your inbox! When you've seen that email arrive, you can move on to explore and analyze the request lifecycle.

Now, to start seeing the request behavior for this microservice, visit the AWS X-Ray console to explore:

[AWS X-Ray Console](https://console.aws.amazon.com/xray/home)

Upon visiting the X-Ray Console you'll be immediately viewing a **service map**, which shows the dependency relationship between all the components that X-Ray receives **trace segments** for:  

![X-Ray Lambda Only](/images/module-6/lambda-only-x-ray.png)

At first, this service map only includes our AWS Lambda functions.  Feel free to explore the X-Ray console to learn more about drilling into the data automatically made visible just by listing the `Tracing: Active` property in the CloudFormation template you deployed.

Next, we're going to instrument more of the microservice stack so that all of the service dependencies are included in the service map and recorded trace segments.

First, we will instrument the API Gateway REST API.  Issue the following command inserting the value for ``REPLACE_ME_QUESTIONS_REST_API_ID`` that is located in the ``questions-service-output.json`` file created but the most recent CloudFormation `descrbe-stacks` command you just ran.  The below command will enable tracing to start at the API Gateway level of the service stack:

```
aws apigateway update-stage --rest-api-id REPLACE_ME_QUESTIONS_REST_API_ID --stage-name prod --patch-operations op=replace,path=/tracingEnabled,value=true
```

Now, submit another question to the Mythical Mysfits website and you'll see that the REST API is also included in the service map!

![API Gateway Traced](/images/module-6/api-x-ray.png)

Next, you will use the [AWS X-Ray SDK for Python](https://docs.aws.amazon.com/xray/latest/devguide/xray-sdk-python.html) so that the services being called by the two Lambda functions as part of the questions stack are also represented in the X-Ray service map.  The code has been written already to accomplish this, you just need to uncomment the relevant lines (uncommenting is performed by deleting the preceding `#` in a line of python code).  In the Lambda function code, you will see comments that indicate `#UNCOMMENT_BEFORE_2ND_DEPLOYMENT` or `#UNCOMMENT_BEFORE_3RD_DEPLOYMENT`.  

You've already completed the first deployment of these functions using CloudFormation, so this will be your **2nd Deployment**.  Uncomment each of the lines indicated below all cases of `UNCOMMENT_BEFORE_2ND_DEPLOYMENT` in the following files, and save the files after making the required changes:
* `~/environment/MythicalMysfitsQuestionsStack-Repository/PostQuestionsService/mysfitsPostQuestion.py`
* `~/environment/MythicalMysfitsQuestionsStack-Repository/ProcessQuestionsStream/mysfitsProcessStream.py`

**Note: The changes you've uncommented enable the AWS X-Ray SDK to instrument the AWS Python SDK (boto3) to capture tracing data and record it within the Lambda service anytime an AWS API call is made. Those few lines of code are all that's required in order for X-Ray to automatically trace your AWS service map throughout a serverless application using AWS Lambda!**

With those changes made, deploy an update to the Lambda function code by issuing the following two commands:

First, use SAM to create new Lambda function code bundles and upload the packaged code to S3:
```
sam package --template-file ~/environment/MythicalMysfitsQuestionsService-Repository/customer-questions.yml --output-template-file ~/environment/MythicalMysfitsQuestionsService-Repository/transformed-questions.yml --s3-bucket REPLACE_ME_NEW_QUESTIONS_SERVICE_CODE_BUCKET_NAME
```

Second, use CloudFormation to deploy the changes to the running stack:

```
aws cloudformation deploy --template-file /home/ec2-user/environment/MythicalMysfitsQuestionsService-Repository/transformed-questions.yml --stack-name MythicalMysfitsQuestionsService-Stack --capabilities CAPABILITY_IAM --parameter-overrides AdministratorEmailAddress=REPLACE_ME_YOUR_EMAIL_ADDRESS
```

Once that command completes, submit an additional question to the Mythical Mysfits website and take a look at the X-Ray console again. Now you're able to trace how Lambda is interacting with DynamoDB as well as SNS!

![Services X-Ray](/images/module-6/services-x-ray.png)

The final step in this module is to familiarize yourself with using AWS X-Ray to triage problems in your application.  To accomplish this, we're going to by *mysfits* ourselves and have you add some terrible code to your application.  All this code will do is cause your web service to add 5 seconds of latency and throw an exception for randomized requests :) .

Go back into the following file and remove the comments indicated by `#UNCOMMENT_BEFORE_3RD_DEPLOYMENT`:  
* `~/environment/MythicalMysfitsQuestionsStack-Repository/PostQuestionsService/mysfitsPostQuestion.py`

This is the code that will cause your Lambda function to throw an exception.  Also, you can note above the `hangingException()` function that we're using out-of-the-box functionality of the **AWS X-Ray SDK** to record a trace subsegment each time that function is called.  Now when you drill into the Trace for a particular request, you'll be able to see that all requests are stuck sitting within this function for at least 5 seconds before they throw the exception.

Using this functionality within your own applications will help you identify similar latency bottlenecks within your code, or places where exceptions are being thrown.

After you make the required code changes and save the `mysfitsPostQuestion.py` file, run the same two commands as before to package and deploy your changes to CloudFormation.  **Use the UP arrow key within your Cloud9 terminal to see past commands and first execute the `sam package` command in your history, and then the `aws cloudformation deploy` command subsequently.**

After you've issued those two commands, submit another few questions on your Mysfits website.  Some of these questions will fail to show up in your inbox. Because your new and terrible code has thrown an error!

If you visit the X-Ray console again, you'll notice that the service map for the MysfitPostQuestionsFunction Lambda function has a ring around it that is no longer only Green. That's because Error responses have been generated there.  X-Ray will give you this visual representation of overall service health across all of the instrumented services in your service map:

![X-Ray Errors](/images/module-6/x-ray-errors.png)

If you click on that service within the service map, you'll notice on the right side of the X-Ray console, you have the ability to view the traces that either match the highlighted overall latency shown within the service latency graph and/or the status code you're interested in.  Zoom the latency graph so that the blip around 5 seconds is within the graph and/or select the Error check box and click **View traces**:

![View Traces](/images/module-6/view-traces.png)

This will take you to the Trace dashboard where you can explore that specific requests lifecycle, see the latency spend on each segment of the service, and view the exception reported and associated stack trace. Click on any of the IDs for the Traces where the response is reported as a 502, then on the subsequent **Trace Details** page, click on the **hangingException** to view that specific subsegment where the exception was thrown in our code:

![Exception](/images/module-6/exception.png)

Congratulations, you've completed module 6!

### [Proceed to Module 7](/module-7)


#### [AWS Developer Center](https://developer.aws)
