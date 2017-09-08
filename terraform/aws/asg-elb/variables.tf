variable "user_region" {
    type                    = "string"
    description             = "AWS region in which all resources will be created"
}

variable "keypair" {
    type                    = "string"
    description             = "AWS SSH keypair to use to connect to instances"
}

variable "flavor" {
    type                    = "string"
    description             = "AWS type to use when creating instances"
}

variable "secgrp" {
    type                    = "string"
    description             = "ID of VPC security group to use"
}
