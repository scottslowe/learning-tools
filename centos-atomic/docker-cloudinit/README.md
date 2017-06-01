# Using cloud-init to Customize Docker on CentOS Atomic Host

These files show an example of how to use `cloud-init` on a public cloud provider (AWS is used in this example) to customize the configuration and behavior of the Docker daemon on a CentOS Atomic Host instance.

## Contents

* **cloud-config.yml**: This is the `cloud-init` configuration file that does the configuration of the Docker daemon on the instance. No modifications to this file should be necessary.

* **launch.sh**: This Bash shell script uses the AWS CLI to gather information from AWS and then launch an instance in your default VPC.

* **README.md**: The file you're currently reading.

* **ssh.cfg**: A custom SSH configuration file; not essential for using this environment.

## Prerequisites

Before you can use this environment, there are a few things you'll need to do:

1. You'll need to install **and** configure the AWS CLI. The launch script provided in this environment assumes that the AWS CLI is installed, configured, and working as expected.

2. In your default VPC, you'll need to either a) modify the default security group to allow inbound SSH; or b) create a security group called "default" that allows inbound SSH. If you prefer to use a name other than "default", you'll need to modify `launch.sh` with the updated name of the security group to use.

3. You'll need to have a working SSH keypair in AWS.

## Instructions

1. If you are using a security group other than one named "default" (as described in the "Prerequisites" section), edit `launch.sh` and modify the command that looks up the security group ID accordingly.

2. Edit `launch.sh` to specify the correct AWS keypair to use when launching the instance.

3. Launch the instance using `./launch.sh`. This launch script assumes that the AWS CLI is working, and that the `cloud-config.yml` file is in the same directory.

4. Using the AWS CLI or the AWS Console, determine the public IP address assigned to the instance you just created.

5. Use SSH to connect to the instance (use the username "centos" to connect). Once logged into the instance, use `systemctl status docker.service` to verify that the Docker daemon is running and that the systemd drop-in located in `/etc/systemd/system/docker.service.d` has been loaded.

6. While logged into the instance, use `ss -lnt` to show that a process (the Docker daemon) is listening on TCP port 2375.

7. Verify the Docker daemon is working across the network by running `sudo docker -H tcp://127.0.0.1:2375 ps`.

Enjoy!

## License

This content is licensed under the MIT License.
