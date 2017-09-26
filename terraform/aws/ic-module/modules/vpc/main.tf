# Create a new VPC
resource "aws_vpc" "vpc" {
    cidr_block              = "${var.vpc_cidr_block}"
    enable_dns_hostnames    = "${var.vpc_dns_hostnames}"
    enable_dns_support      = "${var.vpc_dns_support}"

    tags {
        Name                = "${var.name}_vpc"
        tool                = "terraform"
    }
}

# Modify default VPC security group to allow inbound SSH
resource "aws_security_group_rule" "allow_ssh" {
    type                    = "ingress"
    from_port               = 22
    to_port                 = 22
    protocol                = "tcp"
    cidr_blocks             = ["0.0.0.0/0"]
    security_group_id       = "${aws_vpc.vpc.default_security_group_id}"
}

# Create a new Internet gateway
resource "aws_internet_gateway" "gateway" {
    vpc_id                  = "${aws_vpc.vpc.id}"

    tags {
        Name                = "${var.name}_igw"
        tool                = "terraform"
    }
}

# Add default route to VPC's main route table
resource "aws_route" "default_route" {
    route_table_id          = "${aws_vpc.vpc.main_route_table_id}"
    destination_cidr_block  = "0.0.0.0/0"
    gateway_id              = "${aws_internet_gateway.gateway.id}"
}

# Create a public subnet in the new VPC
resource "aws_subnet" "subnet" {
    count                   = "${length(data.aws_availability_zones.az_list.names)}"
    vpc_id                  = "${aws_vpc.vpc.id}"
    cidr_block              = "${cidrsubnet(var.vpc_cidr_block, 4, count.index)}"
    availability_zone       = "${element(data.aws_availability_zones.az_list.names, count.index)}"
    map_public_ip_on_launch = "${var.subnet_map_pub_ip}"

    tags {
        Name                = "${var.name}_subnet_${count.index}"
        tool                = "terraform"
    }
}
