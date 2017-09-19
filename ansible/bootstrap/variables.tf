variable "key_pair" {
    type                    = "string"
    description             = "SSH keypair to use to connect to instance(s)"
}

variable "dest_vpc" {
    type                    = "string"
    description             = "VPC ID for launched instance(s)"
}

variable "sec_grp" {
    type                    = "string"
    description             = "Security group ID for group to be applied to instance(s)"
}

variable "user_region" {
    type                    = "string"
    description             = "AWS region to use for new resources"
}

variable "dest_subnet" {
    type                    = "string"
    description             = "Subnet ID for launched instance(s)"
}

variable "instance_type" {
    type                    = "string"
    description             = "Type to use for launched instance(s)"
}
