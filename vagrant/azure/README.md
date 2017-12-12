# Using Vagrant with Azure (Single Instance)

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) with Azure, where the VMs managed by Vagrant are actually Azure VMs. Using these files, Vagrant can only operate against a single instance at a time. This configuration was tested using Vagrant 1.9.8, version 2.0.0 of [the vagrant-azure plugin](https://github.com/azure/vagrant-azure), and Microsoft Azure.

## Contents

* **instances.yml**: This YAML file contains the instance-specific configuration information. Four values are expected in this file: `group` (the Azure resource group Vagrant should use; this group should not already exist), `image` (the Azure VM image to use), `name` (the name to be assigned to the Azure VM), and `size` (the VM size to use).

* **README.md**: The file you're currently reading.

* **Vagrantfile**: This file is used by Vagrant to spin up the Azure VM. There are two changes that **must** be made to this file in order for it to function properly: you must specify the correct path to your SSH private key, and you must supply the correct name to the dummy box installed for use with Azure.

## Instructions

These instructions assume that you have an Azure subscription, that you know your Azure subscription ID (which can be obtained by running `az account show --query '[?isDefault].id' -o tsv`), and that you have a valid SSH key pair to use to log in to the Linux-based Azure VM.

1. Vagrant requires that a "dummy box" be installed for use with Azure. Run this command to install the dummy box:

        vagrant box add <box-name> https://github.com/azure/vagrant-azure/raw/v2.0/dummy.box --provider azure

2. Install the Vagrant Azure provider by running `vagrant plugin install vagrant-azure`.

3. _(One time only)_ Create an Azure Active Directory service principal for Vagrant to use when connecting to Azure. You can do this with the command `az ad sp create-for-rbac`. Record and keep the JSON output; you'll need it later.

4. Place the files from the `vagrant-azure` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`), download a ZIP file of the entire "learning-tools" repository, or just download the specific files from the the `vagrant-azure` folder.

5. Edit `instances.yml` to supply the correct information to be used by Vagrant when launching a VM on Azure.

6. In a terminal window, set some environment variables:

    For AZURE_TENANT_ID, use the "tenant" value from the JSON output of step 3.

    For AZURE_CLIENT_ID, use the "appID" value from the JSON output of step 3.

    For AZURE_CLIENT_SECRET, use the "password" value from the JSON output of step 3.

    For AZURE_SUBSCRIPTION_ID, use your Azure subscription ID.

7. In the directory where you placed the files from this GitHub repository, run `vagrant up` to have Vagrant authenticate to Azure and launch the desired VM for you. Once the VM is created and running, you can use `vagrant ssh` to connect to the instance, and `vagrant destroy` will terminate (destroy) the Azure VM for you. (You can follow all these actions in the Azure Portal, if you're interested.)

Enjoy!

## Additional Notes

This environment will only create a single instance on Azure using Vagrant. An environment for spinning up multiple instances is planned for the near future.
