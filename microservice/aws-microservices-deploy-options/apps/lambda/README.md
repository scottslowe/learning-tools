# Serverless API
This project uses [`aws-serverless-java-container`](https://github.com/awslabs/aws-serverless-java-container).

The starter project defines a number of resources `/`, `/resources/greeting`, and `/resources/names` that can accept `GET` requests.

The project folder also includes three Microservices Serverless Application Model templates `GreetingSam.yaml`, `NameSam.yaml` and `WebApp.yaml` . You can use theses [SAM](https://github.com/awslabs/serverless-application-model) files to deploy the project to AWS Lambda and Amazon API Gateway or test in local with [SAM Local](https://github.com/awslabs/aws-sam-local). 

## Prepare Microservice Source Code

The example application consists of there Microservices, they are Greeting Service, Name Service and WebApp Service. Each service is developed as an self-contained module. WebApp service is responsible for frontend service, Greeting and Name service are responsbile for generating response to partials of to the client.

To start with, please use the following commands to clone these services source code into the workspace

Clone Greeting Service Source Code from each service repo 


## Compile

Using [Maven](https://maven.apache.org/), you can create an AWS Lambda-compatible jar file simply by running the maven package command from the projct folder.

```bash
cd services/Greeting && mvn clean package -Plambda
cd ../Name && mvn clean package -Plambda
cd ../WebApp && mvn clean package -Plambda
```

## Install

You can use [AWS SAM Local](https://github.com/awslabs/aws-sam-local) to start your project.

First, install SAM local:

```bash
$ npm install -g aws-sam-local
```

## Test

### Test Lambdas

Next, from the project root folder - where the SAM templates are located - start the APIs with the SAM Local CLI.

```bash
cd app/lambda
sam local invoke -t NameSam.yaml -e test/find-all-names-event.json NamesFunction
sam local invoke -t NameSam.yaml -e test/find-name-event.json NamesFunction
sam local invoke -t WebAppSam.yaml -e test/greeting-event.json GreetingFunction
```

### Test API

Next, from the project root folder - where the SAM files are located - start the APIs with the SAM Local CLI.

For Mac users, please use the following command

```bash
cd apps/lambda
sam local start-api --template GreetingSam.yaml --port 3000 
sam local start-api --template NameSam.yaml --port 3001 
sam local start-api --template WebAppSam.yaml --port 3002 --env-vars test/env-mac.json
```

For Windows users, please use the following command

```bash
cd apps/lambda
sam local start-api --template GreetingSam.yaml --port 3000 
sam local start-api --template NameSam.yaml --port 3001 
sam local start-api --template WebAppSam.yaml --port 3002 --env-vars test/env-win.json
```

Using a new shell, you can send a test ping request to your API:

```bash
$ curl -s http://127.0.0.1:3000/resources/greeting

Hello

$ curl -s http://127.0.0.1:3002/1

Hello Sheldon
``` 

## Deploy to AWS

You can use the [AWS SAM Local CLI](https://github.com/awslabs/aws-sam-local) to quickly deploy your application to AWS Lambda and Amazon API Gateway with your SAM template. To use the package command you will need to have the [AWS CLI](https://aws.amazon.com/cli/) installed.

You will need an S3 bucket to store the artifacts for deployment. Once you have created the S3 bucket, run the following command from the project's root folder - where the `sam.yaml` file is located:

### Deploy Greeting Service

```bash
sam package --template-file GreetingSam.yaml --output-template-file sam.transformed.yaml --s3-bucket <YOUR S3 BUCKET NAME>
```

As the command output suggests, you can now use the cli to deploy the application. Choose a stack name and run the `aws cloudformation deploy` command from the output of the package command.

```bash
sam deploy --template-file sam.transformed.yaml --stack-name aws-microservices-deploy-options-lambda-greeting --capabilities CAPABILITY_IAM
```

### Deploy Name Service

```bash
sam package --template-file NameSam.yaml --output-template-file sam.transformed.yaml --s3-bucket <YOUR S3 BUCKET NAME>
```

As the command output suggests, you can now use the cli to deploy the application. Choose a stack name and run the `aws cloudformation deploy` command from the output of the package command.
 
```
$ sam deploy --template-file sam.transformed.yaml --stack-name aws-microservices-deploy-options-lambda-name --capabilities CAPABILITY_IAM
```

### Deploy WebApp Service

```bash
sam package --template-file WebAppSam.yaml --output-template-file sam.transformed.yaml --s3-bucket <YOUR S3 BUCKET NAME>
```

As the command output suggests, you can now use the cli to deploy the application. Choose a stack name and run the `aws cloudformation deploy` command from the output of the package command.
 
```
$ sam deploy --template-file sam.transformed.yaml --stack-name aws-microservices-deploy-options-lambda-webapp --capabilities CAPABILITY_IAM
```

Once the application is deployed, you can describe the stack to show the API endpoint that was created. The endpoint should be the `WebAppApiEndpoint` key of the `Outputs` property:

```
$ aws cloudformation describe-stacks --stack-name aws-microservices-deploy-options-lambda-webapp --query 'Stacks[0].Outputs[*].{Service:OutputKey,Endpoint:OutputValue}'
[   
    {   
        "Service": "WebAppApiEndpoint",
        "WebAppApiEndpoint": "https://XXX.execute-api.ap-southeast-2.amazonaws.com/prod/"
    }
]
```

Copy the `WebAppApiEndpoint` into a browser or use curl to test your first request:

```bash
$ curl -s https://xxxxxxx.execute-api.us-west-2.amazonaws.com/prod/1
Hello Sheldon
```
