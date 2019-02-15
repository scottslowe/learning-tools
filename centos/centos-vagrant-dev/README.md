# Vagrant Configured Development Virtual Machine

<!-- markdown-toc start - Don't edit this section. Run M-x markdown-toc-generate-toc again -->
**Table of Contents**

- [Vagrant Configured Development Virtual Machine](#vagrant-configured-development-virtual-machine)
    - [Overview](#overview)
    - [Dependencies](#dependencies)
    - [Installation](#installation)
    - [Using the VM](#using-the-vm)
        - [Shared Folders](#shared-folders)
        - [VM Login Details](#vm-login-details)
    - [Vagrant Installed Packages](#vagrant-installed-packages)
    - [Post Installation Configuration](#post-installation-configuration)
        - [Copy/Paste](#copypaste)
        - [General Desktop Configurations](#general-desktop-configurations)
        - [Atom Editor](#atom-editor)
        - [Configuring Git](#configuring-git)
- [PlantUML example](#plantuml-example)

<!-- markdown-toc end -->

## Overview

This Vagrant script will install Emacs, Spacemacs, and various development
environments on a Centos 7.4 Virtual Machine.

## Dependencies

The following dependencies are required and will most likely require
Administration privileges to install.

1. VirtualBox (https://www.virtualbox.org)

2. VirtualBox Guest Additions (https://github.com/dotless-de/vagrant-vbguest)
   - `vagrant plugin install vagrant-vbguest`
    
2. Git (https://git-for-windows.github.io)

3. Vagrant (https://wwww.vagrantup.com)

4. Following installation of Vagrant, at the command-prompt, execute the
   following; `vagrant plugin install vagrant-vbguest`

## Installation

1. Ensure that the dependencies listed in the previous section are installed.

2. Git clone this repository

3. `cd centos-vagrant-dev`

4. Enter `vagrant up` at the command-line in the vagrant-centos-7-vm directory

5. Come back in a couple of hours.

## Using the VM

Enter `vagrant up` at the command-line to start the VM.

Note that the VM is installed without a Window Manager. It is assumed that you
will X11 in from the Host OS.

### Shared Folders
The vagrant virtual machine is configured to share files and folders between the
*Host* system and the *Guest* system. Documents, configurations, and code must
be saved within these shared folders and not in the file-system of the *Guest*
OS because these files *will* be lost if the virtual machine is deleted.

The shared folder is located in the *Guest* system at this location:
`~/Documents` (`/home/vagrant/Documents`).

It is assumed that the `vagrant up` command is executed in a directly under
`\Users\<user_name>\` for example `\Users\<user_name>\<centos-vagrant-7-vm>`
directory, as the shared folder is mapped to `\Users\<user_name>\Documents`.

When in the virtual machine, the *Host* folders are accessible from
`~/Documents/Documents`.

### VM Login Details

Login to the VM as using `vagrant` as both user and password.

## Vagrant Installed Packages

Vagrant will install the following packages when provisioning the VM.

* X11 - supporting Xming (Windows) and XQuartz (OSX)
* PlantUML
* Graphviz
* Reveal.js
* Java
* Maven
* Atom editor
* Emacs, with Spacemacs
* Git
* Docker
* LangTool
* LaTeX - TexLive
* Spinx-docker
* Pandoc
* Python
* GoLang
* Clojure and Leiningen
* SBCL - Common Lisp (FTW!!!)
* StumpWM - a Common Lisp window manager

## Post Installation Configuration

### Copy/Paste

To enable copy/paste between *Host* and *Guest* OS, navigate to the VirtualBox
menu for the virtual machine and enable copy/paste as follows;

*Devices->Shared Clipboard->Bidirectional*

### General Desktop Configurations

1. Change lock screen defaults
   * *Applications->System Tools->Settings->Privacy->Lock Screen->Off*
2. Change Timezone
   * *Applications->System Tools->Settings->Date & Time->Automatic Time Zone->Off*
   * *Applications->System Tools->Settings->Date & Time->Time Zone->PDT*
3. Change screensaver defaults
   * *Applications->System Tools->Settings->Power->Blank Screen->Never*
4. Automatic Login
   * Go to user settings, and enable "Automatic Login" for the `vagrant` user

### Atom Editor

The Vagrant installation script adds support for Markdown and PlantUML to the
Atom editor.

1. The following Atom packages are installed;

    ```bash
    apm install language-plantuml
    apm install plantuml-preview
    apm install language-gfm-enhanced
    apm install markdown-preview-enhanced
    apm install language-restructuredtext
    apm install rst-preview-pandoc
    ```

2. To enable real-time PlantUML preview in Atom; *Packages->PlantUML
   Preview->Toggle*

3. The following must be set in Atom in order to use the `plantuml-preview`
   package.
   
   *Edit->Preferences->Packages->plantuml-preview->Settings*
 
 ```
   Graphvis Dot Executable: /usr/bin/dot
   Additional PlantUML Arguments: -Djava.awt.headless=true
   PlantUML Jar: /home/vagrant/.vagrant-installation/plantuml/plantuml.jar
   Java Executable: /usr/bin/java
  ``` 

### Configuring Git

Enter the following at the command-line;

  ```bash
  git config --global user.name "First Last"
  git config --global user.email "flast@company-name.com"
  git config --global core.autocrlf true
  ```

# PlantUML example

```plantuml
System_A -> System_B
System_B --> System_B
System_A <- System_B
```

```plantuml
Bob -> Alice : hello
Alice -> Bob : Go Away
```

