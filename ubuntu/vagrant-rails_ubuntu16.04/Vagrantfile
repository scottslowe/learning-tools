require 'yaml'

# Loading external Configuration
cfg = YAML.load_file 'config.yml'

# Configuring the Box
Vagrant.configure("2") do |config|

  # Using Ubuntu 16.04 LTS
  config.vm.box = "ubuntu/xenial64"

  # Configuring Network
  config.vm.network "private_network", ip: cfg['vm']['ip']

  # GIT Config
  config.ssh.forward_agent = true
  config.vm.provision "file", source: "~/.gitconfig", destination: ".gitconfig"

  # Binding des différents ports
  cfg['ports'].each do |idx, item|
    config.vm.network :forwarded_port, guest: item['guest'], host: item['host'], auto_correct: true
  end

  # Lien entre les systèmes de fichier
  config.vm.synced_folder cfg['files']['host'], cfg['files']['guest'],  "nfs" => { :mount_options => ['dmode=777', 'fmode=777'] }
  
  # Configuration du proxy
  if (cfg['proxy']['active']) 
    config.proxy.http     = "http://#{cfg['proxy']['chain']}"
    config.proxy.https    = "https://#{cfg['proxy']['chain']}"
    config.proxy.no_proxy = cfg['proxy']['except']
  end
  
  # Configure Box Ressources
  config.vm.provider "virtualbox" do |v|
    v.name = cfg['vm']['name']
    v.customize ["modifyvm", :id, "--memory", cfg['vm']['memory']]
  end

  # Provisioning System
  config.vm.provision "system",
    type: "shell",
    preserve_order: true,
    path: "provision/system.sh"
  

  # Provisioning Nginx PHP
  config.vm.provision "php71-nginx",
    type: "shell",
    preserve_order: true,
    path: "provision/php71-nginx.sh"
  

  # Provisioning Passenger
  config.vm.provision "passenger",
    type: "shell",
    preserve_order: true,
    path: "provision/passenger.sh"
  
  # TODO: Provision NGinx Vhost & conf
  config.vm.provision "file", source: "~/.gitconfig", destination: ".gitconfig"

  # Provisioning ZSH
  config.vm.provision "zsh",
    type: "shell",
    preserve_order: true,
    path: "provision/zsh.sh",
    privileged: false
  

  # Changing Ubuntu Shell to ZSH
  config.vm.provision :shell, inline: "sudo chsh -s /bin/zsh ubuntu"

  # Provisioning rbEnv
  config.vm.provision "rbenv",
    type: "shell",
    preserve_order: true,
    path: "provision/rbenv.sh",
    privileged: false
  

end