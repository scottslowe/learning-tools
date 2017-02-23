data "aws_ami" "ubuntu-1404-ami" {
    most_recent             = true
    owners                  = ["099720109477"]
    filter {
        name                = "root-device-type"
        values              = ["ebs"]
    }
    filter {
        name                = "architecture"
        values              = ["x86_64"]
    }
    filter {
        name                = "virtualization-type"
        values              = ["hvm"]
    }
    filter {
        name                = "name"
        values              = ["*ubuntu-trusty-14.04*"]
    }
}

data "aws_vpc" "default-vpc" {
    filter {
        name                = "isDefault"
        values              = ["true"]
    }
}

data "aws_subnet" "default-subnet" {
    filter {
        name                = "cidrBlock"
        values              = ["172.31.16.0/20"]
    }
}
