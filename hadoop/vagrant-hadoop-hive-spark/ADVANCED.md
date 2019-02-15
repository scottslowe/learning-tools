Additional virtual machine details
==================================


# Useful Vagrant commands

1. Run ```vagrant ssh``` to get into your VM.
2. Run ```vagrant halt`` to stop your VM.
3. Run ```vagrant destroy``` when you want to destroy and get rid of the VM, or
```vagrant destroy && vagrant up``` to destroy and bring up a new VM.

# Getting around the virtual machine

SSH into your VM:

```vagrant ssh```

This will log you in as the `ubuntu` user.

Hadoop and friends are all installed under `/usr/local`.


# Changing the virtual machine memory

Edit `Vagrantfile` and look for the line that contains `--memory` - modify
the value that you see there.

# Vagrant box location
The Vagrant box is downloaded to the ~/.vagrant.d/boxes directory. On Windows, this is C:/Users/{your-username}/.vagrant.d/boxes.

