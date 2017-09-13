data "aws_ami" "worker_ami" {
    most_recent             = true
    owners                  = ["595879546273"]
    filter {
        name                = "name"
        values              = ["*CoreOS-stable*"]
    }
    filter {
        name                = "virtualization-type"
        values              = ["hvm"]
    }
    filter {
        name                = "root-device-type"
        values              = ["ebs"]
    }
}

data "aws_ami" "manager_ami" {
    most_recent             = true
    owners                  = ["099720109477"]
    filter {
        name                = "name"
        values              = ["*ubuntu-xenial-16.04*"]
    }
    filter {
        name                = "virtualization-type"
        values              = ["hvm"]
    }
    filter {
        name                = "root-device-type"
        values              = ["ebs"]
    }
}
