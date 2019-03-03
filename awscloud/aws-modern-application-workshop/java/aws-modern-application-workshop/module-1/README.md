# Module 1: IDE Setup and Static Website Hosting

![Architecture](/images/module-1/architecture-module-1.png)

**Time to complete:** 20 minutes

**Services used:**
* [AWS Cloud9](https://aws.amazon.com/cloud9/)
* [Amazon Simple Storage Service (S3)](https://aws.amazon.com/s3/)

In this module, follow the instructions to create your cloud-based IDE on [AWS Cloud9](https://aws.amazon.com/cloud9/) and deploy the first version of the static Mythical Mysfits website.  [Amazon S3](https://aws.amazon.com/s3/) is a highly durable, highly available, and inexpensive object storage service that can serve stored objects directly via HTTP. This makes it adequate for serving static web content (html, js, css, media, etc.) directly to web browsers.  

## Sign In to the AWS Console
To begin, sign in to the [AWS Console](https://console.aws.amazon.com) for the AWS account you will be using in this workshop.

We recommend using one of the following AWS Regions for this workshop:

* us-east-1 (N. Virginia)
* us-east-2 (Ohio)
* us-west-2 (Oregon)
* eu-west-1 (Ireland)

Select a region from the dropdown in the upper right corner of the AWS Management Console.

## Creating your Mythical Mysifts IDE

### Create a new AWS Cloud9 Environment

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

### Cloning the Mythical Mysfits Workshop Repository

In the bottom panel of your new Cloud9 IDE, you will see a terminal command line terminal open and ready to use. If you close it or need a new one, use the *Window* > *New Terminal* menu.

The code for this workshop is open on the [aws-samples/aws-modern-application-workshop](https://github.com/aws-samples/aws-modern-application-workshop) GitHub repository. The following command clones the java branch to your Cloud9 environment:

```
git clone -b java https://github.com/aws-samples/aws-modern-application-workshop.git
```

After cloning the repository, you'll see that your project explorer now includes the files cloned:
![cloud9-explorer](/images/module-1/cloud9-explorer.png)


In the terminal, change directory to the newly cloned repository directory:

```
cd aws-modern-application-workshop
```

## Creating a Static Website in Amazon S3

### Create an S3 Bucket
The first AWS resource in the Mythical Misfis architecture is an [S3 Bucket](https://docs.aws.amazon.com/AmazonS3/latest/dev/UsingBucket.html). Choose a name for your bucket and create it using the [```aws s3 mb```](https://docs.aws.amazon.com/cli/latest/reference/s3/mb.html) command, replacing where indicated:

```
aws s3 mb s3://REPLACE_ME_BUCKET_NAME
```

* Observe the [requirements for bucket names](https://docs.aws.amazon.com/AmazonS3/latest/dev/BucketRestrictions.html#bucketnamingrules).
* Copy the name you choose and save it for later in a separate textfile. In this workshop you will manage several resources, it helps to take keep track of names and IDs. If you prefer, feel free to set and use this values as environment variables to avoid text substitution. 

You can verify that the bucket was created successfuly using the [```aws s3 ls```](https://docs.aws.amazon.com/cli/latest/reference/s3/ls.html) command.

```
aws s3 ls s3://REPLACE_ME_BUCKET_NAME
```

You will see no output as the bucket is empty. The CLI will emmit an error if the bucket does not exist or is not accesible. Notice that you did not have to setup credentials, as they are [generated and rotated automatically by Cloud9](https://docs.aws.amazon.com/cloud9/latest/user-guide/auth-and-access-control.html#auth-and-access-control-temporary-managed-credentials-create-update). 

### Upload the Website Content to your S3 Bucket

Copy the initial page of the Mystical Misfits website (index.html) to your S3 bucket using the [aws s3 cp] command:

```
aws s3 cp ~/environment/aws-modern-application-workshop/module-1/web/index.html s3://REPLACE_ME_BUCKET_NAME/index.html
```

Following the "principle of least privilege", access to buckets and objects is denied by default. You can verify that issuing a GET request:

```
curl -I "https://REPLACE_ME_BUCKET_NAME.s3-$(aws configure get region).amazonaws.com/index.html"
HTTP/1.1 403 Forbidden
```

 In the next step you will create the authorization policies that explicitly allows access to your objects.

### Update the S3 Bucket Policy

To serve as a public website, we can create an S3 [Bucket Policy](https://docs.aws.amazon.com/AmazonS3/latest/dev/example-bucket-policies.html) that indicates objects stored within this new bucket are publicly accessible. S3 Bucket Policies are represented as JSON documents that authorizes or denies the invocation of S3 *Actions* (S3 API calls) to *Principals* (in our public example case, anyone). 

The JSON document for the necessary bucket policy is located at: `~/environment/aws-modern-application-workshop/module-1/aws-cli/website-bucket-policy.json`.  This file contains a string that needs to be replaced with the bucket name you've chosen (indicated with `REPLACE_ME_BUCKET_NAME`). 

**Note: Throughout this workshop you will be similarly opening files that have contents which need to be replaced (all will be prefixed with `REPLACE_ME_`, to make them easy to find using CTRL-F on Windows or ⌘-F on Mac.)**

To **open a file** in Cloud9, use the File Explorer on the left panel and double click `website-bucket-policy.json`:

![bucket-policy-image.png](/images/module-1/bucket-policy-image.png)

This will open `bucket-policy.json` in the File Editor panel.  Replace the string shown with your chosen bucket name used in the previous commands:

![replace-bucket-name.png](/images/module-1/replace-bucket-name.png)

**! Before you do that !**

We highly recommend that you never grant any kind of public access to your S3 bucket. Serving data out of Amazon S3 incurs in costs, as detailed in the [S3 pricing page](https://aws.amazon.com/s3/pricing). This workshop usage is under the limits of AWS Free Tier, that includes 5 GB of Amazon S3 storage in the Standard Storage class, 20,000 Get Requests, 2,000 Put Requests, and 15 GB of data transfer out each month for one year.

When you are done with the workshop, you can prevent public access by deleting the bucket policy with the following command:
```
aws s3api delete -bucket-policy --bucket REPLACE_ME_BUCKET_NAME 
```

**! Now that you know!**

Execute the following CLI command to add a public bucket policy to your website:

```
aws s3api put-bucket-policy --bucket REPLACE_ME_BUCKET_NAME --policy file://~/environment/aws-modern-application-workshop/module-1/aws-cli/website-bucket-policy.json
```

Your S3 Object should now be accessible:

```
curl -I "https://REPLACE_ME_BUCKET_NAME.s3-$(aws configure get region).amazonaws.com/index.html"
HTTP/1.1 200 OK
```

### Configure website Hosting

As you just verified, your bucket is ready to store and serve "objects" (a.k.a. files plus their metadata). However, a bucket behaves differently from usual webservers. For example, a GET request to the bucket HTTP endpoint would trigger a [List Objects](https://docs.aws.amazon.com/AmazonS3/latest/API/v2-RESTBucketGET.html) operation, instead of returning the root object. The bucket policy does not grant the ```s3:ListBucket```, so this request is denied:

```
curl -I "https://REPLACE_ME_BUCKET_NAME.s3-$(aws configure get region).amazonaws.com"
HTTP/1.1 403 Forbidden
```

Configuring [static website hosting](https://docs.aws.amazon.com/AmazonS3/latest/dev/WebsiteHosting.html) gives you a different endpoint that serves the specified index and error documents, and can evaluate redirection rules. Create a website configuration with the [aws s3 website](https://docs.aws.amazon.com/cli/latest/reference/s3/website.html) command:

```
aws s3 website s3://REPLACE_ME_BUCKET_NAME --index-document index.html
```

Now you can use the website endpoint to serve static content directly from your S3 Bucket. The string to replace **REPLACE_ME_YOUR_REGION** should match whichever region you  created the S3 bucket within (eg: us-east-1):

For us-east-1 (N. Virginia), us-west-2 (Oregon), eu-west-1 (Ireland) use:
```
http://REPLACE_ME_BUCKET_NAME.s3-website-REPLACE_ME_YOUR_REGION.amazonaws.com
```

For us-east-2 (Ohio) use:
```
http://REPLACE_ME_BUCKET_NAME.s3-website.REPLACE_ME_YOUR_REGION.amazonaws.com
```

Verify that the static website is being served correctly:

```
curl -I http://REPLACE_ME_BUCKET_NAME.s3-website-REPLACE_ME_YOUR_REGION.amazonaws.com"
HTTP/1.1 200 OK
```

## Visit the initial Mythical Mysfits website

Now, open up your favorite web browser and enter one of the below URIs into the address bar.  One of the below URIs contains a '.' before the region name, and the other a '-'. Which you should use depends on the region you're using.


![mysfits-welcome](/images/module-1/mysfits-welcome.png)

Congratulations, you have created the basic static Mythical Mysfits Website!

That concludes Module 1.

[Proceed to Module 2](/module-2)


## [AWS Developer Center](https://developer.aws)
