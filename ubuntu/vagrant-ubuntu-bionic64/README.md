# Criação de Ambiente para Treinamento em Vagrant e Shell

Este repositório define o processo de criação automatizada de um ambiente de desenvolvimento usando Vagrant, provisionado com o Shell.

O Ambiente a ser provido é uma máquina com *Ubuntu 18.04 LTS*, com *5 GB de RAM*, e *4 Núcleos* com uso de até *70% de disco*.

### Alguns pacotes e ferramentas estão listados abaixo:

1. Git;
2. XClip;
3. SDKMan;
4. JDK 1.8;
5. Maven;
6. Gradle;
7. Visual Studio Code;
8. Eclipse JEE 2018-12;
9. Tilix;
10. ZSH Terminal;
11. Postman;
12. Docker CE;

### Pacotes instalados como requisitos do Docker CE:

13. apt-transport-https;
14. ca-certificates;
15. curl;
16. gnupg-agent;
17. software-properties-common.

## Requerimentos para o processo:

1. Clonar este repositório: [https://github.com/marcosnasp/vagrant-curso-ufma] (https://github.com/marcosnasp/vagrant-curso-ufma)
2. Clonar o repositório localizado em no mesmo nível desse projeto: [https://github.com/marcosnasp/vagrant-shell-scripts](https://github.com/marcosnasp/vagrant-shell-scripts)
3. Executar esses comandos no diretório raiz do repositório clonado em (1):

```console
foo@bar: vagrant up
```
