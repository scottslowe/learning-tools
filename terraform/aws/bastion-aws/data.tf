data "aws_ami" "atomic_ami" {
    filter {
        name                = "name"
        values              = ["*CentOS Atomic*1701*"]
    }
}

data "aws_ami" "centos_ami" {
    filter {
        name                = "name"
        values              = ["*CentOS 7.3.1611*"]
    }
}
