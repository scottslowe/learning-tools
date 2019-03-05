# Skaffold

Skaffold is a command line utility that facilitates continuous development for Kubernetes applications. With Skaffold, you can iterate on your application source code locally then deploy it to a remote Kubernetes clusters.  Skaffold handles the workflow for building, pushing, and deploying your application.  It can also be used in an automated context such as a CI/CD pipeline to leverage the same workflow and tooling when moving applications to production.

## Operating modes

Skaffold has 2 operating modes: `skaffold dev` and `skaffold run`.

### skaffold dev

Updates your deployed application continually:

* Watches your source code and the dependencies of your docker images for changes and runs a build and deploy when changes are detected
* Streams the logs from deployed containers to your terminal
* Creates a continuous build-deploy loop that only warn on errors

### skaffold run

Runs a Skaffold pipeline once, exits on any errors in the pipeline. Use for:

* Continuous integration or continuous deployment pipelines
* Sanity checking after iterating on your application

## Pluggability

Skaffold has a pluggable architecture that allows you to choose which tools you want to use.  In the following exercise, you will build the container locally using the Docker daemon, push it to an ECR registry, and deploy it to EKS by applying a Kubernetes manifest. 

![plugabbility](https://github.com/jicowan/aws-microservices-deploy-options/blob/master/images/skaffold.jpg)

## Prerequisites

### Install ECR Credential Helper

The ECR credential helper makes it easier to use ECR by seamless passing your AWS credentials to the service.  When you use the credential helper there is no need to use `docker login` or `docker logout`.  

> If you're planning to use the credential helper with an assumed role, you'll need to set the environment variable `AWS_SDK_LOAD_CONFIG=true`.  The IAM principle you're using has to have permission to ECR too. 

Install the credential helper with  `go get`:

```
go get -u github.com/awslabs/amazon-ecr-credential-helper/ecr-login/cli/docker-credential-ecr-login
```

Place the `docker-credential-ecr-login` binary on your `PATH` and add the following to the contents of your `~/.docker/config.json` file: 

```
{
    "credHelpers": {
        "[account_number].dkr.ecr.[region].amazonaws.com": "ecr-login" 
        }
}
```

This configures the daemon to use the credential helper for a specific ECR registry. 

> Configuring a specific registry will only work with Docker v1.13.0 or higher.

### Provision an EKS Cluster

Download and install the latest release of [eksctl](https://eksctl.io/) from Weave. 

```
curl --silent --location "https://github.com/weaveworks/eksctl/releases/download/latest_release/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp
sudo mv /tmp/eksctl /usr/local/bin
```

To create a basic cluster, run: 

```
eksctl create cluster
```

This will create a cluster in a new VPC in the `us-west-2` region with the following defaults: 

* Two (2) `m5.large` nodes
* Nodes are built from EKS optimized AMI

### Create an ECR registry

```
REPOSITORY_URI=$(aws ecr create-repository --repository-name k8s-skaffold/skaffold-example | jq -r 'repository.repositoryUri')  
```

## Install Skaffold

Download the latest OSX build: 

```
curl -Lo skaffold https://storage.googleapis.com/skaffold/releases/latest/skaffold-darwin-amd64 \
&& chmod +x skaffold && sudo mv skaffold /usr/local/bin
```

## Getting Started

Clone this repository to get access to the examples: 

```
git clone https://github.com/GoogleContainerTools/skaffold
```

Change directories to the `getting-started` example:

```
cd examples/getting-started
```

Create a new skaffold.yaml file: 

```
cat > skaffold.yaml <<EOF
apiVersion: skaffold/v1alpha2
kind: Config
build:
  tagPolicy:
    envTemplate:
      template: "{{.IMAGE_NAME}}:{{.DIGEST_HEX}}"
  artifacts:
  - imageName: ${REPOSITORY_URI}
  local:
deploy:
  kubectl:
    manifests:
      - k8s-*
EOF
```

Create a new k8s-pod.yaml file

```
cat > k8s-pod.yaml <<EOF
apiVersion: v1
kind: Pod
metadata:
  name: getting-started
spec:
  containers:
  - name: getting-started
    image: ${REPOSITORY_URI} 
EOF
```

Run `skaffold dev`

```
$ skaffold dev
Starting build...
Sending build context to Docker daemon   5.12kB
Step 1/6 : FROM golang:1.10.1-alpine3.7 as builder
 ---> 52d894fca6d4
Step 2/6 : COPY main.go .
 ---> Using cache
 ---> 9ef81ba62a5b
Step 3/6 : RUN go build -o /app main.go
 ---> Using cache
 ---> 99f0814404c4
Step 4/6 : FROM alpine:3.7
 ---> 3fd9065eaf02
Step 5/6 : CMD ["./app"]
 ---> Using cache
 ---> cc55203a82d0
Step 6/6 : COPY --from=builder /app .
 ---> Using cache
 ---> e846c93af382
Successfully built e846c93af382
Successfully tagged f4562381e3b899a804bb2ec1651fc263:latest
Successfully tagged 820537372947.dkr.ecr.us-west-2.amazonaws.com/k8s-skaffold/skaffold-example:e846c93af382d92e1ff42f7fc0eb1d52416af4de9f1b1f31ec6b7248aca9f386
The push refers to repository [820537372947.dkr.ecr.us-west-2.amazonaws.com/k8s-skaffold/skaffold-example]
8fefffdd1dad: Preparing
cd7100a72410: Preparing
8fefffdd1dad: Layer already exists
cd7100a72410: Layer already exists
e846c93af382d92e1ff42f7fc0eb1d52416af4de9f1b1f31ec6b7248aca9f386: digest: sha256:016b50f011a53ee5b454a19a3db32c2b62bb05bf1128f1c1f1bd820c55fdf76f size: 738
Build complete in 2.496637932s
Starting deploy...
pod "getting-started" created
Deploy complete in 3.48059458s
Watching for changes...
[getting-started] Hello world! 
```

Using your favorite text editor, make a change to `main.go`.  

```
diff --git a/examples/getting-started/main.go b/examples/getting-started/main.go
index 64b7bdfc..f95e053d 100644
--- a/examples/getting-started/main.go
+++ b/examples/getting-started/main.go
**@@ -7,7 +7,7 @@** import (

 func main() {
        for {
-               fmt.Println("Hello world!")
+               fmt.Println("Hello <your_name>!")
                time.Sleep(time.Second * 1)
        }
 }
```

Once you save the file, you should see the pipeline kick off again to redeploy your application:

```
[getting-started] Hello <your_name>!
[getting-started] Hello <your_name>!
```

## Helm Deployments with Skaffold

Change directories to the `helm-deployment` example: 

```
cd examples/helm-deployment 
```

Create a new ECR repository:

```
REPOSITORY_URI=$(aws ecr create-repository --repository-name k8s-skaffold/skaffold-helm | jq -r 'repository.repositoryUri')  
```

Create a new `skaffold.yaml` file:

```
cat > skaffold.yaml <<EOF
apiVersion: skaffold/v1alpha2
kind: Config
build:
  tagPolicy:
    sha256: {}
  artifacts:
  - imageName: ${REPOSITORY_URI}
deploy:
  helm:
    releases:
    - name: skaffold-helm
      chartPath: skaffold-helm
      namespace: skaffold
      #wait: true
      #valuesFilePath: helm-skaffold-values.yaml
      values:
        image: ${REPOSITORY_URI}
      #overrides builds an override values.yaml file to run with the helm deploy
      #overrides:
      # some:
      #   key: someValue
      #setValues get appended to the helm deploy with --set.
      #setValues:
        #some.key: someValue
EOF
```

Run `skaffold dev`

```
$ skaffold dev
Starting build...
Sending build context to Docker daemon  3.072kB
Step 1/1 : FROM nginx:stable
 ---> f759510436c8
Successfully built f759510436c8
Successfully tagged 50dfbd2b5121591bd746a80e7324cf59:latest
Successfully tagged 820537372947.dkr.ecr.us-west-2.amazonaws.com/k8s-skaffold/skaffold-helm:f759510436c8fd6f7ffa13dd9e9d85e64bec8d2bfd12c5aa3fb9af1288eccdab
The push refers to repository [820537372947.dkr.ecr.us-west-2.amazonaws.com/k8s-skaffold/skaffold-helm]
b3fb485368bf: Preparing
6f67560e4591: Preparing
d626a8ad97a1: Preparing
b3fb485368bf: Pushed
d626a8ad97a1: Pushed
6f67560e4591: Pushed
f759510436c8fd6f7ffa13dd9e9d85e64bec8d2bfd12c5aa3fb9af1288eccdab: digest: sha256:0bf115168ab84ca0ddd41724e78db21c2e7daecca35bae1ff59e644d4b558e8b size: 948
Build complete in 21.827191436s
Starting deploy...
Error: release: "skaffold-helm" not found
Helm release skaffold-helm not installed. Installing...
No requirements found in skaffold-helm/charts.
NAME:   skaffold-helm
LAST DEPLOYED: Thu Jun 21 10:59:09 2018
NAMESPACE: skaffold
STATUS: DEPLOYED

RESOURCES:
==> v1/Service
NAME                         TYPE       CLUSTER-IP     EXTERNAL-IP  PORT(S)  AGE
skaffold-helm-skaffold-helm  ClusterIP  10.100.18.153  <none>       80/TCP   0s

==> v1beta1/Deployment
NAME           DESIRED  CURRENT  UP-TO-DATE  AVAILABLE  AGE
skaffold-helm  1        1        1           0          0s

==> v1/Pod(related)
NAME                            READY  STATUS             RESTARTS  AGE
skaffold-helm-5898d5dd7c-vfp66  0/1    ContainerCreating  0         0s


Deploy complete in 3.608035399s
Watching for changes...
```


