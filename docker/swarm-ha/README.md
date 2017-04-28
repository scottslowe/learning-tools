# Using Docker Swarm with Multiple Swarm Managers

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) to build an environment for working with Consul, Docker, Docker Swarm, and multiple Swarm manager instances. This configuration was tested using Vagrant 1.8.1, VMware Fusion 8.1.0, and version 4.0.5 of the Vagrant VMware plugin.

## Contents

* **config.json.erb**: This is an ERB template used by Vagrant (and leveraging the information provided in `machines.yml`) to create VM-specific Consul configuration files. When you run `vagrant up` or `vagrant status`, this template will be used to create three VM-specific configuration files, each named "hostname.config.json", where "hostname" is the VM name specified in `machines.yml`. No edits to this file are needed.

* **consul.conf**: This is an Ubuntu Upstart script for Consul. No changes or edits to this file are needed.

* **consul.sh**: This Bash shell script is used as a provisioning tool by Vagrant when setting up the Consul VMs. No changes to this file are needed.

* **machines.yml**: This is a YAML file containing the configuration data used by Vagrant when creating and provisioning VMs. This Vagrant environment expects six (6) values in this file for each VM: name, Vagrant box, desired RAM, desired vCPUs, private IP address, and role (currently set to either "consul" or "docker"). At a minimum, you'll need to edit this file to specify the correct Vagrant box. Any other changes to this file are optional. Generally, the only other change that might be desired is to change the private IP addresses given to the VMs.

* **README.md**: The file you're currently reading.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines for this environment. No changes need to be made to this document, as all the configuration data is found in other files (like `machines.yml`). However, if you are using a virtualization solution _other_ than VMware Fusion, you might need to make changes to this file.

## Instructions

These instructions assume you've already installed VMware Fusion, Vagrant, and the Vagrant VMware plugin. Please refer to the documentation for those products for more information on installation or configuration. Note that Internet access is required when using `vagrant up` to create this environment.

1. Use `vagrant box add` to install an Ubuntu 14.04 x86_64 Vagrant box. For a VMware-formatted box, the "bento/ubuntu-14.04" box is a good option. For VirtualBox, the "ubuntu/trusty64" box will work fine.

2. Place the files from the `docker-swarm-ha` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`) or just download the specific files from the the `docker-swarm-ha` folder.

3. If necessary, edit `machines.yml` to specify the name of the Vagrant box downloaded in step 1. You may also make changes to the private IP addresses or the RAM/CPU settings, although no changes are required.

4. Run `vagrant up` to instantiate the learning environment. This will spin up six (6) VMs based on the Ubuntu 14.04 base box you downloaded in step 1 and specified in `machines.yml` in step 3. Vagrant will also appropriately configure each VM and start the necessary services. Depending on the speed of your system and your Internet connection, this may take a few minutes.

5. Use `vagrant ssh docker-01` to connect to the first Docker host and join the Docker Engine to a Swarm cluster:

        docker run -d swarm join --addr=192.168.100.104:2375 consul://192.168.100.101:8500/swarm

    If you changed the private IP addresses in `machines.yml`, be sure to supply the appropriate IP addresses from that file in the command above.

6. Repeat step #5 for the second and third Docker VMs, substituting the correct IP address each time. This means that the `--addr=` parameter changes, but the `consul://...` URL does _not_ change.

7. Log back into the first Docker VM to start the first Docker Swarm manager:

        docker run -d -p 3375:2375 swarm manage --replication \
        --advertise 192.168.100.104:3375 consul://192.168.100.101:8500/swarm

    You can use a port other than 3375 for the `-p` parameter, just be sure to match the specified port in the `--advertise` flag.

8. Repeat step #7 on the second and third Docker VMs, changing the IP address specified for the `--advertise` parameter appropriately for each VM. Note that the URL for Consul does _not_ change.

At this point you should have a working Docker Swarm cluster, backed by Consul, with one primary manager and two replica managers. To verify operation, run this command from one of the three Docker VMs:

    docker -H tcp://192.168.100.104:3375 info

This command should return information indicating that there are three Docker Engines in the Swarm cluster.

Enjoy!

## Troubleshooting

* Verify that the Consul cluster is operating properly by querying Consul's HTTP API using `curl`. The following command should return a JSON-formatted list of the Consul cluster nodes:

        curl -X GET http://192.168.100.101:8500/v1/catalog/nodes

* After running the `docker run ... swarm join` commands, verify that the nodes are registering in Consul by examing the logs from the container. The output from the following command should include text that indicates the container is registering with the discovery service:

        docker logs <docker container ID>

## More Information

Refer to the Docker documentation for a [manual Swarm install](https://docs.docker.com/swarm/install-manual/) and [working with multiple managers](https://docs.docker.com/swarm/multi-manager-setup/).
