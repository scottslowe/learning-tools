# -*- mode: ruby -*-
# vi: set ft=ruby :
#
# Changes made to support docker as a provider.
# 
Vagrant.require_version '>= 1.4.3'
VAGRANTFILE_API_VERSION = '2'.freeze

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
    config.vm.provider "docker" do |d|
	    d.image = "nishidayuya/docker-vagrant-ubuntu:xenial"
	    d.has_ssh = true
	    d.ports = ["8088:8088","8080:8080","9083:9083","4040:4040","8888:8888","16010:16010"]
    end
    config.vm.provider "virtualbox" do |v, override|
	    override.vm.box = "ubuntu/xenial64"
	    v.gui = false
	    v.name = "node1"
            v.customize ['modifyvm', :id, '--memory', '8192']
    end
    config.vm.network "forwarded_port", guest: 8080, host: 8080
    config.vm.network "forwarded_port", guest: 8088, host: 8088
    config.vm.network "forwarded_port", guest: 9083, host: 9083
    config.vm.network "forwarded_port", guest: 4040, host: 4040
    config.vm.network "forwarded_port", guest: 18888, host: 18888
    config.vm.network "forwarded_port", guest: 16010, host: 16010
    config.vm.define "node1" do |node|
        node.vm.network :private_network, ip: '10.211.55.101'
        node.vm.hostname = 'node1'
        node.vm.provision :shell, path: 'scripts/setup-ubuntu.sh'
        node.vm.provision :shell, path: 'scripts/setup-java.sh'
        node.vm.provision :shell, path: 'scripts/setup-hadoop.sh'
        node.vm.provision :shell, path: 'scripts/setup-hive.sh'
        node.vm.provision :shell, path: 'scripts/setup-spark.sh'
        node.vm.provision :shell, path: 'scripts/setup-tez.sh'
	# Optional components - uncomment to include
        node.vm.provision :shell, path: 'scripts/setup-hbase.sh'
        #node.vm.provision :shell, path: 'scripts/setup-pig.sh'
        #node.vm.provision :shell, path: 'scripts/setup-flume.sh'
        #node.vm.provision :shell, path: 'scripts/setup-sqoop.sh'
        #node.vm.provision :shell, path: 'scripts/setup-zeppelin.sh'
        node.vm.provision :shell, path: 'scripts/finalize-ubuntu.sh'
        node.vm.provision :shell, path: 'scripts/bootstrap.sh', run: 'always'
    end
end
