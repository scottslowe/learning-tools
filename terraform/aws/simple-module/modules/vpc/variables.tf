variable "name" {
    type                    = "string"
    description             = "Name prefix to use for networking resources"
}

variable "vpc_cidr_block" {
    type                    = "string"
    description             = "CIDR block to use for new VPC"
}

variable "vpc_dns_hostnames" {
    type                    = "string"
    description             = "True/False to enable DNS hostnames in new VPC"
}

variable "vpc_dns_support" {
    type                    = "string"
    description             = "True/False to enable DNS support in new VPC"
}

variable "subnet_cidr_block" {
    type                    = "string"
    description             = "CIDR block (in VPC CIDR block) to use for new subnet"
}

variable "subnet_map_pub_ip" {
    type                    = "string"
    description             = "True/False to map public IP addresses on launch"
}
