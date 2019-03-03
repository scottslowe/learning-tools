# Module 3 - Adding a Data Tier with Amazon DynamoDB

![Architecture](/images/module-3/architecture-module-3.png)

**Time to complete:** 20 minutes

**Services used:**
* Amazon DynamoDB

### Overview

Now that you have a service deployed and a working CI/CD pipeline to deliver changes to that service automatically whenever you update your code repository, you can quickly move new application features from conception to available for your Mythical Mysfits customers.  With this increased agility, let's add another foundational piece of functionality to the Mythical Mysfits website architecture, a data tier.  In this module you will create a table in Amazon DynamoDB, a managed and scalable NoSQL database service on AWS with super fast performance.  Rather than have all of the Mysfits be stored in a static JSON file, we will store them in a database to make the websites future more extensible and scalable.

### Adding a NoSQL Database to Mythical Mysfits

#### Create a DynamoDB Table

To add a DynamoDB table to the architecture, we have included another JSON CLI input file that defines a table called **MysfitsTable**. This table will have a primary index defined by a hash key attribute called **MysfitId**, and two more secondary indexes.  The first secondary index will have the hash key of **Species** and a range key of **MysfitId**, and the second secondary index will have the hash key of **Alignment** and a range key of **MysfitId**.  These two secondary indexes will allow us to execute queries against the table to retrieve all of the mysfits that match a given Species or Alignment to enable the filter functionality you may have noticed isn't yet working on the website.  You can view this file at `~/environment/aws-modern-application-workshop/module-3/aws-cli/dynamodb-table.json`. No changes need to be made to this file and it is ready to execute.

To create the table using the AWS CLI, execute the following command in the Cloud9 terminal:

```
aws dynamodb create-table --cli-input-json file://~/environment/aws-modern-application-workshop/module-3/aws-cli/dynamodb-table.json
```

After the command runs, you can view the details of your newly created table by executing the following AWS CLI command in the terminal:

```
aws dynamodb describe-table --table-name MysfitsTable
```

If we execute the following command to retrieve all of the items stored in the table, you'll see that the table is empty:

```
aws dynamodb scan --table-name MysfitsTable
```

```
{
    "Count": 0,
    "Items": [],
    "ScannedCount": 0,
    "ConsumedCapacity": null
}
```

#### Add Items to the DynamoDB Table

Also provided is a JSON file that can be used to batch insert a number of Mysfit items into this table.  This will be accomplished through the DynamoDB API **BatchWriteItem.** To call this API using the provided JSON file, execute the following terminal command (the response from the service should report that there are no items that went unprocessed):

```
aws dynamodb batch-write-item --request-items file://~/environment/aws-modern-application-workshop/module-3/aws-cli/populate-dynamodb.json
```

Now, if you run the same command to scan all of the table contents, you'll find the items have been loaded into the table:

```
aws dynamodb scan --table-name MysfitsTable
```

### Committing The First *Real* Code change

#### Copy the Updated Go Service Code
Now that we have our data included in the table, let's modify our application code to read from this table instead of returning the static JSON file that was used in Module 2.  We have included a new set of Go files for your microservice, but now instead of reading the static JSON file will make a request to DynamoDB.

The request is formed using the AWS SDK for Go. This SDK is a powerful yet simple way to interact with AWS services via Go code. It enables you to use service client definitions and functions that have great symmetry with the AWS APIs and CLI commands you've already been executing as part of this workshop.  Translating those commands to working Go code is simple when using The AWS SDK for Go.  To copy the new files into your CodeCommit repository directory, execute the following command in the terminal:

```
cp ~/environment/aws-modern-application-workshop/module-3/app/service/* ~/environment/MythicalMysfitsService-Repository/service/
```

#### Push the Updated Code into the CI/CD Pipeline

Now, we need to check in these code changes to CodeCommit using the git command line client.  Run the following commands to check in the new code changes and kick of your CI/CD pipeline:

```
cd ~/environment/MythicalMysfitsService-Repository
```

```
git add .
```

```
git commit -m "Add new integration to DynamoDB."
```

```
git push
```

Now, in just 5-10 minutes you'll see your code changes make it through your full CI/CD pipeline in CodePipeline and out to your deployed Go service to AWS Fargate on Amazon ECS.  Feel free to explore the AWS CodePipeline console to see the changes progress through your pipeline.

#### Update The Website Content in S3

Finally, we need to publish a new index.html page to our S3 bucket so that the new API functionality using query strings to filter responses will be used.  The new index.html file is located at `~/environment/aws-modern-application-workshop/module-3/web/index.html`.  Open this file in your Cloud9 IDE and replace the string indicating “REPLACE_ME” just as you did in Module 2, with the appropriate NLB endpoint. Remember do not inlcude the /mysfits path. Refer to the file you already edited in the /module-2/ directory if you need to.  After replacing the endpoint to point at your NLB, upload the new index.html file by running the following command (replacing with the name of the bucket you created in Module 1:

```
aws s3 cp --recursive ~/environment/aws-modern-application-workshop/module-3/web/ s3://your_bucket_name_here/
```

Re-visit your Mythical Mysfits website to see the new population of Mysfits loading from your DynamoDB table and how the Filter functionality is working!

That concludes module 3.

[Proceed to Module 4](/module-4)
