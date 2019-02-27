# Mars - Azure Serverless Function in Action

live site: www.cloudhero.me

## User Story
A user launches the website and login in. Then she will see a list of courses that she has registered. From there, she can click the course and navigate files.

## Non Functional Requirement 
Keep the hosting cost low. Azure provides a serverless feature. The pricing model is by number of requests, asopposite to VM which you pay for computing hours, even no one accesses it.
Reasonable responding time.

## Design
An Azure Function App with a HTTP trigger will expose an URL to allow a user to access.
The Function App will return SPA
The SPA will communicate a WebAPI which will provide list of files stored on the Azure Blob Storage. 

<img src="https://github.com/victorguo1/Mars/blob/bf3034e515b53ee6845bf7c99d9169fe9dbe45bc/Assets/Mars%20Site%20Architecture.jpg?raw=true" ></img> 

## Development
### Serverless 101 
Create Azure Function App - Portal                                     
https://docs.microsoft.com/en-us/azure/azure-functions/functions-create-first-azure-function

Create Azure Function App – Visual Studio                                         
https://docs.microsoft.com/en-us/azure/azure-functions/functions-create-your-first-function-visual-studio

## Serverless 201
### Serverless website
https://stackoverflow.com/questions/41789192/how-to-build-serverless-web-site-on-azure-rather-then-on-aws-amazon
https://dontpaniclabs.com/blog/post/2017/09/20/a-website-without-servers-using-azure-functions-part-1-introduction

### Access Azure storage
https://cmatskas.com/copy-azure-blob-data-between-storage-accounts-using-functions/

### Serverless 301
Working with identity in an Azure Function                  
https://contos.io/working-with-identity-in-an-azure-function-1a981e10b900
