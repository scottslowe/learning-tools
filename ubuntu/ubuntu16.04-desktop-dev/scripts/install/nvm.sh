#!/bin/bash

install_nvm() {

	if [ -e "/usr/local/bin/nvm" ]; then return; fi

	export HOME=/home/vagrant
	#export NODE_VERSIION=6.9.3
	sudo apt-get install -y git-core curl build-essential

	#wget https://github.com/robbyrussell/oh-my-zsh/raw/master/tools/install.sh -O - | zsh

	#curl -sL --proto-redir -all,https https://raw.githubusercontent.com/zplug/installer/master/installer.zsh| zsh

	#zplug "lukechilds/zsh-nvm"
	#curl https://raw.githubusercontent.com/creationix/nvm/v0.33.6/install.sh | su - vagrant -c 

	#echo "source /home/vagrant/.nvm/nvm.sh" >> /home/vagrant/.profile
	export NVM_DIR="$HOME/.nvm" && (
	  git clone https://github.com/creationix/nvm.git "$NVM_DIR"
	  cd "$NVM_DIR"
	  git checkout `git describe --abbrev=0 --tags --match "v[0-9]*" origin`
	) && . "$NVM_DIR/nvm.sh"

	echo "export NVM_DIR=\"$HOME/.nvm\"" >> /home/vagrant/.bashrc
	echo "[ -s \"$NVM_DIR/nvm.sh\" ] && . \"$NVM_DIR/nvm.sh\"" >> /home/vagrant/.bashrc # This loads nvm

	source /home/vagrant/.bashrc
	nvm install 8.9.1

	#su - vagrant -c "
	#git clone https://github.com/lukechilds/zsh-nvm.git $HOME/.zsh-nvm
	#source $HOME/.zsh-nvm/zsh-nvm.plugin.zsh

}
