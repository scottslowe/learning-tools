#!/bin/sh
echo " "
echo "-----------------------------------------------------------------------------"
echo "Provisioning Oh-My-ZSH on Ubuntu user"
echo "-----------------------------------------------------------------------------"
echo " "
echo "[1/2] --== Cloning Oh-My-ZSH ==--"
echo "             /!\\ Don't panic if some lines are red below, it's just normal /!\\"
if [ -d ~/.oh-my-zsh ]; then
  echo "  +--> Updating repository"
  cd ~/.oh-my-zsh
  git pull >> /vagrant/log/provition-rbenv.log 2>&1
else
  echo "  +--> Fresh install, cloning"
  cd ~
  wget https://github.com/robbyrussell/oh-my-zsh/raw/master/tools/install.sh -O - | zsh
fi
echo "             /!\\         Lines should be green or white from now          /!\\"
echo "[3/2] --== Changing default theme of Oh-My-ZSH if needed ==--"
sed -i 's/robbyrussell/agnoster/g' .zshrc >> /vagrant/log/provition-zsh.log 2>&1
