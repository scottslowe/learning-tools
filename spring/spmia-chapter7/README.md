# Introduction
Welcome to Spring Microservices in Action, Chapter 7.  Chapter 7 demonstrates how to build security with your services using Spring Cloud Security.  In this chapter we build an OAUth2 Authentication service using OAuth2.  This chapter has two branches of code.

1. The master branch uses Spring Cloud Security to build a standard Spring Cloud OAuth2 service.  With the master branch of the project our OAuth2 service will create an OAuth2 token for a user.  Every protected service will need to take that service and callback into the OAuth2 service to validated.

2.  The JWT_Example branch will build an OAuth2 service that uses the JavasScript Web Token (JWT) specification.   JWT provides a standard, cryptographically signed token.  With JWT services protected by OAuth2 do not need to call back into the OAuth2 services to validate the token.  Instead, they can determine if the JWT token has been tampered with and if not use its contents to validate the user.  To download this branch you need to first checkout the master branch of the project and the do a git checkout -b JWT_Example to pull down the branch.     

By the time you are done reading this chapter you will have built and/or deployed:

1. A Spring Cloud based OAuth2 authentication service that can issue and validate OAuth2 tokens.  
2. A Spring Cloud Config server that is deployed as Docker container and can manage a services configuration information using a file system or GitHub-based repository.
3.  A Eureka server running as a Spring-Cloud based service.  This service will allow multiple service instances to register with it.  Clients that need to call a service will use Eureka to lookup the physical location of the target service.
4.  A Zuul API Gateway.  All of our microservices can be routed through the gateway and have pre, response and
post policies enforced on the calls.
5.  A organization service that will manage organization data used within EagleEye.
6.  A new version of the organization service.  This service is used to demonstrate how to use the Zuul API gateway to route to different versions of a service.
7.  A special routes services that the the API gateway will call to determine whether or not it should route a user's service call to a different service then the one they were originally calling.  This service is used in conjunction with the orgservice-new service to determine whether a call to the organization service gets routed to an old version of the organization service vs. a new version of the service.
8.  A licensing service that will manage licensing data used within EagleEye.
9.  A Postgres SQL database used to hold the data for these two services.

# Software needed
1.	[Apache Maven] (http://maven.apache.org). I used version 3.3.9 of the Maven. I chose Maven because, while other build tools like Gradle are extremely popular, Maven is still the pre-dominate build tool in use in the Java ecosystem. All of the code examples in this book have been compiled with Java version 1.8.
2.	[Docker] (http://docker.com). I built the code examples in this book using Docker V1.12 and above. I am taking advantage of the embedded DNS server in Docker that came out in release V1.11. New Docker releases are constantly coming out so it's release version you are using may change on a regular basis.
3.	[Git Client] (http://git-scm.com). All of the source code for this book is stored in a GitHub repository. For the book, I used version 2.8.4 of the git client.

# Building the Docker Images for Chapter 7
To build the code examples for Chapter 7 as a docker image, open a command-line window change to the directory where you have downloaded the chapter 7 source code.

Run the following maven command.  This command will execute the [Spotify docker plugin](https://github.com/spotify/docker-maven-plugin) defined in the pom.xml file.  

   **mvn clean package docker:build**

Running the above command at the root of the project directory will build all of the projects.  If everything builds successfully you should see a message indicating that the build was successful.

# Running the services in Chapter 7

Now we are going to use docker-compose to start the actual image.  To start the docker image,
change to the directory containing  your chapter 7 source code.  Issue the following docker-compose command:

   **docker-compose -f docker/common/docker-compose.yml up**

If everything starts correctly you should see a bunch of Spring Boot information fly by on standard out.  At this point all of the services needed for the chapter code examples will be running.
