# Deploying services onto EKS with Spinnaker

## Install Halyard

Halyard is the command line interface for managing and deploying Spinnaker. Although a Helm chart is available for Spinnaker, Halyard is still considered the best way to install Spinnaker.

### Install on OSX

**Prerequisites**

Be sure you have Java 1.8 installed before continuing.  To determine whether you have Java 1.8 installed, from the command line, type:

```
java -version
```

If you don't have it installed, Java 1.8 can be download for OSX from [http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html](http://download.oracle.com/otn-pub/java/jdk/8u171-b11/512cd62ec5174c3487ac17c61aaa89e8/jdk-8u171-macosx-x64.dmg).

Once installed, run java_home to verify the Java 1.8 home:

```
/usr/libexec/java_home -v 1.8
```

**Download the Installer**

```
curl -O https://raw.githubusercontent.com/spinnaker/halyard/master/install/macos/InstallHalyard.sh
```

Install Halyard: 

```
sudo bash InstallHalyard.sh
```

If you get an error about not finding Java 1.8, manually set the environment:

```
export JAVA_HOME=/usr/libexec/java_home -v 1.8
export PATH=$PATH:$JAVA_HOME/bin
```

and pass the same environment to launch the installer:

```
sudo JAVA_HOME=`/usr/libexec/java_home -v 1.8` PATH=$PATH:$JAVA_HOME/bin bash InstallHalyard.sh
```

**Verify Installation**

```
hal -v
```

Move `hal` into your `$PATH`

## Create a Github repository

Later in this exercise you will be creating a Spinnaker pipeline that runs when code is pushed to a Github repository called spin-kub-repo.
 
Login to Github and fork the [lwander/spin-kub-demo](https://github.com/lwander/spin-kub-demo) repository to your Github account.  

> For directions on how to fork a repository, see https://help.github.com/articles/fork-a-repo/

## Create a link to your Github repository from Docker Hub

In this exercise you'll be using Docker's automated build service to create Docker images when code is pushed to your Github repository.  The first step is to link your Github repository from Docker Hub.  

1. Log into Docker Hub.
2. Navigate to **Your Login Name > Settings > Linked Accounts & Services**.
3. Click **Link Github**.
    The system prompts you to choose between Public and Private and Limited Access. Choose the **Public and Private** connection type since its required if you want to use the Automated Builds.
4. Press **Select** under Public and Private connection type.
    The system will prompt you to enter your Github credentials. 

![linked-account](https://github.com/jicowan/aws-microservices-deploy-options/blob/master/images/linked-acct.png)

After you grant access to you code repository, the system returns you to Docker Hub and the link is complete. 

## Create an automated build repository

Automated build repositories build an image when changes are pushed to your source code repository.  To create an automated build repository follow these instructions: 

1. Select **Create** > **Create Automated Build** (from the drop down, upper right) on [Docker Hub](https://hub.docker.com/).
2. Select **Github.**
    The system prompts you with a list of User/Organizations and code repositories.
3. Select your username from the User/Organizations list.
4. Select `spin-kub-demo` repository from the list.
5. Customize the automated build by pressing the **Click here to customize** this behavior link.
    1. Enter “Spin Kub Build Demo” for **Short Description**.
    2. Set the **Docker Tag value** for **Push Type: Branch** to latest
    3. Add a **Push Type: Tag** with the Docker Tag Name of 1.0
6. Click **Create**.
7. Click **Build Settings**.
8. Click **Trigger** to trigger the build manually.  

## Add docker registry provider

Enable the `docker-registry` provider:

```
hal config provider docker-registry enable
```

Add Docker Hub as the registry provider: 

```
hal config provider docker-registry account add dockerhub \
--address index.docker.io \
--repositories <your_dockerhub_username>/spin-kub-demo \
--username <your_dockerhub_username> \
--password
```

## Add a Kubernetes account

Enable the Kubernetes (legacy) provider, enter the following commands in a terminal: 

```
hal config provider kubernetes enable
```

Add the Kubernetes provider to your account:

```
hal config provider kubernetes account add eks-cluster \
--docker-registries dockerhub
```

### Configure kubernetes roles (rbac)

The following YAML creates the correct `ClusterRoleBinding` and `ServiceAccount`.

```
cat > spinnaker.yaml <<EOF
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
 name: spinnaker-robot
roleRef:
 apiGroup: rbac.authorization.k8s.io
 kind: ClusterRole
 name: cluster-admin
subjects:
- kind: ServiceAccount
  name: spinnaker-robot
  namespace: default 
---
apiVersion: v1
kind: ServiceAccount
metadata:
 name: spinnaker-robot
 namespace: default
EOF
```

Apply the manifest: 

```
kubectl apply -f spinnaker.yaml
```

### Extract  token from the secret associated with the Spinnaker service account: 

List the secrets with:

```
kubectl get secrets
```

The output should include a secret named similarly to **spinnaker-robot-token-xxxx**.  Copy that token name for use below.

Retrieve the token from the **spinnaker-robot-token-xxxx** using the following command:

```
kubectl get secret <secret name> -o jsonpath="{['data']['token']}" | base64 -D
```

Update `kubeconfig`:


> Create a backup of your kubeconfig, e.g. cp ~/.kube/config ~/.kube/config.old, before making the following modifications. 

```
users:
- name: aws
  user:
    token: <token_string> 
    exec:
      apiVersion: client.authentication.k8s.io/v1alpha1
      command: heptio-authenticator-aws
      args:
        - "token"
        - "-i"
        - "<eks_cluster_name>"
```

## Choose an environment

This tells Halyard what type of environment to install Spinnaker into.  For this exercise we'll be using the distributed installation on Kubernetes method.  This method deploys each of Spinnaker's microservices separately.  

Run the following command to add the environment: 

```
hal config deploy edit --type distributed --account-name eks-cluster
```

## Choose a storage service

Spinnaker can be configured to use S3 to store all its persistent data in a S3 bucket. 

To configure the S3 settings, run the following (notice, do not supply the value of `--secret-access-key` on the command line, you will be prompted to enter the value on STDIN once the command has started running):

```
hal config storage s3 edit \
--access-key-id <aws_access_key_id> \
--secret-access-key \
--region <aws_region> 
```

> This assumes that you are logged in as a IAM user with administrator/root privileges


Set the storage source to S3:

```
hal config storage edit --type s3
```

> If you get an S3 error such as “Access Denied”, insure that you have proper S3 bucket create and write access.

## Deploy

Now that we've finished setting the configuration parameters for Spinnaker, we're ready to deploy it and connect to it.  

### Pick a version

List the available versions:

```
hal version list
```

Set the version you want to use:

```
hal config version edit --version 1.8.0 
```

## Deploy Spinnaker

```
hal deploy apply
```

## Connect to the Spinnaker UI

Spinnaker will not be publicly reachable by default so you will need to run the following command to reach the UI on [localhost:9000](http://localhost:9000/):

```
hal deploy connect
```

## Create a Spinnaker pipline

For the rest of the exercise we will be following the steps in this tutorial, https://www.spinnaker.io/guides/tutorials/codelabs/kubernetes-source-to-prod/#1-create-a-spinnaker-application

