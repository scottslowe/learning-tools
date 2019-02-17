# -*- mode: ruby -*-
# vi: set ft=ruby :

ENV['VAGRANT_DEFAULT_PROVIDER']='virtualbox'
MASTER_LB_IP="10.250.250.11" # Load balanced VIP/IP for the master API

# Defaults
$etcd_cpu=1
$etcd_memory=1024
$master_cpu=1
$master_memory=1024
$minion_cpu=1
$minion_memory=2048

CONFIG = File.expand_path("config.rb")
if File.exist?(CONFIG)
  require CONFIG
end

# create a random cluster token or read from cluster-token if exists
if File.exist?("cluster-token") 
  cluster_token=File.read("cluster-token")
else
  t1=`cat /dev/urandom | LC_CTYPE=C tr -dc 'a-f0-9' | fold -w 6 | head -n 1`.chomp
  t2=`cat /dev/urandom | LC_CTYPE=C tr -dc 'a-f0-9' | fold -w 16 | head -n 1`.chomp
  cluster_token="#{t1}.#{t2}"
  File.write("cluster-token", cluster_token)
end

def etcdIP(num)
  return "10.250.250.#{num+1}"
end

def masterIP(num)
  return "10.250.250.#{num+10}"
end

def minionIP(num)
  return "10.250.250.#{num+20}"
end

# Setup for etcd
etcdIPs = [*1..$etcd_count].map{ |i| etcdIP(i) }
initial_etcd_cluster = etcdIPs.map.with_index{ |ip, i| "etcd#{i+1}=http://#{ip}:2380" }.join(",")
etcd_endpoints = etcdIPs.map.with_index{ |ip, i| "http://#{ip}:2379" }.join(",")
etcd_spaced_endpoints = etcdIPs.map.with_index{ |ip, i| "http://#{ip}:2379" }.join(" ")

Vagrant.configure(2) do |config|
config.ssh.insert_key = false
#config.vm.provider :virtualbox do |v|
#  v.check_guest_additions = false
#  v.functional_vboxsf     = false
#end
config.vm.synced_folder ".", "/vagrant", disabled: true
config.vm.box = "dmcc/alpine-3.6.0-docker-17.05.0-kubernetes-#{$kubernetes_version}"

  # disable vbguest updates as this does not work on alpine.
  if Vagrant.has_plugin?("vagrant-vbguest")
    config.vbguest.auto_update = false
  end

  (1..$etcd_count).each do |i|
    config.vm.define vm_name = "etcd#{i}" do |etcd|

      etcd.vm.provider :virtualbox do |vb|
        vb.customize ["modifyvm", :id, "--memory", $etcd_memory]
        vb.customize ["modifyvm", :id, "--cpus", $etcd_cpu]   
      end

      etcdIP = etcdIP(i)
      etcd.vm.network :private_network, ip: etcdIP, auto_config: false

      etcd.vm.provision :shell, path: "shared.sh", :privileged => true, env: { "SET_HOSTNAME": "etcd#{i}.example.com", "MY_IP": etcdIP }
      etcd.vm.provision :shell, path: "etcd.sh", :privileged => true, env: { "ETCD_NAME": "etcd#{i}", "ETCD_ADVERTISE_CLIENT_URLS": "http://#{etcdIP}:2379", "ETCD_LISTEN_CLIENT_URLS": "http://#{etcdIP}:2379,http://localhost:2379", "ETCD_INITIAL_ADVERTISE_PEER_URLS": "http://#{etcdIP}:2380", "ETCD_LISTEN_PEER_URLS": "http://0.0.0.0:2380", "ETCD_INITIAL_CLUSTER": initial_etcd_cluster }
   end
  end

  (1..$master_count).each do |i|
    config.vm.define vm_name = "master#{i}" do |master|

      master.vm.provider :virtualbox do |vb|
        vb.customize ["modifyvm", :id, "--memory", $master_memory]
        vb.customize ["modifyvm", :id, "--cpus", $master_cpu]   
      end

      masterIP = masterIP(i)
      master.vm.network :private_network, ip: masterIP, auto_config: false

      master.vm.provision :shell, path: "shared.sh", :privileged => true, env: { "SET_HOSTNAME": "master#{i}.example.com", "MY_IP": masterIP, "MY_NUMBER": $i }
      master.vm.provision :shell, path: "master.sh", :privileged => true, env: { "KUBE_TOKEN": cluster_token, "KUBERNETES_VERSION": $kubernetes_version, "ETCD_ENDPOINTS": etcd_spaced_endpoints, "MY_IP": masterIP, "MASTER_COUNT": $master_count, "MASTER_LB_IP": MASTER_LB_IP }
    end
  end

  (1..$minion_count).each do |i|
    config.vm.define vm_name = "minion#{i}" do |minion|

      minion.vm.provider :virtualbox do |vb|
        vb.customize ["modifyvm", :id, "--memory", $minion_memory]
        vb.customize ["modifyvm", :id, "--cpus", $minion_cpu]   
      end

      minionIP = minionIP(i)
      minion.vm.network :private_network, ip: minionIP, auto_config: false

      minion.vm.provision :shell, path: "shared.sh", :privileged => true, env: { "SET_HOSTNAME": "minion#{i}.example.com", "MY_IP": minionIP }
      minion.vm.provision :shell, path: "minion.sh", :privileged => true, env: { "KUBE_TOKEN": cluster_token, "MASTER_LB_IP": MASTER_LB_IP }
    end
  end
end

