# Create a new VPC
resource "aws_vpc" "bastion_vpc" {
    cidr_block              = "10.2.0.0/16"
    enable_dns_hostnames    = true
    enable_dns_support      = true
    tags {
        tool                = "terraform"
        demo                = "bastion-aws"
        area                = "networking"
    }
}

# Create a public subnet in the new VPC
resource "aws_subnet" "bastion_net" {
    vpc_id                  = "${aws_vpc.bastion_vpc.id}"
    cidr_block              = "10.2.1.0/24"
    map_public_ip_on_launch = true
    tags {
        tool                = "terraform"
        demo                = "bastion-aws"
        area                = "networking"
    }
}

# Create a private subnet in the new VPC
resource "aws_subnet" "private_net" {
    vpc_id                  = "${aws_vpc.bastion_vpc.id}"
    cidr_block              = "10.2.2.0/24"
    map_public_ip_on_launch = false
    tags {
        tool                = "terraform"
        demo                = "bastion-aws"
        area                = "networking"
    }
}

# Create a new Internet gateway
resource "aws_internet_gateway" "bastion_gw" {
    vpc_id                  = "${aws_vpc.bastion_vpc.id}"
    tags {
        tool                = "terraform"
        demo                = "bastion-aws"
        area                = "networking"
    }
}

# Create a route table for the new VPC
resource "aws_route_table" "bastion_routes" {
    vpc_id                  = "${aws_vpc.bastion_vpc.id}"
    route {
        cidr_block          = "0.0.0.0/0"
        gateway_id          = "${aws_internet_gateway.bastion_gw.id}"
    }
    tags {
        tool                = "terraform"
        demo                = "bastion-aws"
        area                = "networking"
    }
}

# Associate route table with subnet in VPC
resource "aws_route_table_association" "bastion_rt_assoc" {
    subnet_id               = "${aws_subnet.bastion_net.id}"
    route_table_id          = "${aws_route_table.bastion_routes.id}"
}
