# Create a new VPC
resource "aws_vpc" "traefik_vpc" {
    cidr_block              = "10.1.0.0/16"
    enable_dns_hostnames    = "true"
    enable_dns_support      = "true"
    tags {
        Name                = "traefik_vpc"
        tool                = "terraform"
        demo                = "traefik"
        area                = "networking"
    }
}

# Create a public subnet in the new VPC
resource "aws_subnet" "traefik_pub_subnet" {
    vpc_id                  = "${aws_vpc.traefik_vpc.id}"
    cidr_block              = "10.1.1.0/24"
    map_public_ip_on_launch = "true"
    tags {
        Name                = "traefik_pub_subnet"
        tool                = "terraform"
        demo                = "traefik"
        area                = "networking"
    }
}

# Create a new Internet gateway
resource "aws_internet_gateway" "traefik_gw" {
    vpc_id                  = "${aws_vpc.traefik_vpc.id}"
    tags {
        Name                = "traefik_gw"
        tool                = "terraform"
        demo                = "traefik"
        area                = "networking"
    }
}

# Create a route table for the new VPC
resource "aws_route_table" "traefik_rte_tbl" {
    vpc_id                  = "${aws_vpc.traefik_vpc.id}"
    route {
        cidr_block          = "0.0.0.0/0"
        gateway_id          = "${aws_internet_gateway.traefik_gw.id}"
    }
    tags {
        Name                = "traefik_rte_tbl"
        tool                = "terraform"
        demo                = "traefik"
        area                = "networking"
    }
}

# Associate route table with subnet in VPC
resource "aws_route_table_association" "traefik_rta" {
    subnet_id               = "${aws_subnet.traefik_pub_subnet.id}"
    route_table_id          = "${aws_route_table.traefik_rte_tbl.id}"
}
