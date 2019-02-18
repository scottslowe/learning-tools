## Known Issues

### General

* The Mesos VM uses an
  [INSECURE SSH KEYPAIR](https://github.com/mitchellh/vagrant/tree/master/keys)
* During shell provisioning the warning `stdin: is not a tty` will appear. This
  is harmless and can be ignored. See [Vagrant Issue #1674](https://github.com/mitchellh/vagrant/issues/1673)

### Mesos

* Mesos executor is running all jobs as root, need to figure out how to
  adjust this.
