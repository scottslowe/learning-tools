# Create a new VPC
resource "aws_vpc" "coreos_vpc" {
    cidr_block              = "10.1.0.0/16"
    enable_dns_hostnames    = "true"
    enable_dns_support      = "true"
    tags {
        tool                = "terraform"
    }
}

# Create a subnet in the new VPC
resource "aws_subnet" "coreos_subnet" {
    vpc_id                  = "${aws_vpc.coreos_vpc.id}"
    cidr_block              = "10.1.1.0/24"
    map_public_ip_on_launch = "true"
    tags {
        tool                = "terraform"
    }
}

# Create a new security group
resource "aws_security_group" "allow_inbound_ssh" {
    vpc_id                  = "${aws_vpc.coreos_vpc.id}"
    name                    = "allow-inbound-ssh"
    description             = "Allows inbound SSH"
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
    }
}

# Create a new Internet gateway
resource "aws_internet_gateway" "coreos_gw" {
    vpc_id                  = "${aws_vpc.coreos_vpc.id}"
    tags {
        tool                = "terraform"
    }
}

# Create a route table for the new VPC
resource "aws_route_table" "coreos_vpc_rt" {
    vpc_id                  = "${aws_vpc.coreos_vpc.id}"
    route {
        cidr_block          = "0.0.0.0/0"
        gateway_id          = "${aws_internet_gateway.coreos_gw.id}"
    }
    tags {
        tool                = "terraform"
    }
}

# Associate route table with subnet in VPC
resource "aws_route_table_association" "coreos_vpc_rta" {
    subnet_id               = "${aws_subnet.coreos_subnet.id}"
    route_table_id          = "${aws_route_table.coreos_vpc_rt.id}"
}

# Launch a new CoreOS instance in the new subnet and VPC
resource "aws_instance" "coreos01" {
    ami                     = "${data.aws_ami.coreos_stable.id}"
    instance_type           = "${var.flavor}"
    key_name                = "${var.keypair}"
    vpc_security_group_ids  = ["${aws_security_group.allow_inbound_ssh.id}"]
    subnet_id               = "${aws_subnet.coreos_subnet.id}"
    depends_on              = ["aws_internet_gateway.coreos_gw"]
    tags {
        tool                = "terraform"
    }
}
