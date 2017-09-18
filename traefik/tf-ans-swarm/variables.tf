variable "keypair" {
    type                    = "string"
    description             = "SSH keypair to use to connect to instances"
}

variable "mgr_type" {
    type                    = "string"
    description             = "AWS type to use when creating manager instances"
}

variable "wkr_type" {
    type                    = "string"
    description             = "AWS type to use when creating worker instances"
}

variable "user_region" {
    type                    = "string"
    description             = "AWS region to use for new resources"
}

variable "num_wkr_nodes" {
    type                    = "string"
    description             = "Number of worker nodes to create"
}
