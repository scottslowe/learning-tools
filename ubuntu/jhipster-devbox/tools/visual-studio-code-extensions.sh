#!/bin/sh

# install useful extensions
sudo ln -sf /home/vagrant/.local/share/umake/ide/visual-studio-code/bin/code /usr/local/bin/code
code --install-extension redhat.java
code --install-extension johnpapa.Angular1
code --install-extension johnpapa.Angular2
code --install-extension msjsdiag.debugger-for-chrome
code --install-extension dbaeumer.vscode-eslint
code --install-extension EditorConfig.EditorConfig
code --install-extension PeterJausovec.vscode-docker
code --install-extension abusaidm.html-snippets
code --install-extension xabikos.JavaScriptSnippets
code --install-extension christian-kohler.npm-intellisense #intelligent js imports
code --install-extension alefragnani.project-manager #manage several vscode windows
code --install-extension jhipster-ide.jdl #jhipster-ide JDL support
