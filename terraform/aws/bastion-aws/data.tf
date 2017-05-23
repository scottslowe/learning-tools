data "aws_ami" "atomic_ami" {
    most_recent             = true
    owners                  = ["410186602215"]
    filter {
        name                = "name"
        values              = ["CentOS Atomic Host 7*"]
    }
    filter {
        name                = "virtualization-type"
        values              = ["hvm"]
    }
}

data "aws_ami" "centos_ami" {
    most_recent             = true
    owners                  = ["410186602215"]
    filter {
        name                = "name"
        values              = ["CentOS Linux 7*"]
    }
    filter {
        name                = "virtualization-type"
        values              = ["hvm"]
    }
}
