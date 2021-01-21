# Get the ID of a CAPA AMI
data "aws_ami" "capa_ami" {
  most_recent = true
  owners      = ["258751437250"]
  filter {
    name   = "name"
    values = ["capa-ami-ubuntu-18.04-1.18.2*"]
  }
  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
  filter {
    name   = "root-device-type"
    values = ["ebs"]
  }
  filter {
    name   = "architecture"
    values = ["x86_64"]
  }
}

# Launch an instance to serve as an SSH bastion host
resource "aws_instance" "bastion" {
  ami                    = data.aws_ami.capa_ami.id
  instance_type          = var.bastion_instance_type
  key_name               = var.keypair
  vpc_security_group_ids = [aws_security_group.bastion-sg.id]
  subnet_id              = aws_subnet.pub-subnet[0].id
  depends_on             = [aws_internet_gateway.inetgw]
  tags = {
    Name = "bastion-velero"
    tool = "terraform"
    demo = "velero-capi-testing"
    area = "compute"
  }
}

# Launch an instance to serve as a control plane node for mgmt cluster A
resource "aws_instance" "cp-mgmt-a" {
  ami                    = data.aws_ami.capa_ami.id
  instance_type          = var.cp_instance_type
  key_name               = var.keypair
  vpc_security_group_ids = [aws_security_group.controlplane-sg.id]
  subnet_id              = aws_subnet.priv-subnet[0].id
  iam_instance_profile   = "control-plane.cluster-api-provider-aws.sigs.k8s.io"
  depends_on             = [aws_internet_gateway.inetgw]
  tags = {
    "Name"                         = "cp-mgmt-a-velero"
    "tool"                         = "terraform"
    "demo"                         = "velero-capi-testing"
    "kubernetes.io/cluster/mgmt-a" = "shared"
  }
}

# Attach mgmt cluster A control plane node to mgmt cluster A ELB
resource "aws_elb_attachment" "mgmt-a-elb-attach" {
  elb        = aws_elb.mgmt-a-elb.id
  instance   = aws_instance.cp-mgmt-a.id
  depends_on = [aws_elb.mgmt-a-elb]
}

# Launch an instance to serve as a control plane node for mgmt cluster B
resource "aws_instance" "cp-mgmt-b" {
  ami                    = data.aws_ami.capa_ami.id
  instance_type          = var.cp_instance_type
  key_name               = var.keypair
  vpc_security_group_ids = [aws_security_group.controlplane-sg.id]
  subnet_id              = aws_subnet.priv-subnet[0].id
  iam_instance_profile   = "control-plane.cluster-api-provider-aws.sigs.k8s.io"
  depends_on             = [aws_internet_gateway.inetgw]
  tags = {
    "Name"                         = "cp-mgmt-b-velero"
    "tool"                         = "terraform"
    "demo"                         = "velero-capi-testing"
    "kubernetes.io/cluster/mgmt-b" = "shared"
  }
}

# Attach mgmt cluster B control plane node to mgmt cluster B ELB
resource "aws_elb_attachment" "mgmt-b-elb-attach" {
  elb        = aws_elb.mgmt-b-elb.id
  instance   = aws_instance.cp-mgmt-b.id
  depends_on = [aws_elb.mgmt-b-elb]
}

# Launch an instance to serve as a worker node in mgmt cluster A
resource "aws_instance" "wkr-mgmt-a" {
  ami                    = data.aws_ami.capa_ami.id
  instance_type          = var.cp_instance_type
  key_name               = var.keypair
  vpc_security_group_ids = [aws_security_group.worker-sg.id]
  subnet_id              = aws_subnet.priv-subnet[0].id
  iam_instance_profile   = "nodes.cluster-api-provider-aws.sigs.k8s.io"
  depends_on             = [aws_internet_gateway.inetgw]
  tags = {
    "Name"                         = "wkr-mgmt-a-velero"
    "tool"                         = "terraform"
    "demo"                         = "velero-capi-testing"
    "kubernetes.io/cluster/mgmt-a" = "shared"
  }
}

# Launch an instance to serve as a worker node in mgmt cluster B
resource "aws_instance" "wkr-mgmt-b" {
  ami                    = data.aws_ami.capa_ami.id
  instance_type          = var.cp_instance_type
  key_name               = var.keypair
  vpc_security_group_ids = [aws_security_group.worker-sg.id]
  subnet_id              = aws_subnet.priv-subnet[0].id
  iam_instance_profile   = "nodes.cluster-api-provider-aws.sigs.k8s.io"
  depends_on             = [aws_internet_gateway.inetgw]
  tags = {
    "Name"                         = "wkr-mgmt-b-velero"
    "tool"                         = "terraform"
    "demo"                         = "velero-capi-testing"
    "kubernetes.io/cluster/mgmt-b" = "shared"
  }
}
