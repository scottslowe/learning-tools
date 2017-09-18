# Create a security group for worker nodes
resource "aws_security_group" "wkr_sg" {
    vpc_id                  = "${aws_vpc.traefik_vpc.id}"
    name                    = "wkr_sg"
    description             = "Security group for worker nodes"
    ingress {
        from_port           = "0"
        to_port             = "0"
        protocol            = "-1"
        cidr_blocks         = ["${aws_vpc.traefik_vpc.cidr_block}"]
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
        Name                = "wkr_sg"
        tool                = "terraform"
        demo                = "traefik"
        area                = "security"
    }
}

# Create a security group for the manager
resource "aws_security_group" "mgr_sg" {
    vpc_id                  = "${aws_vpc.traefik_vpc.id}"
    name                    = "mgr_sg"
    description             = "Security group for the manager node"
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
        Name                = "mgr_sg"
        tool                = "terraform"
        demo                = "traefik"
        area                = "security"
    }
}
