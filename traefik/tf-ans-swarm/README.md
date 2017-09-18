# Using Traefik with Docker Swarm on Ubuntu on AWS

These files help establish a Docker Swarm mode cluster on which you can deploy Traefik ([https://traefik.io](https://traefik.io)), a dynamic reverse proxy, to help direct traffic for services deployed on the Swarm cluster. The cluster is deployed on AWS using an Ubuntu-based AMI.

## Prerequisites

This demo environment assumes that you have the following software already installed on your system:

* Terraform 0.9.x (tested with 0.9.11, should work with any 0.9.x version)
* Ansible 2.x (tested with 2.3.2.0)

Before trying to use this demo environment, please ensure that you've installed the necessary prerequisites.

This demo environment was tested on macOS 10.12 "Sierra", but it should work on any recent macOS release or recent Linux distribution. I don't know what would be required to make it work on Windows, sorry.

## Contents

* **ansible.cfg**: This file configures Ansible to connect to the AWS instances. You'll need to edit this file to specify the correct SSH private key to use (should match the key specified in `terraform.tfvars`; see the Instructions).

* **compute.tf**: This Terraform configuration launches the Ubuntu-based instances used for the Docker Swarm setup.

* **create-swarm.yml**: This is the Ansible playbook that configures the Ubuntu-based AWS instances into a Docker Swarm mode cluster.

* **data.tf**: This Terraform configuration file provides information to Terraform on which AWS AMIs to use.s

* **ec2.ini**: This file configures the Ansible dynamic inventory script. If you're using an AWS region _other_ than "us-west-2", you'll want to edit this script appropriately.

* **ec2.py**: This Python script is a dynamic inventory script for Ansible.

* **networking.tf**: This Terraform configuration file creates all the networking constructs needed for the environment (VPC, Internet gateway, subnet, gateway attachment, route table, and route table associations).

* **output.tf**: This Terraform configuration outputs information about the resources created (the public IP addresses of the instances, specifically).

* **provider.tf**: This Terraform configuration files configures the AWS provider.

* **README.md**: The file you're currently reading.

* **security.tf**: This Terraform configuration creates the security groups used by the instances.

* **variables.tf**: This Terraform configuration file specifies variables that Terraform will need in order to create the desired AWS infrastructure.

## Instructions

As mentioned in "Prerequisites" above, these instructions assume that you have a working Terraform installation capable of working with AWS.

1. Place the files from the `traefik/tf-ans-swarm` directory of the "learning-tools" GitHub repository into a direcotry on your local system. You can clone the entire repository (using `git clone`) or just download the specific files from the `traefik/tf-ans-swarm` directory.

2. Create a file named `terraform.tfvars` and populate it with the name of the AWS keypair you'd like to use for the instances, the AWS region to use, the type of the instances (such as "t2.micro"), and the number of worker nodes you'd like created. Refer to [this URL](https://www.terraform.io/intro/getting-started/variables.html) for specific details on the syntax of this file. You can refer to the contents of `variables.tf` for the names of the variables that need to be defined.

3. Run `terraform validate` to ensure there are no errors in the Terraform configuration. (If using Terraform 0.10.x, you will also need to run `terraform init` first; however, I haven't tested this environment with 0.10.x yet).

4. Run `terraform plan` to have Terraform examine the current infrastructure and determine what changes are necessary to realize the desired configuration.

5. Run `terraform apply` to have Terraform make the changes necessary to realize the desired configuration. When this command has completed, it will output the public IP addresses assigned to the created instances.

6. Run `ansible-playbook create-swarm.yml` to have Ansible configure the AWS instances created by Terraform into a Docker Swarm mode cluster.

7. All remaining steps should be run while connected to the "manager" instance via SSH. First, create an overlay network:

        docker network create --driver=overlay demo-net

8. Next, create a service (constrained to the manager node) to run the Traefik reverse proxy:

        docker service create --name traefik \
        --constraint 'node.role==manager' \
        --publish 80:80 --publish 8080:8080 \
        --mount type=bind,source=/var/run/docker.sock,target=/var/run/docker.sock \
        --network demo-net
        traefik --web --docker --docker.watch \
        --docker.swarmmode --docker.domain=docker.local

    If you'd like additional logging, add `--logLevel=DEBUG` to the above command.

9. Deploy a web service to be used behind Traefik:

        docker service create --name web \
        --label 'traefik.port=5000' \
        --network demo-net slowe/flask-demo-app:1.0

10. Run this command against the IP address of the manager and note the output:

        curl -H "Host:web.docker.local" http://<manager_ip_address>

    If you used a name other than "web" in step 11, replace that name in the "Host" portion of the above command.

11. Run `docker service scale web=3` to scale up the "web" service (replace "web" with whatever name you used in step 9). Repeat step 10 and note that Traefik will load balance across the different containers hosting the service.

12. Deploy a new web service:

        docker service create --name api \
        --label 'traefik.port=5000' \
        --network demo-net slowe/flask-demo-app:1.0

13. Run this command to access the "api" service behind Traefik:

        curl -H "Host:api.docker.local" http://<manager_ip_address>

    If you used a name other than "api", use that name in the above command.

14. Note that Traefik is taking inbound traffic to the manager and correctly routing it to the appropriate backend container based on the "Host" header. Feel free to deploy additional services with different names to see Traefik in action.

## License

This content is licensed under the MIT License.
