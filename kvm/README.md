# Learning Tools: KVM

The contents of this folder are targeted toward increasing knowledge and understanding of KVM.

## Contents

**kvm-generic**: The files in this folder allow you to create a learning environment for working with KVM and Libvirt on Ubuntu 16.04. The provisioning is handled via Ansible installed on the Vagrant host. This environment supports the use of VMware Fusion and Libvirt, but not VirtualBox (due to the requirement for nested virtualization).

**kvm-macvtap**: This learning environment has a `Vagrantfile` and supporting files for working with KVM and macvtap interfaces on Ubuntu 14.04. The provisioning is handled via Ansible (pre-2.0) installed on the Vagrant host.
