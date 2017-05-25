# Create a new VPC
resource "aws_vpc" "vpc" {
    cidr_block              = "${var.vpc_cidr_block}"
    enable_dns_hostnames    = "${var.vpc_dns_hostnames}"
    enable_dns_support      = "${var.vpc_dns_support}"
}

# Create a public subnet in the new VPC
resource "aws_subnet" "subnet" {
    vpc_id                  = "${aws_vpc.vpc.id}"
    cidr_block              = "${var.subnet_cidr_block}"
    map_public_ip_on_launch = "${var.subnet_map_pub_ip}"
}

# Create a new Internet gateway
resource "aws_internet_gateway" "gateway" {
    vpc_id                  = "${aws_vpc.vpc.id}"
}

# Create a route table for the new VPC
resource "aws_route_table" "routes" {
    vpc_id                  = "${aws_vpc.vpc.id}"
}

# Create a route in new route table
resource "aws_route" "default_route" {
    route_table_id          = "${aws_route_table.routes.id}"
    destination_cidr_block  = "0.0.0.0/0"
    gateway_id              = "${aws_internet_gateway.gateway.id}"
}

# Associate route table with subnet in VPC
resource "aws_route_table_association" "rte_tbl_assoc" {
    subnet_id               = "${aws_subnet.subnet.id}"
    route_table_id          = "${aws_route_table.routes.id}"
}
