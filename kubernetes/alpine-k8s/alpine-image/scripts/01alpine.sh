set -ux

echo "Setting up remote repositories..."
cat >/etc/apk/repositories <<EOT
http://nl.alpinelinux.org/alpine/edge/main/
http://nl.alpinelinux.org/alpine/edge/community/
http://nl.alpinelinux.org/alpine/edge/testing/
EOT

echo "Performing an update/upgrade"
apk update
apk upgrade
apk add bash bash-completion util-linux pciutils usbutils coreutils findutils grep gawk sed lsof

echo "Setting systctl kernel setting to relax security"
cat >/etc/sysctl.d/00-alpine.conf <<EOT
net.ipv4.ip_forward = 1
net.ipv4.tcp_syncookies = 1
net.ipv4.conf.default.rp_filter = 0
net.ipv4.conf.all.rp_filter = 0
net.ipv4.ping_group_range=0 2147483647
kernel.panic = 120
EOT

cat >/etc/sysctl.d/01-disable-grsec.conf <<EOT
kernel.grsecurity.chroot_caps = 0
kernel.grsecurity.chroot_deny_chmod = 0
kernel.grsecurity.chroot_deny_chroot = 0
kernel.grsecurity.chroot_deny_fchdir = 0
kernel.grsecurity.chroot_deny_mknod = 0
kernel.grsecurity.chroot_deny_mount = 0
kernel.grsecurity.chroot_deny_pivot = 0
kernel.grsecurity.chroot_deny_shmat = 0
kernel.grsecurity.chroot_deny_sysctl = 0
kernel.grsecurity.chroot_deny_unix = 0
kernel.grsecurity.chroot_enforce_chdir = 0
kernel.grsecurity.chroot_findtask = 0
kernel.grsecurity.chroot_restrict_nice = 0
kernel.grsecurity.harden_ipc = 0
kernel.grsecurity.ip_blackhole = 0
kernel.grsecurity.socket_all = 0
kernel.grsecurity.socket_all_gid = 0
kernel.grsecurity.socket_client = 0
kernel.grsecurity.socket_all_gid = 0
kernel.grsecurity.socket_client = 0
kernel.grsecurity.socket_client_gid = 0
kernel.grsecurity.socket_server = 0
kernel.grsecurity.socket_server_gid = 0
kernel.grsecurity.consistent_setxid = 0
kernel.grsecurity.deny_new_usb = 0
kernel.grsecurity.deter_bruteforce = 0
kernel.grsecurity.disable_priv_io = 0
kernel.grsecurity.dmesg = 0
kernel.grsecurity.enforce_symlinksifowner = 0
kernel.grsecurity.fifo_restrictions = 0
kernel.grsecurity.harden_ptrace = 0
kernel.grsecurity.lastack_retries = 0
kernel.grsecurity.linking_restrictions = 0
kernel.grsecurity.ptrace_readexec = 0
kernel.grsecurity.romount_protect = 0
kernel.grsecurity.symlinkown_gid = 0
kernel.grsecurity.tpe = 0
kernel.grsecurity.tpe_gid = 0
kernel.grsecurity.tpe_invert = 0
kernel.grsecurity.tpe_restrict_all = 0
EOT

exit 0
