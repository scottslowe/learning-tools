#!/bin/sh

# update the system
export DEBIAN_FRONTEND=noninteractive
apt-mark hold keyboard-configuration
apt-get update
apt-get -y upgrade
apt-mark unhold keyboard-configuration

################################################################################
# Install the mandatory tools
################################################################################

# install utilities
apt-get -y install vim git zip bzip2 fontconfig curl language-pack-en

# install Java 8
apt-get install openjdk-8-jdk

# install Node.js
wget https://nodejs.org/dist/v10.15.0/node-v10.15.0-linux-x64.tar.gz -O /tmp/node.tar.gz
tar -C /usr/local --strip-components 1 -xzf /tmp/node.tar.gz

# update NPM
npm install -g npm

# install Yarn
npm install -g yarn
su -c "yarn config set prefix /home/vagrant/.yarn-global" vagrant

# install Yeoman
npm install -g yo

# install JHipster
npm install -g generator-jhipster@5.8.1

# install JHipster UML
npm install -g jhipster-uml@2.0.3

################################################################################
# Install the graphical environment
################################################################################

# force encoding
echo 'LANG=en_US.UTF-8' >> /etc/environment
echo 'LANGUAGE=en_US.UTF-8' >> /etc/environment
echo 'LC_ALL=en_US.UTF-8' >> /etc/environment
echo 'LC_CTYPE=en_US.UTF-8' >> /etc/environment

# run GUI as non-privileged user
echo 'allowed_users=anybody' > /etc/X11/Xwrapper.config

# install Ubuntu desktop and VirtualBox guest tools
apt-get install -y xubuntu-desktop virtualbox-guest-dkms virtualbox-guest-utils virtualbox-guest-x11

# remove light-locker (see https://github.com/jhipster/jhipster-devbox/issues/54)
apt-get remove -y light-locker --purge

# change the default wallpaper
#wget https://jhipster.github.io/images/wallpaper-004-2560x1440.png -O /usr/share/xfce4/backdrops/jhipster-wallpaper.png
wget https://raw.githubusercontent.com/jhipster/jhipster-devbox/master/images/jhipster-wallpaper.png -O /usr/share/xfce4/backdrops/jhipster-wallpaper.png
sed -i -e 's/xubuntu-wallpaper.png/jhipster-wallpaper.png/' /etc/xdg/xdg-xubuntu/xfce4/xfconf/xfce-perchannel-xml/xfce4-desktop.xml

################################################################################
# Install the development tools
################################################################################

# install Ubuntu Make - see https://wiki.ubuntu.com/ubuntu-make
apt-get install -y ubuntu-make

# install Chromium Browser
apt-get install -y chromium-browser

# install MySQL Workbench
apt-get install -y mysql-workbench

# install PgAdmin
apt-get install -y pgadmin3

# install Heroku toolbelt
wget -O- https://toolbelt.heroku.com/install-ubuntu.sh | sh

# install Guake
apt-get install -y guake
cp /usr/share/applications/guake.desktop /etc/xdg/autostart/

# install jhipster-devbox
git clone git://github.com/jhipster/jhipster-devbox.git /home/vagrant/jhipster-devbox
chmod +x /home/vagrant/jhipster-devbox/tools/*.sh

# install zsh
apt-get install -y zsh

# install oh-my-zsh
git clone git://github.com/robbyrussell/oh-my-zsh.git /home/vagrant/.oh-my-zsh
cp /home/vagrant/.oh-my-zsh/templates/zshrc.zsh-template /home/vagrant/.zshrc
chsh -s /bin/zsh vagrant
echo 'SHELL=/bin/zsh' >> /etc/environment

# install jhipster-oh-my-zsh-plugin
git clone https://github.com/jhipster/jhipster-oh-my-zsh-plugin.git /home/vagrant/.oh-my-zsh/custom/plugins/jhipster
sed -i -e "s/plugins=(git)/plugins=(git docker docker-compose jhipster)/g" /home/vagrant/.zshrc
echo 'export PATH="$PATH:/usr/bin:/home/vagrant/.yarn-global/bin:/home/vagrant/.yarn/bin:/home/vagrant/.config/yarn/global/node_modules/.bin"' >> /home/vagrant/.zshrc

# change user to vagrant
chown -R vagrant:vagrant /home/vagrant/.zshrc /home/vagrant/.oh-my-zsh

# install Visual Studio Code
su -c 'umake ide visual-studio-code /home/vagrant/.local/share/umake/ide/visual-studio-code --accept-license' vagrant

# fix links (see https://github.com/ubuntu/ubuntu-make/issues/343)
sed -i -e 's/visual-studio-code\/code/visual-studio-code\/bin\/code/' /home/vagrant/.local/share/applications/visual-studio-code.desktop

# disable GPU (see https://code.visualstudio.com/docs/supporting/faq#_vs-code-main-window-is-blank)
sed -i -e 's/"$CLI" "$@"/"$CLI" "--disable-gpu" "$@"/' /home/vagrant/.local/share/umake/ide/visual-studio-code/bin/code

#install IDEA community edition
su -c 'umake ide idea /home/vagrant/.local/share/umake/ide/idea' vagrant

# increase Inotify limit (see https://confluence.jetbrains.com/display/IDEADEV/Inotify+Watches+Limit)
echo "fs.inotify.max_user_watches = 524288" > /etc/sysctl.d/60-inotify.conf
sysctl -p --system

# install latest Docker
curl -sL https://get.docker.io/ | sh

# install latest docker-compose
curl -L "$(curl -s https://api.github.com/repos/docker/compose/releases | grep browser_download_url | head -n 4 | grep Linux | grep -v sha256 | cut -d '"' -f 4)" > /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# configure docker group (docker commands can be launched without sudo)
usermod -aG docker vagrant

# fix ownership of home
chown -R vagrant:vagrant /home/vagrant/

# clean the box
apt-get -y autoclean
apt-get -y clean
apt-get -y autoremove
dd if=/dev/zero of=/EMPTY bs=1M > /dev/null 2>&1
rm -f /EMPTY
