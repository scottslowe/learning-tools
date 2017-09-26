variable "name" {
    type                    = "string"
    description             = "Name to use for instances in this instance cluster"
}

variable "ami" {
    type                    = "string"
    description             = "AMI to use for instances in this instance cluster"
}

variable "type" {
    type                    = "string"
    description             = "Type to use for instances in this instance cluster"
}

variable "ssh_key" {
    type                    = "string"
    description             = "SSH key to inject into instances in this instance cluster"
}

variable "assign_pub_ip" {
    type                    = "string"
    description             = "True/False to assign a public IP address"
}

variable "sec_group_list" {
    type                    = "list"
    description             = "List of security group IDs for instances to use"
}

variable "min_size" {
    type                    = "string"
    description             = "Minimum number of instances in this instance cluster"
}

variable "max_size" {
    type                    = "string"
    description             = "Maximum number of instances in this instance cluster"
}

variable "subnet_list" {
    type                    = "list"
    description             = "List of subnets where instances should be launched"
}
