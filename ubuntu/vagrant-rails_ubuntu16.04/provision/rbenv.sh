#!/bin/bash
echo " "
echo "-----------------------------------------------------------------------------"
echo "Provisioning RBEnv on Ubuntu user"
echo "-----------------------------------------------------------------------------"
echo " "
echo "[1/6] --== Cloning rbenv from GitHub & compile all stuff ==--"
if [ -d ~/.rbenv ]; then
  echo "  +--> Updating repository"
  cd ~/.rbenv
  git pull >> /vagrant/log/provition-rbenv.log 2>&1
else
  echo "  +--> Fresh install, cloning"
  git clone https://github.com/rbenv/rbenv.git ~/.rbenv >> /vagrant/log/provition-rbenv.log 2>&1
fi
echo "  +--> Making stuff"
cd ~/.rbenv && src/configure && make -C src >> /vagrant/log/provition-rbenv.log 2>&1

echo "[2/6] --== Updating path if needed ==--"
grep -q -F 'export PATH="$HOME/.rbenv/bin:$PATH"' ~/.bashrc || echo 'export PATH="$HOME/.rbenv/bin:$PATH"' >> ~/.bashrc
grep -q -F 'export PATH="$HOME/.rbenv/bin:$PATH"' ~/.zshrc || echo 'export PATH="$HOME/.rbenv/bin:$PATH"' >> ~/.zshrc

echo "[3/6] --== Initializing rbenv ==--"
~/.rbenv/bin/rbenv init >> /vagrant/log/provition-rbenv.log 2>&1

echo "[4/6] --== Starting rbenv on new shell ==--"
grep -q -F 'eval "$(rbenv init -)"' ~/.bashrc || echo 'eval "$(rbenv init -)"' >> ~/.bashrc
grep -q -F 'eval "$(rbenv init -)"' ~/.zshrc || echo 'eval "$(rbenv init -)"' >> ~/.zshrc

echo "[5/6] --== Installing rbenv plugins ==--"
apt-get install gcc-6 autoconf bison build-essential libssl-dev libyaml-dev libreadline6-dev zlib1g-dev libncurses5-dev libffi-dev libgdbm3 libgdbm-dev >> /vagrant/log/provition-rbenv.log 2>&1
if [ -d ~/.rbenv/plugins/ruby-build ]; then
  cd ~/.rbenv/plugins/ruby-build
  git pull
else 
  git clone https://github.com/sstephenson/ruby-build.git ~/.rbenv/plugins/ruby-build >> /vagrant/log/provition-rbenv.log 2>&1
fi

if [ -d ~/.rbenv/plugins/rbenv-vars ]; then
  cd ~/.rbenv/plugins/rbenv-vars
  git pull
else 
  git clone https://github.com/sstephenson/rbenv-vars.git ~/.rbenv/plugins/rbenv-vars >> /vagrant/log/provition-rbenv.log 2>&1
fi

echo "[6/6] --== Setting default gem configuration ==--"
echo "gem: --no-document" > ~/.gemrc