data "aws_ami" "coreos_stable" {
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
        values              = ["*1185.3.0*"]
    }
}
