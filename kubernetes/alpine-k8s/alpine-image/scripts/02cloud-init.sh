#!/bin/sh

apk add python cloud-init tzdata

cat >/etc/cloud/cloud.cfg <<EOT
# The top level settings are used as module
# and system configuration.

# A set of users which may be applied and/or used by various modules
# when a 'default' entry is found it will reference the 'default_user'
# from the distro configuration specified below
users:
  - default
  - vagrant:
      name: vagrant
      primary_group: vagrant
      homedir: /home/vagrant
      lock_passwd: false
      shell: /bin/bash
      sudo: ['ALL=(ALL) NOPASSWD:ALL']
      ssh-autorized-keys: 
        - ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEA6NF8iallvQVp22WDkTkyrtvp9eWW6A8YVr+kz4TjGYe7gHzIw+niNltGEFHzD8+v1I2YJ6oXevct1YeS0o9HZyN1Q9qgCgzUFtdOKLv6IedplqoPkcmF0aYet2PkEDo3MlTBckFXPITAMzF8dJSIFo9D8HfdOV0IAdx4O7PtixWKn5y2hMNG0zQPyUecp4pzC6kivAIhyfHilFR61RGL+GPXQ2MWZWFYbAGjyiYJnAmCP3NOTd0jMZEnDkbUvxhMmBYSdETk1rRgm+R4LOzFUGaHqHDLKLX+FIPKcF96hrucXzcWyLbIbEgE98OHlnVYCzRdK8jlqm8tehUc9c9WhQ== vagrant insecure public key     

groups:
  - default 
  - vagrant

# If this is set, 'root' will not be able to ssh in and they 
# will get a message to login instead as the above \$user (ubuntu)
disable_root: false
ssh_pwauth: true
password: alpinek8s

# This will cause the set+update hostname module to not operate (if true)
# preserve_hostname: false
syslog_fix_perms: root:root
ssh_deletekeys: false

cloud_init_modules:
  - seed_random
  - bootcmd
  - write-files
  - growpart
  - resizefs
  - set_hostname
  - update_hostname
  - update_etc_hosts
  - ca-certs
  - users-groups
  - ssh

cloud_config_modules:
  - disk_setup
  - mounts
  - ssh-import-id
  - set-passwords
  - timezone
  - disable-ec2-metadata
  - runcmd

cloud_final_modules:
  - scripts-vendor
  - scripts-per-once
  - scripts-per-boot
  - scripts-per-instance
  - scripts-user
  - ssh-authkey-fingerprints
  - keys-to-console
  - phone-home
  - final-message
  - power-state-change

# System and/or distro specific settings
# (not accessible to handlers/transforms)
system_info:
  # This will affect which distro class gets used
  distro: alpine
  # Default user name + that default users groups (if added/used)
  default_user:
    name: alpine
    gecos: Alpine
    sudo: ["ALL=(ALL) NOPASSWD:ALL"]
    shell: /bin/bash
    lock_passwd: false
   # Other config here will be given to the distro class and/or path classes
  paths:
    cloud_dir: /var/lib/cloud/
    templates_dir: /etc/cloud/templates/
EOT

# Enable at boot
rc-update add cloud-init-local boot
#rc-update add cloud-init boot
#rc-update add cloud-config boot
#rc-update add cloud-final boot

