data "aws_ami" "f26_atomic_ami" {
    most_recent             = true
    filter {
        name                = "name"
        values              = ["*Fedora-Atomic-26*"]
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

data "aws_ami" "f26_ami" {
    most_recent             = true
    filter {
        name                = "name"
        values              = ["*Fedora-Cloud-Base-26*"]
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
