data "aws_ami" "node_ami" {
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
