# Introduction
Welcome to Spring Microservices in Action, Chapter 2.  Chapter 2 focuses on what exactly is a microservice and goes into more detail on how to build a microservice using Spring Boot.  This chapter in this code focuses on building a single service called the licensing service.  After you have compiled and started the code you should have a service called the licensing service up and running.

# Software needed
1.	Apache Maven (http://maven.apache.org). I used version 3.3.9 of the Maven. I chose Maven because, while other build tools like Gradle are extremely popular, Maven is still the pre-dominate build tool in use in the Java ecosystem. All of the code examples in this book have been compiled with Java version 1.8.
2.	Docker (http://docker.com). I built the code examples in this book using Docker V1.12 and above. I am taking advantage of the embedded DNS server in Docker that came out in release V1.11. New Docker releases are constantly coming out so it's release version you are using may change on a regular basis.
3.	Git Client (http://git-scm.com). All of the source code for this book is stored in a GitHub repository. For the book, I used version 2.8.4 of the git client.

# Building the Docker Images for Chapter 2
To build the code examples for Chapter 2 as a docker image, open a command-line window change to the directory where you have downloaded the chapter 2 source code.

Run the following maven command.  This command will execute the [Spotify docker plugin](https://github.com/spotify/docker-maven-plugin) defined in the pom.xml file.  

   **mvn clean package docker:build**

If everything builds successfully you should see a message indicating that the build was successful.

# Running the services for Chapter 2

Now we are going to use docker-compose to start the actual image.  To start the docker image,
change to the directory containing  your chapter 2 source code.  Issue the following docker-compose command:

   **docker-compose -f docker/common/docker-compose.yml up**

If everything starts correctly you should see a bunch of Spring Boot information fly by on standard out.  At this point all of the services needed for the chapter code examples will be running.
