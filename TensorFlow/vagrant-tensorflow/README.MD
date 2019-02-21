![TensorFlow Logo](logo.png)

# Vagrant Machine  - TensorFlow
This vagrant machine(s) has been created to make sure developers have an easy method for boostrapping a local TensorFlow development environment.

* Machines with TensorFlow 1.6.0 (https://github.com/tensorflow/tensorflow/tree/v1.6.0-rc1)
    * Debian 8.0.0
        * Provider(s)          : VirtualBox.
        * Arch(s)              : amd64.
        * Installation Type(s) : Standard, desktop. 
        * Additional Info
            * Root User
                * Login: <i>root</i>
                * Password: <i>123.Password</i>
            * Default user
                * Login: <i>vagrant</i>
                * Password: <i>vagrant</i>

## Usage
1. You need to have installed in your system the following softwares:
    * [VirtualBox](https://www.virtualbox.org)                 - Tested with version 5.0.x
    * [Vagrant](https://www.vagrantup.com)                     - Tested with version 1.7.4
2. Clone this git repository in /some/path/ and go to him.
3. Choose one of the machines in the repository and go to him.
3. Run `vagrant up` to start your machine.
4. Run `vagrant ssh` to accesss your machine.
5. Run `python -m tensorflow.models.image.mnist.convolutional` to test your machine.

## Contributing
1. Fork it!
2. Create your feature branch: `git checkout -b my-new-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin my-new-feature`
5. Submit a pull request =^.~=

## License
BSD 3