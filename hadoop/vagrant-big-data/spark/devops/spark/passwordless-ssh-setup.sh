#!/bin/bash

ssh-copy-id -i /home/vagrant/.ssh/id_rsa.pub vagrant@sparkslave1
ssh-copy-id -i /home/vagrant/.ssh/id_rsa.pub vagrant@sparkslave2
ssh-copy-id -i /home/vagrant/.ssh/id_rsa.pub vagrant@sparkslave3
ssh-copy-id -i /home/vagrant/.ssh/id_rsa.pub vagrant@sparkmaster1

