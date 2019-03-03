# Module 4: Adding User and API features with Amazon API Gateway and AWS Cognito

![Architecture](/images/module-4/architecture-module-4.png)

**Time to complete:** 60 minutes

**Services used:**
* [Amazon Cognito](http://aws.amazon.com/cognito/)
* [Amazon API Gateway](https://aws.amazon.com/api-gateway/)
* [Amazon Simple Storage Service (S3)](https://aws.amazon.com/s3/)

### Overview

In order to add some more critical aspects to the Mythical Mysfits website, like allowing users to vote for their favorite mysfit and adopt a mysfit, we need to first have users register on the website.  To enable registration and authentication of website users, we will create a [**User Pool**](https://docs.aws.amazon.com/cognito/latest/developerguide/cognito-user-identity-pools.html) in [**AWS Cognito**](http://aws.amazon.com/cognito/) - a fully managed user identity management service.  Then, to make sure that only registered users are authorized to like or adopt mysfits on the website, we will deploy an REST API with [**Amazon API Gateway**](https://aws.amazon.com/api-gateway/) to sit in front of our NLB. Amazon API Gateway is also a managed service, and provides commonly required REST API capabilities out of the box like SSL termination, request authorization, throttling, API stages and versioning, and much more.

### Adding a User Pool for Website Users

#### Create the Cognito User Pool

To create the **Cognito User Pool** where all of the Mythical Mysfits visitors will be stored, execute the following CLI command to create a user pool named *MysfitsUserPool* and indicate that all users who are registered with this pool should automatically have their email address verified via confirmation email before they become confirmed users.

```
aws cognito-idp create-user-pool --pool-name MysfitsUserPool --auto-verified-attributes email
```
Copy the response from the above command, which includes the unique ID for your user pool that you will need to use in later steps. Eg: `Id: us-east-1_ab12345YZ`

#### Create a Cognito User Pool Client

Next, in order to integrate our frontend website with Cognito, we must create a new **User Pool Client** for this user pool. This generates a unique client identifier that will allow our website to be authorized to call the unauthenticated APIs in cognito where website users can sign-in and register against the Mythical Mysfits user pool.  To create a new client using the AWS CLI for the above user pool, run the following command (replacing the `--user-pool-id` value with the one you copied above):

```
aws cognito-idp create-user-pool-client --user-pool-id REPLACE_ME --client-name MysfitsUserPoolClient
```

### Adding a new REST API with Amazon API Gateway

#### Create an API Gateway VPC Link
Next, let's turn our attention to creating a new RESTful API in front of our existing Flask service, so that we can perform request authorization before our NLB receives any requests.  We will do this with **Amazon API Gateway**, as described in the module overview.  In order for API Gateway to privately integrate with our NLB, we will configure an **API Gateway VPC Link** that enables API Gateway APIs to directly integrate with backend web services that are privately hosted inside a VPC. **Note:** For the purposes of this workshop, we created the NLB to be *internet-facing* so that it could be called directly in earlier modules. Because of this, even though we will be requiring Authorization tokens in our API after this module, our NLB will still actually be open to the public behind the API Gateway API.  In a real-world scenario, you should create your NLB to be *internal* from the beginning (or create a new internal load balancer to replace the existing one), knowing that API Gateway would be your strategy for Internet-facing API authorization.  But for the sake of time, we'll use the NLB that we've already created that will stay publicly accessible.

Create the VPC Link for our upcoming REST API using the following CLI command (you will need to replace the indicated value with the Load Balancer ARN you saved when the NLB was created in module 2):

```
aws apigateway create-vpc-link --name MysfitsApiVpcLink --target-arns REPLACE_ME_NLB_ARN > ~/environment/api-gateway-link-output.json
```

The above command will create a file called `api-gateway-link-output.json` that contains the `id` for the VPC Link that is being created.  It will also show the status as `PENDING`, similar to below.  It will take about 5-10 minutes to finish being created, you can copy the `id` from this file and proceed on to the next step.

```
{
    "status": "PENDING",
    "targetArns": [
        "YOUR_ARN_HERE"
    ],
    "id": "abcdef1",
    "name": "MysfitsApiVpcLink"
}
```

With the VPC link creating, we can move on to create the actual REST API using Amazon API Gateway.  

#### Create the REST API using Swagger

Your MythicalMysfits REST API is defined using **Swagger**, a popular open-source framework for describing APIs via JSON.  This Swagger definition of the API is located at `~/environment/aws-modern-applicaiton-workshop/module-4/aws-cli/api-swagger.json`.  Open this file and you'll see the REST API and all of its resources, methods, and configuration defined within.  

There are several places within this JSON file that need to be updated to include parameters specific to your Cognito User Pool, as well as your Network Load Balancer.  

The `securityDefinitions` object within the API definition indicates that we have setup an apiKey authorization mechanism using the Authorization header.  You will notice that AWS has provided custom extensions to Swagger using the prefix `x-amazon-api-gateway-`, these extensions are where API Gateway specific functionality can be added to typical swagger files to take advantage of API Gateway-specific capabilities.

CTRL-F through the file to search for the various places `REPLACE_ME` is located and awaiting your specific parameters.  Once the edits have been made, save the file and execute the following AWS CLI command:

```
aws apigateway import-rest-api --parameters endpointConfigurationTypes=REGIONAL --body file://~/environment/aws-modern-application-workshop/module-4/aws-cli/api-swagger.json --fail-on-warnings
```

Copy the response this command returns and save the `id` value for the next step:

```
{
    "name": "MysfitsApi",
    "endpointConfiguration": {
        "types": [
            "REGIONAL"
        ]
    },
    "id": "abcde12345",
    "createdDate": 1529613528
}
```

#### Deploy the API

Now, our API has been created, but it's yet to be deployed anywhere. To deploy our API, we must first create a deployment and indicate which **stage** the deployment is fore.  A stage is a named reference to a deployment, which is a snapshot of the API. You use a Stage to manage and optimize a particular deployment. For example, you can set up stage settings to enable caching, customize request throttling, configure logging, define stage variables or attach a canary release for testing.  We will call our stage `prod`. To create a deployment for the prod stage, execute the following CLI command:

```
aws apigateway create-deployment --rest-api-id REPLACE_ME_WITH_API_ID --stage-name prod
```

With that, our REST API that's capable of user Authorization is deployed and available on the Internet... but where?!  Your API is available at the following location:

```
https://REPLACE_ME_WITH_API_ID.execute-api.REPLACE_ME_WITH_REGION.amazonaws.com/prod
```

Copy the above, replacing the appropriate values, and add `/mysfits` to the end of the URI.  Entered into a browser address bar, you should once again see your Mysfits JSON response.  But, we've added several capabilities like adopting and liking mysfits that our Flask service backend doesn't have implemented yet.

Let's take care of that next.


### Updating the Mythical Mysfits Website

#### Update the Flask Service Backend

To accommodate the new functionality to view Mysfit Profiles, like, and adopt them, we have included updated Python code for your backend Flask web service.  Let's overwrite your existing codebase with these files and push them into the repository:

```
cd ~/environment/MythicalMysfitsService-Repository/
```

```
cp -r ~/environment/aws-modern-application-workshop/module-4/app/* .
```

```
git add .
```

```
git commit -m "Update service code backend to enable additional website features."
```

```
git push
```

While those service updates are being automatically pushed through your CI/CD pipeline, continue on to the next step.

#### Update the Mythical Mysfits Website in S3

Open the new version of the Mythical Mysfits index.html file we will push to S3 shortly, it is located at: `~/environment/aws-modern-application-workshop/module-4/app/web/index.html`
In this new index.html file, you'll notice additional HTML and JavaScript code that is being used to add a user registration and login experience.  This code is interacting with the AWS Cognito JavaScript SDK to help manage registration, authentication, and authorization to all of the API calls that require it.

In this file, replace the strings **REPLACE_ME** inside the single quotes with the OutputValues you copied from above and save the file:

![before-replace](/images/module-4/before-replace.png)

Also, for the user registration process, you have an additional two HTML files to insert these values into.  `register.html` and `confirm.html`.  Insert the copied values into the **REPLACE_ME** strings in these files as well.

Now, lets copy these HTML files, as well as the Cognito JavaScript SDK to the S3 bucket hosting our Mythical Mysfits website content so that the new features will be published online.

```
aws s3 cp --recursive ~/environment/aws-modern-application-workshop/module-4/web/ s3://YOUR-S3-BUCKET/
```

Refresh the Mythical Mysfits website in your browser to see the new functionality in action!

This concludes Module 4.

[Proceed to Module 5](/module-5)


## [AWS Developer Center](https://developer.aws)
