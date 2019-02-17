set -eux

# add in order to allow packer ssh access to provision
# the system, remove here to make box more secure
#sed -i '/^PermitRootLogin yes/d' /etc/ssh/sshd_config

# make 'vagrant ssh' connections faster
echo "UseDNS no" >> /etc/ssh/sshd_config

