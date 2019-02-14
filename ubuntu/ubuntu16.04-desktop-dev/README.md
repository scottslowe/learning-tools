# Ubuntu 16.04 desktop development machine #

### What is this repository for? ###

* This repository contains a Vagrant file and scripts to create a development machine based on Ubuntu 16.04 
* 0.1.0
* [Learn Markdown](https://bitbucket.org/tutorials/markdowndemo)

### What tools and libraries are installed ? ###

#### The tools and packages installed by this scipt are the following: ####

* Browsers
````
- Google Chrome
- Mozilla Firefox
- Opera
````
* IDEs
````
- IntelliJ IDEA
- Sublime Text 3
````
* VPN Client (SoftEther VPN Client)
* DevTools
````
- Node and npm (node package manager)
- NVM (Node version manager)
- Angular CLI
- AWS CLI
- OpenJDK 8
- Maven 3
- Gradle 4.3.1
- Git
- Docker 
- Docker Compose
- Mongo instance (running as a docker container)
- MongoBooster
````
* Other tools
````
- Guake Terminal
- pip
````

### How do I get set up? ###

* You can either run ``vagrant up``  or create your own Vagrant file to start from an already built Vagrant box:
````
Vagrant.configure(2) do |config|

  config.vm.box = "eciuca/ubuntu1604-dev-desktop"
  config.vm.box_version = "0.1.0"

end
````
* After the Virtual machine is created you can login with the following credentials:
````
user: vagrant
password: vagrant
````
* The following tools need to be configured:
````
- Git (user name, email and ssh key)
- IntelliJ IDEA
- AWS CLI credentials
- SoftEther VPN Client

If you use private registries/repositories
	- Maven
	- Docker credentials 
````

### Contribution guidelines ###

* Writing tests
* Code review
* Other guidelines

### Who do I talk to? ###

1. Emanuel Ciuca
	- Mail: emanuel.ciuca@gmail.com