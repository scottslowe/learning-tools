variable "keypair" {
    type                    = "string"
    description             = "AWS SSH keypair to use to connect to instances"
    default                 = "aws_rsa"
}

variable "flavor" {
    type                    = "string"
    description             = "AWS type to use when creating instances"
    default                 = "t2.micro"
}

variable "sec-group" {
    type                    = "string"
    description             = "AWS security group to apply to instances"
    default                 = "sg-7099b514"
}
