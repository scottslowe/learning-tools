variable "user_region" {
    type                    = "string"
    description             = "AWS region to use for all resources"
}

variable "node_type" {
    type                    = "string"
    description             = "Type of instance to use"
}

variable "key_pair" {
    type                    = "string"
    description             = "SSH keypair to use for accessing instances"
}
