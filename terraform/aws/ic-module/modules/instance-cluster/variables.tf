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

variable "assign_pub_ip" {
    type                    = "string"
    description             = "True/False to assign a public IP address"
}

variable "ssh_key" {
    type                    = "string"
    description             = "SSH key to inject into instances in this instance cluster"
}

variable "cluster_size" {
    type                    = "string"
    description             = "Number of instances in this instance cluster"
}

variable "subnet_list" {
    type                    = "list"
    description             = "List of subnet IDs where to launch instances"
}

variable "sec_group_list" {
    type                    = "list"
    description             = "List of security group IDs for instances to use"
}

variable "role" {
    type                    = "string"
    description             = "Value to assign to the Role tag on instances"
}
