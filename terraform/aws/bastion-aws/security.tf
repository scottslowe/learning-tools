# Create a security group to allow web traffic to remote host
resource "aws_security_group" "remote_sg" {
    vpc_id                  = "${aws_vpc.bastion_vpc.id}"
    name                    = "remote-sg"
    description             = "Security group for web traffic to remote hosts"
    ingress {
        from_port           = "80"
        to_port             = "80"
        protocol            = "tcp"
        cidr_blocks         = ["0.0.0.0/0"]
    }
    ingress {
        from_port           = "443"
        to_port             = "443"
        protocol            = "tcp"
        cidr_blocks         = ["0.0.0.0/0"]
    }
    ingress {
        from_port           = "22"
        to_port             = "22"
        protocol            = "tcp"
        cidr_blocks         = ["${aws_vpc.bastion_vpc.cidr_block}"]
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
