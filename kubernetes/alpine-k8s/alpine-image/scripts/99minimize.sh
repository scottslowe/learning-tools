
set -ux

# add in order to allow packer ssh access to provision
# the system, remove here to make box more secure
sed -i '/^PermitRootLogin yes/d' /etc/ssh/sshd_config

echo "Clean up apk cache"
rm -rf /var/cache/apk/*

dd if=/dev/zero of=/EMPTY bs=1M
rm -f /EMPTY
# Block until the empty file has been removed, otherwise, Packer
# will try to kill the box while the disk is still full and that's bad
sync
sync
sync

exit 0
