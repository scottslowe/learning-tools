# Create a new VPC
resource "aws_vpc" "demo_vpc" {
    cidr_block              = "${var.assigned_cidr}"
    enable_dns_hostnames    = "true"
    enable_dns_support      = "true"
    tags {
        Name                = "multi_az_vpc"
        tool                = "terraform"
        demo                = "terraform"
        area                = "networking"
    }
}

# Modify default security group to allow inbound SSH
resource "aws_security_group_rule" "allow_ssh" {
    type                    = "ingress"
    from_port               = 22
    to_port                 = 22
    protocol                = "tcp"
    cidr_blocks             = ["0.0.0.0/0"]
    security_group_id       = "${aws_vpc.demo_vpc.default_security_group_id}"
}

# Create a new Internet gateway
resource "aws_internet_gateway" "demo_gw" {
    vpc_id                  = "${aws_vpc.demo_vpc.id}"
    tags {
        Name                = "multi_az_gw"
        tool                = "terraform"
        demo                = "terraform"
        area                = "networking"
    }
}

# Add default route to VPC's main route table
resource "aws_route" "default" {
    route_table_id          = "${aws_vpc.demo_vpc.main_route_table_id}"
    destination_cidr_block  = "0.0.0.0/0"
    gateway_id              = "${aws_internet_gateway.demo_gw.id}"
}

# Create public subnet(s) in the new VPC
resource "aws_subnet" "demo_subnet" {
    count                   = "${length(data.aws_availability_zones.avail_zones.names)}"
    vpc_id                  = "${aws_vpc.demo_vpc.id}"
    cidr_block              = "${cidrsubnet(var.assigned_cidr, 8, count.index)}"
    availability_zone       = "${element(data.aws_availability_zones.avail_zones.names, count.index)}"
    map_public_ip_on_launch = "true"
    tags {
        Name                = "demo_subnet_${count.index}"
        tool                = "terraform"
        demo                = "terraform"
        area                = "networking"
    }
}
