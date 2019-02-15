# -*- mode: ruby -*-
# vi: set ft=ruby :

VAGRANTFILE_API_VERSION = "2"

MY_VAGRANT_DOT_FILE = ENV['VAGRANT_DOTFILE_PATH'] || Dir.pwd + '/.vagrant'

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|

  config.vm.box = "ubuntu/trusty64"
  memory_mb = 1024

# First, install python
  config.vm.provision "shell" do |s|
    s.inline = "apt-get install -y python"
  end
  

  # If adding a new host add configuration also to init_network.yml
  cluster = {
    'zookeeper-node-1'        => "192.168.5.100",
    'zookeeper-node-2'        => "192.168.5.101",
    'zookeeper-node-3'        => "192.168.5.102",
    'kafka-node-1'            => "192.168.5.110",
    'kafka-node-2'            => "192.168.5.111",
    'kafka-node-3'            => "192.168.5.112",
    'storm-nimbus-node-1'     => "192.168.5.120",
    'storm-supervisor-node-1' => "192.168.5.130",
  }

  cluster.each_with_index do |(short_name, ip), idx|

    config.vm.define short_name do |host|

      host.vm.network :private_network, ip: ip
      host.vm.hostname = short_name
      host.vm.provider :virtualbox do |vb|
        vb.customize ["modifyvm", :id, "--memory", memory_mb]
      end

      host.vm.provision :ansible do |ansible|
        ansible.extra_vars = {
          cluster_node_seq: idx + 1,
#          cluster_ip_addresses: cluster.values,
          cluster_ip_addresses: ["192.168.5.100", "192.168.5.101", "192.168.5.102"],
          machine_ip: ip
        }
        if short_name.include? "storm-nimbus"
            ansible.playbook = "storm-nimbus-provision.yml"
        elsif short_name.include? "storm-supervisor"
            ansible.playbook = "storm-supervisor-provision.yml"
        elsif short_name.include? "zookeeper"
            ansible.playbook = "zookeeper-provision.yml"
        else short_name.include? "kafka"
            ansible.playbook = "kafka-provision.yml"
        end
      end
    end
  end
end
