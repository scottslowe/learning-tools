# VIM Customisation and bundles

This directory contains vim customisations and settings that make vim look and behave better.

These extensions and setting require the full version of vim. To install on Linux use: -

```bash
 sudo apt-get install vim
```

#### Rename vimrc file

Rename `vimrc` file to  `~/.vimrc`

To use [vundle](https://github.com/VundleVim/Vundle.vim/blob/master/README.md) uncomment the relevent lines in vimrc and
run PluginInstall from within vim.

**Note** that vundle requires *curl, git* and an internet connection.

To use YouCompleteMe, additional setup steps are required listed [here](https://valloric.github.io/YouCompleteMe/#ubuntu-linux-x64)

#### untar vim.tar file

Untar the `vim.tar` file in home directory - this will create the `.vim` directory.

Martin Robson 29/06/2017
