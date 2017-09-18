data "aws_ami" "xenial_ami" {
    most_recent             = true
    owners                  = ["099720109477"]
    filter {
        name                = "name"
        values              = ["*ubuntu-trusty-14*"]
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
