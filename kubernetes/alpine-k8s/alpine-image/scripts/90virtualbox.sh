
set -eux
echo "VBoxGuestAdditions currently do not build or install on Alpine Linux."
exit 0
#
# #
# # VBoxGuestAdditions fails to install.
# #
# # Alpine is intended to be 'minimal' so
# # there are certain things VBGA
# # 1. needs
# # 2. *assumes* are available
# # 3. or function a specific way
# # which is, not yet, the case...
# #
#
# mkdir -p /mnt/virtualbox
# retval=$?
# [ $retval -eq 0 ] || exit $retval
#
# modprobe loop
# retval=$?
# [ $retval -eq 0 ] || exit $retval
#
# LOOP=`losetup -f`
# retval=$?
# [ $retval -eq 0 ] || exit $retval
#
# losetup $LOOP /root/VBoxGuestAdditions.iso
# retval=$?
# [ $retval -eq 0 ] || exit $retval
#
# mount -t iso9660 -o ro $LOOP /mnt/virtualbox
# retval=$?
# [ $retval -eq 0 ] || exit $retval
#
# # current error 'unable to determine library path.'
# # "ldconfig -v" does not result in a list of valid
# # library paths (it is actually a shell script which
# # silently ignores -v).
# #
# # there are other issues as well, which have been
# # open with oracle/virtualbox for several years.
# # without forward progress (according to search
# # results and skimming through various discussions).
# sh /mnt/virtualbox/VBoxLinuxAdditions.run
# retval=$?
# [ $retval -eq 0 ] || exit $retval
#
# ln -s /opt/VBoxGuestAdditions-*/lib/VBoxGuestAdditions /usr/lib/VBoxGuestAdditions
# umount /mnt/virtualbox
# rm -rf /root/VBoxGuestAdditions.iso
#
# # END
