#!/bin/bash

# Make a copy of the CirrOS disk image
cp cirros-0.3.2-x86_64-disk.img cirros-01.img

# Launch a VM using the copy of the disk image
sudo virt-install --name=cirros --ram=256 --vcpus=1 \
  --disk path=./cirros-01.img,format=qcow2,bus=virtio \
  --import --vnc --noautoconsole --hvm \
  --network network:ovs,model=virtio
