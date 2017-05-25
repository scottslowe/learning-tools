variable "user_region" {
    type                    = "string"
    description             = "AWS region in which to create all resources"
}

variable "keypair" {
    type                    = "string"
    description             = "AWS SSH keypair to use to connect to instances"
}

variable "flavor" {
    type                    = "string"
    description             = "AWS type to use when creating instances"
}

variable "sec-group" {
    type                    = "string"
    description             = "AWS security group to apply to instances"
}
