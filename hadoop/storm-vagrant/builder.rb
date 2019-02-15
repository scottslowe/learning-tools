# -*- mode: ruby -*-
# vi: set ft=ruby :

require './machineconfig'
require './syncedfolder'

$counts = {}

def build(config, provision)
  $provider = provision['provider']
  $gitssh = provision['gitssh']
  $share = provision['share']
  $ssh = provision['ssh']
  $os = provision['os']

  if $provider == 'hyperv'
    config.vm.network :public_network, bridge: 'Internet'
    config.vm.synced_folder '.', '/vagrant', type: 'smb', mount_options: ['vers=3.0'], smb_username: ENV['HOST_USERNAME'], smb_password: ENV['HOST_PASSWORD']
  else
    config.vm.network :private_network, type: :dhcp
  end
  
  config.vm.boot_timeout = provision['timeout']
  # config.vbguest.auto_update = false
  provision['machines'].each_with_index do |machine, index|
    name = name(machine)
    syncs = syncs(machine, name)
    config.vm.define name do |node|
      node.vm.hostname = name
      script(node, machine['type'])
      network(node, machine)
      ssh(node, index)
      if $gitssh
        gitssh(node)
      end
      provision(node, MachineConfig.new(machine['type'], name, machine['box'], machine['memory'], machine['cpus'], syncs))
    end
  end
end

def name(machine)
  $counts[machine['type']] = $counts.key?(machine['type']) ? $counts[machine['type']] + 1 : 0
  machine['type'] + '-' + $counts[machine['type']].to_s
end

def syncs(machine, name)
  syncs = []
  machine['syncs'].each do |sync|
    syncs.push(SyncedFolder.new(name, "#{$share}/" + name, sync['dest'] + "/#{$share}", sync['mount_options']))
  end
  syncs
end

def script(node, type)
  node.vm.provision :shell, path: "scripts/#{$os}/common.sh"
  if type == 'broker'
    node.vm.provision :shell, path: "scripts/#{$os}/karaf.sh"
    node.vm.provision :shell, path: "scripts/#{$os}/karaf-activemq.sh"
    node.vm.provision :shell, path: "scripts/#{$os}/karaf-hawtio.sh"
  elsif type == 'nimbus'
    node.vm.provision 'file', source: 'config/storm.yaml', destination: '/home/vagrant/apache-storm/conf/storm.yaml'
    node.vm.provision 'file', source: 'config/nimbus.conf', destination: '/home/vagrant/supervisor/conf/nimbus.conf'
    node.vm.provision :shell, path: "scripts/#{$os}/storm.sh"
    node.vm.provision :shell, path: "scripts/#{$os}/nimbus.sh"
  elsif type == 'supervisor'
    node.vm.provision 'file', source: 'config/storm.yaml', destination: '/home/vagrant/apache-storm/conf/storm.yaml'
    node.vm.provision 'file', source: 'config/supervisor.conf', destination: '/home/vagrant/supervisor/conf/supervisor.conf'
    node.vm.provision :shell, path: "scripts/#{$os}/storm.sh"
    node.vm.provision :shell, path: "scripts/#{$os}/supervisor.sh"
  elsif type == 'webapp'
  elsif type == 'zookeeper'
    node.vm.provision :shell, path: "scripts/#{$os}/zookeeper.sh"
  else
    puts 'Unrecognized machine type!'
  end
end

def network(node, machine)
  if $provider != 'hyperv'
    node.vm.network :private_network
  end
  ports(node, machine['ports'])
end

def ports(node, ports)
  ports.each { |port| port(node, port['guest'], port['host']) }
end

def port(node, guest, host, protocol = 'tcp')
  node.vm.network :forwarded_port, guest: guest, host: host, protocol: protocol
end

def ssh(node, index)
  node.vm.network :forwarded_port, guest: 22, host: ($ssh + index), id: 'ssh'
end

def provision(node, machine)
  setup(node, machine)
  machine.syncs.each { |sync| sync(node, sync) }
end

def setup(node, machine)
  node.vm.box = machine.box
  node.vm.hostname = machine.name
  node.vm.provider $provider do |vm|
    if $provider == 'hyperv'
      vm.vmname = machine.name
    else
      machine.syncs.each_with_index do |sync, index|
        vm.customize ['setextradata', :id, "VBoxInternal2/SharedFoldersEnableSymlinksCreate/#{sync.name}", index.to_s]
      end
      vm.name = machine.name
    end
    vm.memory = machine.memory
    vm.cpus = machine.cpus
  end
end

def gitssh(node)
  node.vm.provision 'file', source: ENV['SSH_ID_RSA'], destination: '/home/vagrant/.ssh/git_id_rsa'
end

def sync(node, sync)
  if $provider == 'hyperv'
    node.vm.synced_folder sync.src.to_s, sync.dest.to_s, id: sync.name, create: true, type: 'smb', mount_options: ['vers=3.0'], smb_username: ENV['HOST_USERNAME'], smb_password: ENV['HOST_PASSWORD']
  else
    node.vm.synced_folder sync.src.to_s, sync.dest.to_s, id: sync.name, create: true, mount_options: sync.mount_options
  end
  cleanup(node)
end

def cleanup(node)
  if $provider == 'hyperv'
    node.trigger.after :destroy, execute: "rd /s /q #{$share}", stdout: true
  else
    node.trigger.after :destroy, execute: "rm -rf #{$share}", stdout: true
  end
end
