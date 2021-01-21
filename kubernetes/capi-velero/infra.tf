# Get AZ information
data "aws_availability_zones" "azinfo" {
  state = "available"
}

# Create a new VPC
resource "aws_vpc" "velero-test-vpc" {
  cidr_block           = var.base_cidr
  enable_dns_hostnames = "true"
  enable_dns_support   = "true"
  tags = {
    "Name"                         = "velero-test-vpc"
    "tool"                         = "terraform"
    "demo"                         = "velero-capi-testing"
    "kubernetes.io/cluster/mgmt-a" = "shared"
    "kubernetes.io/cluster/mgmt-b" = "shared"
  }
}

# Create public subnets in the new VPC
resource "aws_subnet" "pub-subnet" {
  count      = length(data.aws_availability_zones.azinfo.names)
  vpc_id     = aws_vpc.velero-test-vpc.id
  cidr_block = cidrsubnet(var.base_cidr, 4, count.index)
  # cidr_block              = format("%s.%d.0/20", var.base_cidr, 32*count.index)
  availability_zone       = data.aws_availability_zones.azinfo.names[count.index]
  map_public_ip_on_launch = "true"
  tags = {
    "Name"                         = format("pub-subnet-%d", 1 + count.index)
    "tool"                         = "terraform"
    "demo"                         = "velero-capi-testing"
    "kubernetes.io/cluster/mgmt-a" = "shared"
    "kubernetes.io/cluster/mgmt-b" = "shared"
  }
}

# Create private subnets in the new VPC
resource "aws_subnet" "priv-subnet" {
  count      = length(data.aws_availability_zones.azinfo.names)
  vpc_id     = aws_vpc.velero-test-vpc.id
  cidr_block = cidrsubnet(var.base_cidr, 4, 15 - count.index)
  # cidr_block              = format("%s.%d.0/20", var.base_cidr, 16+(32*count.index))
  availability_zone       = data.aws_availability_zones.azinfo.names[count.index]
  map_public_ip_on_launch = "false"
  tags = {
    "Name"                         = format("priv-subnet-%d", 1 + count.index)
    "tool"                         = "terraform"
    "demo"                         = "velero-capi-testing"
    "kubernetes.io/cluster/mgmt-a" = "shared"
    "kubernetes.io/cluster/mgmt-b" = "shared"
  }
}

# Create an Internet gateway
resource "aws_internet_gateway" "inetgw" {
  vpc_id = aws_vpc.velero-test-vpc.id
  tags = {
    "Name"                         = "velero-inetgw"
    "tool"                         = "terraform"
    "demo"                         = "velero-capi-testing"
    "kubernetes.io/cluster/mgmt-a" = "shared"
    "kubernetes.io/cluster/mgmt-b" = "shared"
  }
}

# Create a route table for the public subnets
resource "aws_route_table" "inetgw-rte-tbl" {
  vpc_id = aws_vpc.velero-test-vpc.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.inetgw.id
  }
  tags = {
    "Name"                         = "inetgw-rte-tbl"
    "tool"                         = "terraform"
    "demo"                         = "velero-capi-testing"
    "kubernetes.io/cluster/mgmt-a" = "shared"
    "kubernetes.io/cluster/mgmt-b" = "shared"
  }
}

# Associate the public subnets with the route table
resource "aws_route_table_association" "pub-rta" {
  count          = length(aws_subnet.pub-subnet[*].id)
  subnet_id      = aws_subnet.pub-subnet[count.index].id
  route_table_id = aws_route_table.inetgw-rte-tbl.id
}

# Allocate an Elastic IP for the NAT gateway
resource "aws_eip" "eipnat" {
  vpc        = true
  depends_on = [aws_internet_gateway.inetgw]
  tags = {
    "Name"                         = "eipnat"
    "tool"                         = "terraform"
    "demo"                         = "velero-capi-testing"
    "kubernetes.io/cluster/mgmt-a" = "shared"
    "kubernetes.io/cluster/mgmt-b" = "shared"
  }
}

# Create a NAT gateway
resource "aws_nat_gateway" "natgw" {
  allocation_id = aws_eip.eipnat.id
  subnet_id     = aws_subnet.pub-subnet[0].id
  depends_on    = [aws_internet_gateway.inetgw]
  tags = {
    "Name"                         = "velero-natgw"
    "tool"                         = "terraform"
    "demo"                         = "velero-capi-testing"
    "kubernetes.io/cluster/mgmt-a" = "shared"
    "kubernetes.io/cluster/mgmt-b" = "shared"
  }
}

# Create a route table for the private subnets
resource "aws_route_table" "natgw-rte-tbl" {
  vpc_id = aws_vpc.velero-test-vpc.id
  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.natgw.id
  }
  depends_on = [aws_nat_gateway.natgw]
  tags = {
    "Name"                         = "inetgw-rte-tbl"
    "tool"                         = "terraform"
    "demo"                         = "velero-capi-testing"
    "kubernetes.io/cluster/mgmt-a" = "shared"
    "kubernetes.io/cluster/mgmt-b" = "shared"
  }
}

# Associate the private subnets with the route table
resource "aws_route_table_association" "priv-rta" {
  count          = length(aws_subnet.priv-subnet[*].id)
  subnet_id      = aws_subnet.priv-subnet[count.index].id
  route_table_id = aws_route_table.natgw-rte-tbl.id
}

# Create an ELB for mgmt cluster A
resource "aws_elb" "mgmt-a-elb" {
  name                      = "mgmt-a-elb"
  subnets                   = aws_subnet.pub-subnet.*.id
  cross_zone_load_balancing = true
  security_groups           = [aws_security_group.mgmt-a-elb-sg.id]
  listener {
    instance_port     = 6443
    instance_protocol = "tcp"
    lb_port           = 6443
    lb_protocol       = "tcp"
  }
  health_check {
    healthy_threshold   = 3
    unhealthy_threshold = 3
    timeout             = 5
    target              = "SSL:6443"
    interval            = 10
  }
  tags = {
    "Name"                         = "mgmt-a-elb"
    "tool"                         = "terraform"
    "demo"                         = "velero-capi-testing"
    "kubernetes.io/cluster/mgmt-a" = "shared"
  }
}

# Create an ELB for mgmt cluster B
resource "aws_elb" "mgmt-b-elb" {
  name                      = "mgmt-b-elb"
  subnets                   = aws_subnet.pub-subnet.*.id
  cross_zone_load_balancing = true
  security_groups           = [aws_security_group.mgmt-b-elb-sg.id]
  listener {
    instance_port     = 6443
    instance_protocol = "tcp"
    lb_port           = 6443
    lb_protocol       = "tcp"
  }
  health_check {
    healthy_threshold   = 3
    unhealthy_threshold = 3
    timeout             = 5
    target              = "SSL:6443"
    interval            = 10
  }
  tags = {
    "Name"                         = "mgmt-b-elb"
    "tool"                         = "terraform"
    "demo"                         = "velero-capi-testing"
    "kubernetes.io/cluster/mgmt-b" = "shared"
  }
}
