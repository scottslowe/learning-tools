# Create a security group to allow SSH traffic to private host(s) only from bastion
resource "aws_security_group" "private_sg" {
    vpc_id                  = "${aws_vpc.bastion_vpc.id}"
    name                    = "private-sg"
    description             = "Security group for web traffic to private hosts"
    ingress {
        from_port           = "22"
        to_port             = "22"
        protocol            = "tcp"
        cidr_blocks         = ["10.2.1.0/24"]
    }
    egress {
        from_port           = "0"
        to_port             = "0"
        protocol            = "-1"
        cidr_blocks         = ["0.0.0.0/0"]
    }
    tags {
        tool                = "terraform"
        demo                = "bastion-aws"
        area                = "security"
    }
}

# Create a security group to allow inbound SSH (for bastion only)
resource "aws_security_group" "bastion_sg" {
    vpc_id                  = "${aws_vpc.bastion_vpc.id}"
    name                    = "bastion-sg"
    description             = "Security group for SSH bastion host"
    ingress {
        from_port           = "22"
        to_port             = "22"
        protocol            = "tcp"
        cidr_blocks         = ["0.0.0.0/0"]
    }
     egress {
        from_port           = "0"
        to_port             = "0"
        protocol            = "-1"
        cidr_blocks         = ["0.0.0.0/0"]
    }
    tags {
        tool                = "terraform"
        demo                = "bastion-aws"
        area                = "security"
    }
}
