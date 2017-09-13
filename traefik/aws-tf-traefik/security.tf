# Create a security group to allow inbound web traffic
resource "aws_security_group" "web_sg" {
    vpc_id                  = "${aws_vpc.traefik_vpc.id}"
    name                    = "web_sg"
    description             = "Security group for inbound web traffic"
    ingress {
        from_port           = "0"
        to_port             = "0"
        protocol            = "-1"
        cidr_blocks         = ["${aws_vpc.traefik_vpc.cidr_block}"]
    }
    ingress {
        from_port           = "8080"
        to_port             = "8080"
        protocol            = "tcp"
        cidr_blocks         = ["0.0.0.0/0"]
    }
    ingress {
        from_port           = "80"
        to_port             = "80"
        protocol            = "tcp"
        cidr_blocks         = ["0.0.0.0/0"]
    }
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
        Name                = "web_sg"
        tool                = "terraform"
        demo                = "traefik"
        area                = "security"
    }
}

# Create a security group to allow inbound Docker management traffic
resource "aws_security_group" "mgmt_sg" {
    vpc_id                  = "${aws_vpc.traefik_vpc.id}"
    name                    = "mgmt_sg"
    description             = "Security group for inbound Docker management traffic"
    ingress {
        from_port           = "0"
        to_port             = "0"
        protocol            = "-1"
        cidr_blocks         = ["${aws_vpc.traefik_vpc.cidr_block}"]
    }
    ingress {
        from_port           = "8080"
        to_port             = "8080"
        protocol            = "tcp"
        cidr_blocks         = ["0.0.0.0/0"]
    }
    ingress {
        from_port           = "80"
        to_port             = "80"
        protocol            = "tcp"
        cidr_blocks         = ["0.0.0.0/0"]
    }
    ingress {
        from_port           = "22"
        to_port             = "22"
        protocol            = "tcp"
        cidr_blocks         = ["0.0.0.0/0"]
    }
    ingress {
        from_port           = "2376"
        to_port             = "2377"
        protocol            = "tcp"
        cidr_blocks         = ["0.0.0.0/0"]
    }
    ingress {
        from_port           = "3376"
        to_port             = "3377"
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
        Name                = "mgmt_sg"
        tool                = "terraform"
        demo                = "traefik"
        area                = "security"
    }
}
