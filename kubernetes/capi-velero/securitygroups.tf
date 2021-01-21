# Create a security group for the SSH bastion host
resource "aws_security_group" "bastion-sg" {
  vpc_id      = aws_vpc.velero-test-vpc.id
  name        = "bastion-sg"
  description = "Security group for SSH bastion hosts"
  ingress {
    from_port   = "0"
    to_port     = "0"
    protocol    = "-1"
    cidr_blocks = [aws_vpc.velero-test-vpc.cidr_block]
  }
  ingress {
    from_port   = "22"
    to_port     = "22"
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  egress {
    from_port   = "0"
    to_port     = "0"
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags = {
    "Name" = "bastion-sg"
    "tool" = "terraform"
    "demo" = "velero-capi-testing"
  }
}

# Create a security group for the ELB for management cluster A
resource "aws_security_group" "mgmt-a-elb-sg" {
  vpc_id      = aws_vpc.velero-test-vpc.id
  name        = "mgmt-a-elb-sg"
  description = "ELB security group for mgmt cluster A"
  ingress {
    from_port   = "6443"
    to_port     = "6443"
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  egress {
    from_port   = "6443"
    to_port     = "6443"
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags = {
    "Name"                         = "mgmt-a-elb-sg"
    "tool"                         = "terraform"
    "demo"                         = "velero-capi-testing"
    "kubernetes.io/cluster/mgmt-a" = "shared"
  }
}

# Create a security group for the ELB for management cluster B
resource "aws_security_group" "mgmt-b-elb-sg" {
  vpc_id      = aws_vpc.velero-test-vpc.id
  name        = "mgmt-b-elb-sg"
  description = "ELB security group for mgmt cluster B"
  ingress {
    from_port   = "6443"
    to_port     = "6443"
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  egress {
    from_port   = "6443"
    to_port     = "6443"
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags = {
    "Name"                         = "mgmt-b-elb-sg"
    "tool"                         = "terraform"
    "demo"                         = "velero-capi-testing"
    "kubernetes.io/cluster/mgmt-b" = "shared"
  }
}

# Create a security group for control plane nodes
resource "aws_security_group" "controlplane-sg" {
  vpc_id      = aws_vpc.velero-test-vpc.id
  name        = "controlplane-sg"
  description = "Security group for K8s control plane nodes"
  ingress {
    from_port   = "0"
    to_port     = "0"
    protocol    = "-1"
    cidr_blocks = [aws_vpc.velero-test-vpc.cidr_block]
  }
  ingress {
    from_port       = "6443"
    to_port         = "6443"
    protocol        = "tcp"
    security_groups = [aws_security_group.mgmt-a-elb-sg.id, aws_security_group.mgmt-b-elb-sg.id]
  }
  ingress {
    from_port       = "22"
    to_port         = "22"
    protocol        = "tcp"
    security_groups = [aws_security_group.bastion-sg.id]
  }
  egress {
    from_port   = "0"
    to_port     = "0"
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags = {
    "Name"                         = "controlplane-sg"
    "tool"                         = "terraform"
    "demo"                         = "velero-capi-testing"
    "kubernetes.io/cluster/mgmt-a" = "shared"
    "kubernetes.io/cluster/mgmt-b" = "shared"
  }
}

# Create a security group for K8s worker nodes
resource "aws_security_group" "worker-sg" {
  vpc_id      = aws_vpc.velero-test-vpc.id
  name        = "worker-sg"
  description = "Security group for K8s worker nodes"
  ingress {
    from_port   = "0"
    to_port     = "0"
    protocol    = "-1"
    cidr_blocks = [aws_vpc.velero-test-vpc.cidr_block]
  }
  ingress {
    from_port       = "22"
    to_port         = "22"
    protocol        = "tcp"
    security_groups = [aws_security_group.bastion-sg.id]
  }
  egress {
    from_port   = "0"
    to_port     = "0"
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags = {
    "Name"                         = "worker-sg"
    "tool"                         = "terraform"
    "demo"                         = "velero-capi-testing"
    "kubernetes.io/cluster/mgmt-a" = "shared"
    "kubernetes.io/cluster/mgmt-b" = "shared"
  }
}
