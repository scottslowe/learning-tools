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

This will log you in as the `vagrant` user.
For root access run the following:

```sudo su -```

Hadoop and friends are all installed under `/usr/local`.


# Changing the virtual machine memory

Edit `Vagrantfile` and look for the line that contains `--memory` - modify
the value that you see there.

# Speeding up the virtual machine provisioning time

The first time you run `vagrant up` it will download Hadoop and Spark
and put their tarballs into the `resources` directory.  When you
reprovision your virtual machine with the `vagrant destroy && vagrant up`
command, the deployment scripts will be smart enough to pick up the
previously downloaded tarballs.

The only exception is Java - by default Java is installed via `yum`,
so every time you reprovision your virtual machine it will download
Java.  To speed this up you can manually download the Oracle JDK into
the `resources` directory.  By default the script expects
the file to be:

jdk-8u51-linux-x64.gz

You can change the default by editing `scripts/common.sh`.


# Vagrant boxes
A list of available Vagrant boxes is shown at http://www.vagrantbox.es.

# Vagrant box location
The Vagrant box is downloaded to the ~/.vagrant.d/boxes directory. On Windows, this is C:/Users/{your-username}/.vagrant.d/boxes.

