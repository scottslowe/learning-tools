# Module 1: IDE Setup and Static Website Hosting

![Architecture](/images/module-1/architecture-module-1.png)

**Time to complete:** 20 minutes

**Services used:**
* AWS Cloud9
* Amazon Simple Storage Service (S3)

In this module, follow the instructions to create your cloud-based IDE on AWS Cloud9 and deploy the first version of the static Mythical Mysfits website.  Amazon S3 is a highly durable, highly available, and inexpensive object storage service that can serve stored objects directly via HTTP. This makes it wonderfully useful for serving static web content (html, js, css, media content, etc.) directly to web browsers for sites on the Internet.  We will utilize S3 to host the content for our Mythical Mysfits website.

### Getting Started

#### Sign In to the AWS Console
To begin, sign in to the AWS Console for the AWS account you will be using in this workshop.

This web application can be deployed in any AWS region that supports all the services used in this application. The supported regions include:

* us-east-1 (N. Virginia)
* us-east-2 (Ohio)
* us-west-2 (Oregon)
* eu-west-1 (Ireland)

Select a region from the dropdown in the upper right corner of the AWS Management Console.
Once you've selected the region for the IDE,
that becomes the default region for all of the resources you create using the IDE.

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

In the bottom panel of your new Cloud9 IDE, you will see a terminal command line terminal open and read to use.  Run the following git command in the terminal to clone the necessary code to complete this tutorial:

```
git clone https://github.com/aws-samples/aws-modern-application-workshop.git
```

After cloning the repository, you'll see that your project explorer now includes the files cloned:
![cloud9-explorer](/images/module-1/cloud9-explorer.png)


In the terminal, change directory to the newly cloned repository directory:

```
cd aws-modern-application-workshop
```

### Creating a Static Website in Amazon S3

#### Create an S3 Bucket and Configure it for Website Hosting
Next, we will create the infrastructure components needed for hosting a static website in Amazon S3 via the AWS CLI.  

First, create an S3 bucket, replace REPLACE_ME_BUCKET_NAME with your own unique bucket name.  Copy the name you choose and save it for later, as you will use it in several other places during this workshop:

```
aws s3 mb s3://REPLACE_ME_BUCKET_NAME
```

Now that we have created a bucket, we need to set some configuration options that enable the bucket to be used for static website hosting.  This configuration enables the objects in the bucket to be requested using a registered public DNS name for the bucket, as well as direct site requests to the base path of the DNS name to a selected website homepage (index.html in most cases). Don't forget to replace REPLACE_ME_BUCKET_NAME with the bucket name you used on the previous step:

```
aws s3 website s3://REPLACE_ME_BUCKET_NAME --index-document index.html
```

#### Update the S3 Bucket Policy

All buckets created in Amazon S3 are fully private by default.  In order to be used as a public website, we need to create an S3 **Bucket Policy** that indicates objects stored within this new bucket may be publicly accessed by anyone. Bucket policies are represented as JSON documents that define the S3 *Actions* (S3 API calls) that are allowed (or not not allowed) to be performed by different *Principals* (in our case the public, or anyone). The JSON document for the necessary bucket policy is located at: `/~/environment/aws-modern-application-workshop/module-1/aws-cli/website-bucket-policy.json`.  This file includes one place that you need to change to use the new bucket name you've created (indicated with `REPLACE_ME_BUCKET_NAME`).

Execute the following CLI command to add a public bucket policy to your website:

```
aws s3api put-bucket-policy --bucket REPLACE_ME_BUCKET_NAME --policy file://~/environment/aws-modern-application-workshop/module-1/aws-cli/website-bucket-policy.json
```

#### Publish the Website Content to S3

Now that our new website bucket is configured appropriately, let's add the first iteration of the Mythical Mysfits homepage to the bucket.  Use the following S3 CLI command that mimics the linux command for copying files (**cp**) to copy the provided index.html page locally from your IDE up to the new S3 bucket (replacing REPLACE_ME_BUCKET_NAME appropriately).

```
aws s3 cp ~/environment/aws-modern-application-workshop/module-1/web/index.html s3://REPLACE_ME_BUCKET_NAME/index.html
```

Browse to one of the following addresses,
replacing REPLACE_ME_BUCKET_NAME with the name of your bucket and REPLACE_ME_REGION
with the region where you signed into the console.
Note that one of the addresses contains a '.' before the region name,
and the other a '-'. The one you use depends on the region you're using.

For us-east-1 (N. Virginia), us-west-2 (Oregon), eu-west-1 (Ireland) use:

http://REPLACE_ME_BUCKET_NAME.s3-website-REPLACE_ME_YOUR_REGION.amazonaws.com

For us-east-2 (Ohio) use:

http://REPLACE_ME_BUCKET_NAME.s3-website.REPLACE_ME_YOUR_REGION.amazonaws.com

![mysfits-welcome](/images/module-1/mysfits-welcome.png)

Congratulations, you have created the basic static Mythical Mysfits Website!

That concludes Module 1.

[Proceed to Module 2](/module-2)
