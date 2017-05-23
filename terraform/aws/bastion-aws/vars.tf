variable "keypair" {
    type                    = "string"
    description             = "AWS SSH keypair to use to connect to instances"
}

variable "flavor" {
    type                    = "string"
    description             = "AWS type to use when creating instances"
}
