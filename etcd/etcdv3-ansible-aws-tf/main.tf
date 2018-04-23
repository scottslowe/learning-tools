module "etcd-vpc" {
  source = "./modules/vpc"

  name              = "etcd"
  vpc_cidr_block    = "10.200.0.0/16"
  vpc_dns_hostnames = "true"
  vpc_dns_support   = "true"
  subnet_map_pub_ip = "true"
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
  sec_group_list = ["${module.etcd-vpc.default_sg_id}"]
  role           = "etcd"
}
