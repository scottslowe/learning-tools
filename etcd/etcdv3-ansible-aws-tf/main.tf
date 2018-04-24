module "etcd-vpc" {
  source = "./modules/vpc"

  name              = "etcd"
  vpc_cidr_block    = "10.200.0.0/16"
  vpc_dns_hostnames = "true"
  vpc_dns_support   = "true"
  subnet_map_pub_ip = "true"
}

resource "aws_security_group" "etcd_sg" {
  name        = "etcd_sg"
  description = "Allow traffic needed by etcd"
  vpc_id      = "${module.etcd-vpc.vpc_id}"
}

resource "aws_security_group_rule" "etcd_sg_allow_sg_in" {
  security_group_id        = "${aws_security_group.etcd_sg.id}"
  type                     = "ingress"
  from_port                = 0
  to_port                  = 0
  protocol                 = "-1"
  source_security_group_id = "${aws_security_group.etcd_sg.id}"
}

resource "aws_security_group_rule" "etcd_sg_allow_sg_out" {
  security_group_id        = "${aws_security_group.etcd_sg.id}"
  type                     = "egress"
  from_port                = 0
  to_port                  = 0
  protocol                 = "-1"
  source_security_group_id = "${aws_security_group.etcd_sg.id}"
}

resource "aws_security_group_rule" "etcd_sg_allow_client" {
  security_group_id = "${aws_security_group.etcd_sg.id}"
  type              = "ingress"
  from_port         = 2379
  to_port           = 2379
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
}

resource "aws_security_group_rule" "etcd_sg_allow_peer" {
  security_group_id = "${aws_security_group.etcd_sg.id}"
  type              = "ingress"
  from_port         = 2380
  to_port           = 2380
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
}

module "etcd" {
  source = "./modules/instance-cluster"

  name           = "etcd"
  ami            = "${data.aws_ami.node_ami.id}"
  type           = "${var.node_type}"
  assign_pub_ip  = true
  ssh_key        = "${var.key_pair}"
  cluster_size   = 3
  subnet_list    = ["${module.etcd-vpc.subnet_id}"]
  sec_group_list = ["${module.etcd-vpc.default_sg_id}", "${aws_security_group.etcd_sg.id}"]
  role           = "etcd"
}
