variable "user_region" {
    type                    = "string"
    description             = "AWS region to use for new resources"
}

variable "assigned_cidr" {
    type                    = "string"
    description             = "CIDR block to use for VPC and subnets"
}

variable "node_type" {
    type                    = "string"
    description             = "Type of instance to launch (such as t2.micro)"
}

variable "key_pair" {
    type                    = "string"
    description             = "SSH keypair to inject into launched instances"
}

variable "num_nodes" {
    type                    = "string"
    description             = "Number of nodes to launch"
}
