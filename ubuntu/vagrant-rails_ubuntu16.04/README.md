# Vagrant Box - RubyOnRails
*Laboratoire Gilbert - Vagrant Box*

## Packages:
Ubuntu 16.04 LTS, ZSH (agnoster) Nginx, Passenger, PHP7.1, RBEnv

*No Chief, KISS*

## Avant de commencer :

1) Installez les plugins vagrant suivant :
```bash
vagrant plugin install vagrant-proxyconf # Si vous souhaitez utiliser un proxy
vagrant plugin install vagrant-vbguest   # Installe les outils d'intégration Virtualbox
```

2) copier le fichier config.yml.sample en config.yml et adaptez le à vos besoins
```bash
cp config.yml.sample config.yml
```

## Premier vagrant up
1) Démarrer la VM
```bash
vagrant up
```

2) Terminer l'installation en installant les différentes version de Ruby utilisés aux Laboraoires Gilbert
```bash
vagrant ssh
rbenv install [RUBY_VERSION]
rbenv rehash
```
Pour passer cette version en globale:
```bash
rbenv global [RUBY_VERSION]
```
Puis déloggez vous et reloggez vous pour que le shell se mette à jour

Pour passer cette version uniquement sur l'arborescence
```bash
rbenv local [RUBY_VERSION]
````


## Lancement de vagrant et initialisation d'un projet :
1) Démarrer la vm et loggez-vous
```bash
vagrant up
vagrant ssh
```
2) Aller dans le projet
```bash
cd /var/www/[PROJET]
bundle install
```
***Note :** Si ca ne fonctionne pas, vérifiez que vous avez installé la bonne version de ruby par rapport au projet et que vous l'avez définie en local sur le répertoire (cf ci-dessus)*

3) Créer un nouveau vhost et redémarrez la provision
```bash
cp /vagrant/provision/nginx/testapp.conf /vagrant/provision/nginx/[VHOST].conf
exit
vagrant reload --provision
```

4) Pushez le nouveau VHOST pour les copains
```bash
git push
```

5) Codez

## RubyMine
Si vous utilisez RubyMine, vous devez taper la ligne suivante dans votre terminal :
```bash
vagrant ssh-config | sed 's/^Host default/Host 127.0.0.1/' >> ~/.ssh/config
```
