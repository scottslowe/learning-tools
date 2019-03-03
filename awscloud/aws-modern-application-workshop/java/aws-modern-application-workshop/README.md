# Build a Modern Application on AWS (Java)

![mysfits-welcome](/images/module-1/mysfits-welcome.png)

**Welcome to the **Java** version of the Build a Modern Application on AWS Workshop!**

**Time to Complete:** 3-4 hours

**! Attention !**

* No AWS experience is required for this tutorial, we'll go step-by-step!
* This workshop is designed to run at minimal cost (less than $1/day), and mostly covered by the [AWS Free Tier](https://aws.amazon.com/free).
*Assuming little to no traffic will be served by your demo website created as part of this workshop.*
* After you complete the workshop, make sure you delete all AWS resrouces to avoid further costs, as described below in this page.
* This workshop requires Administrator level access to an AWS account. Open your account now, new accounts have extra benefits in the first 12 months of the [AWS Free Tier](https://aws.amazon.com/free/)
* Use an AWS account dedicated to development and education, without access to production systems or data, to avoid security issues. 

### Application Architecture

![Application Architecture](/images/arch-diagram.png)

The initial Mythical Mysfits architecture features:
* A static website served static directly from [Amazon S3](https://aws.amazon.com/s3)
* A backend serverless API, published securely worldwide by [AWS API Gateway](https://aws.amazon.com/api-gateway/) 
* Application architecture following "microservices" patterns, deployed as a container to [AWS Fargate](https://aws.amazon.com/fargate/)
* A low-latency NoSQL data store, [Amazon DynamoDB](https://aws.amazon.com/dynamodb/)
* Authentication, authorization and secure data sync with [Amazon Cognito](https://aws.amazon.com/fargate/)
* Click record streams are ingested at scale by by [Amazon Kinesis Firehose](https://aws.amazon.com/kinesis/data-firehose/). From there records are processed by serverless AWS Lambda functions and then stored in Amazon S3.

In this workshope we are going to use the [AWS Command Line Interface](https://aws.amazon.com/cli/) to create resources, instead of the [AWS Console](https://aws.amazon.com/console/) that you may be more familiar. This allows all changes to be automatically pushed to production and continuously delivering value to customers. This workshop includes a fully managed build and deployment pipeline utilizing [AWS CodeCommit](https://aws.amazon.com/codecommit/), [AWS CodeBuild](https://aws.amazon.com/codebuild/), and [AWS CodePipeline](https://aws.amazon.com/codepipeline/).  There is no specific software requirements, any modern browser should work, leveraging the cloud-based IDE, [AWS Cloud9](https://aws.amazon.com/cloud9/).
 

## Before you begin
If this is your first time using AWS, also visit our [Getting Started](https://aws.amazon.com/getting-started/) page and get familiar with using your AWS account.

When you are redy, begin the workshop in the [Module 1: IDE Setup and Static Website Hosting](/module-1)


## After you complete
Be sure to delete all of the resources created during the workshop in order to ensure that billing for the resources does not continue for longer than you intend.  We recommend that you utilize the [AWS Console](https://aws.amazon.com/console/) to explore the resources you've created and delete them when you're ready.  

For the two cases where you provisioned resources using AWS CloudFormation, you can remove all resources at once by deleting the given stack with the following command:

```
aws cloudformation delete-stack --stack-name STACK-NAME-HERE
```

To explore and remove the resources created individually, visit the the AWS Console for services used during the Mythical Mysfits workshop:
* [AWS Kinesis](https://console.aws.amazon.com/kinesis/home)
* [AWS Lambda](https://console.aws.amazon.com/lambda/home)
* [Amazon S3](https://console.aws.amazon.com/s3/home)
* [Amazon API Gateway](https://console.aws.amazon.com/apigateway/home)
* [Amazon Cognito](https://console.aws.amazon.com/cognito/home)
* [AWS CodePipeline](https://console.aws.amazon.com/codepipeline/home)
* [AWS CodeBuild](https://console.aws.amazon.com/codebuild/home)
* [AWS CodeCommit](https://console.aws.amazon.com/codecommit/home)
* [Amazon DynamoDB](https://console.aws.amazon.com/dynamodb/home)
* [Amazon ECS](https://console.aws.amazon.com/ecs/home)
* [Amazon EC2](https://console.aws.amazon.com/ec2/home)
* [Amazon VPC](https://console.aws.amazon.com/vpc/home)
* [AWS IAM](https://console.aws.amazon.com/iam/home)
* [AWS CloudFormation](https://console.aws.amazon.com/cloudformation/home)


Redy to start again? Go to [Module 1: IDE Setup and Static Website Hosting](/module-1)


[AWS Developer Center](https://developer.aws)
