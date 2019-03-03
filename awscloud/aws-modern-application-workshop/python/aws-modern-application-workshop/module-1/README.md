# Module 1: IDE Setup and Static Website Hosting

![Architecture](/images/module-1/architecture-module-1.png)

**Time to complete:** 20 minutes

**Services used:**
* [AWS Cloud9](https://aws.amazon.com/cloud9/)
* [Amazon CloudFront](https://aws.amazon.com/cloudfront/)
* [Amazon Simple Storage Service (S3)](https://aws.amazon.com/s3/)

In this module, follow the instructions to create your cloud-based IDE on [AWS Cloud9](https://aws.amazon.com/cloud9/) and deploy the first version of the static Mythical Mysfits website. The content will be served from Amazon CloudFront content delivery service, with the source stored in [Amazon S3](https://aws.amazon.com/s3/). This combination allows highly scalable and secure serving in CloudFront, while S3 storage provides a highly durable, highly available, and inexpensive object storage service. This makes it wonderfully useful for serving static web content (html, js, css, media content, etc.) directly to web browsers for sites on the Internet.

### Getting Started

#### Sign In to the AWS Console
To begin, sign in to the [AWS Console](https://console.aws.amazon.com) for the AWS account you will be using in this workshop.

This web application can be deployed in any AWS region that supports all the services used in this application. The supported regions include:

* us-east-1 (N. Virginia)
* us-east-2 (Ohio)
* us-west-2 (Oregon)
* eu-west-1 (Ireland)

Select a region from the dropdown in the upper right corner of the AWS Management Console.

### Creating your Mythical Mysifts IDE

#### Create a new AWS Cloud9 Environment

 On the AWS Console home page, type **Cloud9** into the service search bar and select it:
 ![aws-console-home](/images/module-1/cloud9-service.png)


Click **Create Environment** on the Cloud9 home page:
![cloud9-home](/images/module-1/cloud9-home.png)


Name your environment **MythicalMysfitsIDE** with any description you'd like, and click **Next Step**:
![cloud9-name](/images/module-1/cloud9-name-ide.png)


Leave the Environment settings as their defaults and click **Next Step**:
![cloud9-configure](/images/module-1/cloud9-configure-env.png)


Click **Create Environment**:
![cloud9-review](/images/module-1/cloud9-review.png)


When the IDE has finished being created for you, you'll be presented with a welcome screen that looks like this:
![cloud9-welcome](/images/module-1/cloud9-welcome.png)

#### Cloning the Mythical Mysfits Workshop Repository

In the bottom panel of your new Cloud9 IDE, you will see a terminal command line terminal open and ready to use.  Run the following git command in the terminal to clone the necessary code to complete this tutorial:

```
git clone -b python https://github.com/aws-samples/aws-modern-application-workshop.git
```

After cloning the repository, you'll see that your project explorer now includes the files cloned:
![cloud9-explorer](/images/module-1/cloud9-explorer.png)


In the terminal, change directory to the newly cloned repository directory:

```
cd aws-modern-application-workshop
```

### Creating a Website with Amazon CloudFront and Amazon S3

#### Create an S3 Bucket for Storing Content
Next, we will create the infrastructure components needed for storing static website in Amazon S3 via the ![AWS CLI](https://aws.amazon.com/cli/). If you do not already have the AWS CLI configured see ![getting started](http://docs.aws.amazon.com/cli/latest/userguide/)

First, create an S3 bucket, replace the name below (REPLACE_ME_BUCKET_NAME) with your own unique bucket name.  Copy the name you choose and save it for later, as you will use it in several other places during this workshop:

```
aws s3 mb s3://REPLACE_ME_BUCKET_NAME
```

#### Update the S3 Bucket Policy

All buckets created in Amazon S3 are fully private by default. In order to be used as a website accessible only through CloudFront, we need to create an S3 [Bucket Policy](https://docs.aws.amazon.com/AmazonS3/latest/dev/example-bucket-policies.html) that indicates objects stored within this new bucket may be accessed by only your CloudFront origin access identity that you create. 

The JSON document for the necessary bucket policy is located at: `~/environment/aws-modern-application-workshop/module-1/aws-cli/website-bucket-policy.json`.  This file contains two strings that need to be replaced, the Id (indicated with `REPLACE_ME_CLOUDFRONT_ORIGIN_ACCESS_IDENTITY_ID`), and the bucket name you've chosen (indicated with `REPLACE_ME_BUCKET_NAME`). 

First we need to create a CloudFront Access Identity using the following CLI command.

```
aws cloudfront create-cloud-front-origin-access-identity --cloud-front-origin-access-identity-config CallerReference=Mysfits,Comment=Mysfits
```
Example Output: 
```
{
    "CloudFrontOriginAccessIdentity": {
        "CloudFrontOriginAccessIdentityConfig": {
            "Comment": "Mysfits", 
            "CallerReference": "Mysfits"
        }, 
        "S3CanonicalUserId": "ABCABCCCAfICCQD6m7oRw0uXOjANBgkqhkiG9w0BAQUFADCBiDELMAkGA1EXAMPLE", 
        "Id": "REPLACE_ME_CLOUDFRONT_ORIGIN_ACCESS_IDENTITY_ID"
    }, 
    "ETag": "ANPAJ2UCCR6DPCEXAMPLE", 
    "Location": "https://cloudfront.amazonaws.com/2017-10-30/origin-access-identity/cloudfront/ANPAJ2UCCR6DPCEXAMPLE"
}

```
Record the **Id** from the output as we will need to use it in subsequent steps, where we will refer to it as `REPLACE_ME_CLOUDFRONT_ORIGIN_ACCESS_IDENTITY_ID`.
  

**Note: Throughout this workshop you will be similarly opening files that have contents which need to be replaced (all will be prefixed with `REPLACE_ME_`, to make them easy to find using CTRL-F on Windows or ⌘-F on Mac.)**

To **open a file** in Cloud9, use the File Explorer on the left panel and double click `bucket-policy.json`:

![bucket-policy-image.png](/images/module-1/bucket-policy.png)

This will open `bucket-policy.json` in the File Editor panel.  Replace the string shown on both lines 9 and 12 with your Id and chosen bucket name used in the previous commands:

![replace-bucket-name.png](/images/module-1/replace-bucket-name.png)

In Cloud9, open the `website-cloudfront-distribution.json` file and edit the bucket name on line 12 and the Id on line 14:

![bucket-policy-image.png](/images/module-1/cloudfront-distribution.png)

Execute the following CLI command to add a public bucket policy to allow CloudFront:

```
aws s3api put-bucket-policy --bucket REPLACE_ME_BUCKET_NAME --policy file://~/environment/aws-modern-application-workshop/module-1/aws-cli/website-bucket-policy.json
```

#### Publish the Website Content to S3

Now that our S3 bucket is created, let's add the first iteration of the Mythical Mysfits homepage to the bucket.  Use the following S3 CLI command that mimics the linux command for copying files (**cp**) to copy the provided index.html page locally from your IDE up to the new S3 bucket (replacing the bucket name appropriately).

```
aws s3 cp ~/environment/aws-modern-application-workshop/module-1/web/index.html s3://REPLACE_ME_BUCKET_NAME/index.html
```

#### Create CloudFront Distribution

Using the AWS CLI, we will create a CloudFront distribution, and configure the S3 bucket as the origin. The JSON document for the necessary CloudFront configuration is located at: `~/environment/aws-modern-application-workshop/module-1/aws-cli/website-cloudfront-distribution.json`.  This file contains two strings that need to be replaced, the Id (indicated with `REPLACE_ME_ID`), and the bucket name you've chosen (indicated with `REPLACE_ME_BUCKET_NAME`).  

```
aws cloudfront create-distribution --distribution-config file://~/environment/aws-modern-application-workshop/module-1/aws-cli/website-cloudfront-distribution.json
```

  * If this was a web site used for more than just testing you should enable logging, and consider the AWS Web Application Firewall (WAF) service to help protect. For more information on the other configuration options, see [Values That You Specify When You Create or Update a Web Distribution](https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/distribution-web-values-specify.html) in the CloudFront documentation.


5. You can check on the status of the CloudFront distribution by using the CLI command:
```
aws cloudfront list-distributions
```
Note the `DomainName` as that will be your web site. After CloudFront creates your distribution, the value of `Status` will change from In Progress to Deployed.
For more information, see [Testing a Web Distribution](https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/distribution-web-testing.html) in the CloudFront documentation.


6. You have now configured Amazon CloudFront with basic settings and S3 as origin.

For more information on configuring CloudFront, see [Viewing and Updating CloudFront Distributions](https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/HowToUpdateDistribution.html) in the CloudFront documentation.

#### Test Website

Now, it can take some time e.g. 15 to 30 minutes for your newly created S3 bucket to be accessible by CloudFront, as it is a global network of 100+ edge locations. Open up your favorite web browser and enter the **Domain Name** from the CloudFront console, it will be a series of characters and end in cloudfront.net. If you get an error and/or redirect to the S3 bucket wait some more time.

![mysfits-welcome](/images/module-1/mysfits-welcome.png)

Congratulations, you have created the basic static Mythical Mysfits Website!

That concludes Module 1.

[Proceed to Module 2](/module-2)


## [AWS Developer Center](https://developer.aws)
