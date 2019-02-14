# -*- mode: ruby -*-
# vi: set ft=ruby :

# All Vagrant configuration is done below. The "2" in Vagrant.configure
# configures the configuration version (we support older styles for
# backwards compatibility). Please don't change it unless you know what
# you're doing.
VAGRANT_COMMAND = ARGV[0]
Vagrant.configure("2") do |config|
  # The most common configuration options are documented and commented below.
  # For a complete reference, please see the online documentation at
  # https://docs.vagrantup.com.

  # Every Vagrant development environment requires a box. You can search for
  # boxes at https://atlas.hashicorp.com/search.
  #config.vm.box = "minimal/centos7"
  #config.vm.box = "centos/7"
  config.vm.box = "bento/centos-7.4"

  # Disable automatic box update checking. If you disable this, then
  # boxes will only be checked for updates when the user runs
  # `vagrant box outdated`. This is not recommended.
  # config.vm.box_check_update = false

  # Create a forwarded port mapping which allows access to a specific port
  # within the machine from a port on the host machine. In the example below,
  # accessing "localhost:8080" will access port 80 on the guest machine.
  # config.vm.network "forwarded_port", guest: 80, host: 8080

  # Create a private network, which allows host-only access to the machine
  # using a specific IP.
  # config.vm.network "private_network", ip: "192.168.33.10"

  # Create a public network, which generally matched to bridged network.
  # Bridged networks make the machine appear as another physical device on
  # your network.
  # config.vm.network "public_network"

  # Share an additional folder to the guest VM. The first argument is
  # the path on the host to the actual folder. The second argument is
  # the path on the guest to mount the folder. And the optional third
  # argument is a set of non-required options.
  # config.vm.synced_folder "../data", "/vagrant_data"

  if Vagrant::Util::Platform.windows? then
    config.vm.synced_folder "..\\", "/home/vagrant/Documents", owner: "vagrant", type: "virtualbox"
    config.vm.synced_folder ".", "/vagrant", type: "virtualbox"
  else
    config.vm.synced_folder "~/", "/home/vagrant/Documents", owner: "vagrant", type: "virtualbox"
    config.vm.synced_folder ".", "/vagrant", type: "virtualbox"
  end


  # Provider-specific configuration so you can fine-tune various
  # backing providers for Vagrant. These expose provider-specific options.
  # Example for VirtualBox:
  #
  config.vm.provider "virtualbox" do |vb|
    # Display the VirtualBox GUI when booting the machine
    vb.gui = true

    # Customize the amount of memory on the VM:
    vb.memory = "6144"
    vb.cpus = 2
    # vb.customize ["modifyvm", :id, "--cpuexecutioncap", "80"]
    vb.customize ["modifyvm", :id, "--vram", "12"]

    vb.name = "CentOS 7 Development VM"
  end

  # Enable ssh forwarding
  config.ssh.forward_agent = true
  config.ssh.forward_x11 = true

  # View the documentation for the provider you are using for more
  # information on available options.

  # Define a Vagrant Push strategy for pushing to Atlas. Other push strategies
  # such as FTP and Heroku are also available. See the documentation at
  # https://docs.vagrantup.com/v2/push/atlas.html for more information.
  # config.push.define "atlas" do |push|
  #   push.app = "YOUR_ATLAS_USERNAME/YOUR_APPLICATION_NAME"
  # end

  # Enable provisioning with a shell script. Additional provisioners such as
  # Puppet, Chef, Ansible, Salt, and Docker are also available. Please see the
  # documentation for more information about their specific syntax and use.
  config.vm.provision "shell", privileged: false, inline: <<-SHELL
    if [ ! -d ~/.vagrant-installation/ ]
    then
       echo Creating the ~/.vagrant-installation directory
       mkdir ~/.vagrant-installation
       mkdir ~/bin
    fi
  SHELL

  # Update all packages and install a base set of dependencies,
  # Gnome, Development Tools, etc.
  config.vm.provision "dependencies", type: "shell", path: "dependencies.sh"

  # config.vm.provision "gnome", type: "shell", path: "gnome.sh"

  config.vm.provision "x11", type: "shell", path: "x11.sh"

  # config.vm.provision "mu4e", type: "shell", path: "mu4e.sh", privileged: false

  # Install the latest version of Git
  config.vm.provision "git", type: "shell", path: "git.sh"

  # Download and Install Docker
  config.vm.provision "docker", type: "shell", path: "docker.sh", privileged: false

  # Download, Compile and Install Emacs
  config.vm.provision "emacs", type: "shell", path: "emacs.sh", privileged: false

  # Download and Install Spacemacs
  config.vm.provision "spacemacs", type: "shell", path: "spacemacs.sh", privileged: false

  # Download and Install Dot-Emacs
  config.vm.provision "dot-emacs", type: "shell", path: "dot-emacs.sh", privileged: false

  # Download and Install PlantUML
  config.vm.provision "plantuml", type: "shell", path: "plantuml.sh", privileged: false

  # Download, Compile and Install LanguageTool
  config.vm.provision "langtool", type: "shell", path: "langtool.sh", privileged: false

  # Download and Install Reveal.js
  config.vm.provision "reveal", type: "shell", path: "reveal.sh", privileged: false

  # Create the texlive.profile file to allow configuration of Texlive
  config.vm.provision "shell", path: "texlive.prof.sh"

  # Download and Install Texlive
  config.vm.provision "texlive", type: "shell", path: "texlive.sh"

  # Download and Install the Sphinx Documentation Generator
  config.vm.provision "sphinx", type: "shell", path: "sphinx.sh"

  # Download and Install the Pandoc Documentation Generator
  config.vm.provision "pandoc", type: "shell", path: "pandoc.sh"

  # Download and Install the Atom editor
  config.vm.provision "atom", type: "shell", path: "atom.sh", privileged: false

  # Download and Install Clojure and Leiningen
  # https://leiningen.org/
  config.vm.provision "lein", type: "shell", path: "lein.sh", privileged: false

  #Disable "Tracker" as it sucks down too much CPU
  #config.vm.provision "shell", path: "tracker.sh", privileged: false

  config.vm.provision "stumpwm", type: "shell", path: "stumpwm.sh", privileged: false

  config.vm.provision "cmatrix", type: "shell", path: "cmatrix.sh", privileged: false

  # Download and Install Golang
  config.vm.provision "golang", type: "shell", path: "golang.sh", privileged: false

  # Download and Install the latest stable version of the Eclipse IDE
  # config.vm.provision "shell", path: "eclipse.sh", privileged: false

  # Install all C/C++ dependencies
  config.vm.provision "cplusplus", type: "shell", path: "cplusplus.sh"

  # config.vm.provision "shell", inline: <<-SHELL
  #     systemctl set-default graphical.target
  # SHELL

end
