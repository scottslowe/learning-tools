# Alpine-Docker-Kubernetes Vagrant Image

A builder for creating a virtualbox vagrant box with the latest Alpine operating system, docker and the latest available kubernetes binaries compiled for the muscl c library.

Features: -
* The latest alpine extended installer is used so that the alpine installation which is sensitive to time succeeds - you may with to try a more minmal installation and pull all packages from the network depending on the quality of your connection.
* build_image.sh controls the build and publishing process.  It won't build or compile unless there are new versions available
 * \-\-atlas will include uploading the built image to Hashicorp Atlas
 * \-\-force will force a rebuild of the vagrant image and upload to atlas.
 * You must set ATLAS_USER and ATLAS_TOKEN variables in order to upload to Atlas. 
* The image creation is split into two stages: -
 1) Lookup versions and build Alpine Kubernetes hyperkube binary and CNI
 2) Run packer to create the Alpine image run provisioning scripts which add docker and our kubernetes binaries.
* The kubernetes compilation requires go, alpine muscl libs and some extra packages and it was easiest to use the official golang:alpine docker image as a base.
 * You must give your user access to docker in order to perform the build (don't run as root).

Requirements: -

VirtualBox
vagrant
packer
docker (user access)
Atlas account (optional)

## Build notes

Build creates a virtualbox vagrant image and uploads to Atlas by default.
You must provide your atlas credentials to build, run: -

```
ATLAS_USER=dmcc ATLAS_TOKEN=xyz123etc123 ./build_image.sh --atlas [--force]
```

The build_image.sh script will automatically lookup the latest versions of:-

* Alpine Linux
* Kubernetes
* Kubeadm

It will name the box alpine-_version_-docker-_version_-kubernetes-_version_
If a box with the same versions exists the build will abort unless you specify the --force option 
which will cause it to remove the existing box and build it again.

Virtualbox Guest Additions do not build/install on v3.4 of Alpine.

* private network needs be configured as static in Vagrantfile in order to use folder sharing. If it is set to DHCP, Virtualbox will not see the address assigned to the interface, therefore, Vagrant will not be able to retrieve it to configure NFS.
* folder sharing should be configured to use NFS in Vagrantfile.
* `bash` is installed by default so `config.ssh.shell="/bin/sh"` is not necessary.

## Thanks

This image is based off of https://github.com/maier/vagrant-alpine/ which already took care of all the hard work installing alpine.  I just added the extra stuff to automatically lookup latest versions, install docker and kubernetes binaries.


Dave
