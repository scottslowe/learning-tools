Vagrant.configure(2) do |config|

  config.vm.box = "box-cutter/ubuntu1604-desktop"
  #config.vm.box_version = "2.0.26"
  config.ssh.username = "vagrant"
  config.ssh.password = "vagrant"

  config.vm.provider "virtualbox" do |vb|
    #   # Display the VirtualBox GUI when booting the machine
    vb.gui = true
    #
    #   # Customize the amount of memory on the VM:
    vb.memory = "8192"
    vb.cpus = 4
    #config.vm.synced_folder "./vagrant_home", "/home/vagrant"
  end

  config.vm.provision "file", source: "./scripts", destination: "/tmp/vagrant/scripts"

  config.vm.provision "shell", inline: <<-SHELL

    . /tmp/vagrant/scripts/utils/before.sh;
    . /tmp/vagrant/scripts/utils/import-install-scripts.sh;
    . /tmp/vagrant/scripts/utils/install-packages.sh;
    . /tmp/vagrant/scripts/utils/after.sh;

    IMPORTS=(angular-cli aws-cli docker git google-chrome gradle guake intellij-idea jdk8 maven mongo node-and-npm opera pip softether-vpn sublime-text-3)
    PACKAGES=(guake opera sublime-text-3 git docker jdk8 maven gradle node-and-npm angular-cli pip aws-cli mongo softether-vpn intellij-idea google-chrome)

    before

    import-install-scripts $IMPORTS
    install-packages $PACKAGES

    after

   SHELL
end
