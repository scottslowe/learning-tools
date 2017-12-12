# -*- mode: ruby -*-
# vi: set ft=ruby :

# Require the Azure provider plugin and YAML module
require 'vagrant-azure'
require 'yaml'

# Read YAML file with instance information
instances = YAML.load_file(File.join(File.dirname(__FILE__), 'instances.yml'))

# Specify Vagrant version and Vagrant API version
Vagrant.require_version '>= 1.6.0'
VAGRANTFILE_API_VERSION = '2'
ENV['VAGRANT_DEFAULT_PROVIDER'] = 'azure'

# Create and configure the Azure instance(s)
Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|

  # Use dummy Azure box
  config.vm.box = 'azure-dummy'

  # Specify SSH key to use
  config.ssh.private_key_path = '~/.ssh/id_rsa'

  # Configure the Azure provider
  config.vm.provider 'azure' do |az, override|
    # Pull Azure AD service principal information from environment variables
    az.tenant_id = ENV['AZURE_TENANT_ID']
    az.client_id = ENV['AZURE_CLIENT_ID']
    az.client_secret = ENV['AZURE_CLIENT_SECRET']
    az.subscription_id = ENV['AZURE_SUBSCRIPTION_ID']

    # Specify VM parameters
    az.vm_name = instances['name']
    az.vm_size = instances['size']
    az.admin_username = instances['user']
    az.vm_image_urn = instances['image']
    az.resource_group_name = instances['group']
  end # config.vm.provider 'azure'
end # Vagrant.configure
