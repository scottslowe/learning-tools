# Local Kubernetes Cluster with Kubeadm

These files were created to allow users to use Vagrant ([http://www.vagrantup.com](http://www.vagrantup.com)) quickly and relatively easily spin up a local Kubernetes cluster that is bootstrapped using `kubeadm`.

## Contents

* **ansible.cfg**: This Ansible configuration file configures Ansible for use with this Vagrant environment. No changes to this file should be necessary.

* **machines.yml**: This YAML file contains a list of VM definitions. It is referenced by `Vagrantfile` when Vagrant instantiates the VMs. You will need to edit this file to provide the correct Vagrant box names. Other edits should not be necessary.

* **provision.yml**: This Ansible playbook configures the VMs with the necessary package repositories, and installs the prerequisite packages. No changes to this file should be necessary.

* **README.md**: This file you're currently reading.

* **Vagrantfile**: This file is used by Vagrant to spin up the virtual machines. This file is fairly extensively commented to help explain what's happening. You should be able to use this file unchanged; all the VM configuration options are stored outside this file.

## Instructions

These instructions assume you've already installed your virtualization provider (VMware Fusion/Workstation or VirtualBox), Vagrant, and any necessary plugins (such as the Vagrant VMware plugin). Please refer to the documentation for those products for more information on installation or configuration.

1. Use `vagrant box add` to install an Ubuntu 16.04 base box. Review `machines.yml` for some suggested boxes for Libvirt, VirtualBox, and the VMware virtualization platforms.

2. If the name of your Ubuntu 16.04 base box is _not_ one of the ones listed in `machines.yml`, edit `machines.yml` to supply the correct box name(s). This file has separate lines for each provider; be sure to edit the correct line for your provider ("lv" is for Libvirt, "vb" is for VirtualBox, and "vmw" is for VMware).

3. Place the files from the `kubernetes/kubeadm-vagrant` directory of this GitHub repository into a directory on your local system. You can clone the entire "learning-tools" repository (using `git clone`) or just download the specific files from the the `kubernetes/kubeadm-vagrant` folder.

4. Run `vagrant up` to instantiate 3 VMs---one manager and two nodes. All the VMs will be running Ubuntu 16.04.

5. Once Vagrant is finished, use `vagrant ssh master` to log into the manager VM.

6. While logged into "master", run the following command to start bootstrapping your Kubernetes cluster:

        kubeadm init --apiserver-advertise-address 192.168.100.100 \
        --feature-gates CoreDNS=true DynamicKubeletConfig=true \
        SelfHosting=true --pod-network-cidr 172.24.0.0/16

7. When the command completes, follow the steps listed to install a configuration file for `kubelet`.

8. Copy the final output of the `kubeadm init` command, which includes a token, to the clipboard. You'll need it later.

9. The three VMs all have two network cards, and we want to ensure that all Kubernetes services run on the second NIC (enp0s8 on Ubuntu 16.04). This is the one connected to the private network 192.168.100.0/24. To ensure this is the case, log into each node (e.g. `vagrant ssh master`). As root, edit the file `/etc/default/kubelet` and add  `--node-ip=$ipaddr` to the `KUBELET_EXTRA_ARGS=` line, where `$ipaddr` is the IP address assigned to the second NIC. Then reload the daemons and restart the kubelet service. The following commands will do this (don't forget to run them as root, and also make sure you run them on all three nodes):

        eth1="enp0s8"
        ipaddr=$(ip a show $eth1 | egrep -o '([0-9]*\.){3}[0-9]*' | head -n1)
        sed -i "s/KUBELET_EXTRA_ARGS=/KUBELET_EXTRA_ARGS=--node-ip=$ipaddr/" /etc/default/kubelet
        systemctl daemon-reload && systemctl restart kubelet

10. Install a CNI plugin, such as Calico (see [https://docs.projectcalico.org/v3.3/getting-started/kubernetes/](https://docs.projectcalico.org/v3.3/getting-started/kubernetes/))

        kubectl apply -f \
        https://docs.projectcalico.org/v3.3/getting-started/kubernetes/installation/hosted/etcd.yaml
        kubectl apply -f \
        https://docs.projectcalico.org/v3.3/getting-started/kubernetes/installation/rbac.yaml
        kubectl apply -f \
        https://docs.projectcalico.org/v3.3/getting-started/kubernetes/installation/hosted/calico.yaml

11. Log out and run `vagrant ssh node-01` to log into "node-01". Run the `kubeadm join` command you copied from the output on "master".

12. Repeat step 10, but on "node-02".

Enjoy!
