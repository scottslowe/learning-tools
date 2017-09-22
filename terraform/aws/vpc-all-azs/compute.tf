# Launch instances across subnets/AZs
resource "aws_instance" "node" {
    count                   = "${var.num_nodes}"
    ami                     = "${data.aws_ami.node_ami.id}"
    instance_type           = "${var.node_type}"
    key_name                = "${var.key_pair}"
    vpc_security_group_ids  = ["${aws_vpc.demo_vpc.default_security_group_id}"]
    subnet_id               = "${element(aws_subnet.demo_subnet.*.id, count.index)}"
    tags {
        Name                = "node-${count.index}"
        tool                = "terraform"
        demo                = "terraform"
        area                = "compute"
    }
}
