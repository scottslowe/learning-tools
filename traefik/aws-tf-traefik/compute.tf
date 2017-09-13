# Launch an instance to serve as a manager
resource "aws_instance" "manager" {
    ami                     = "${data.aws_ami.worker_ami.id}"
    instance_type           = "${var.mgr_flavor}"
    key_name                = "${var.keypair}"
    vpc_security_group_ids  = ["${aws_security_group.mgmt_sg.id}"]
    subnet_id               = "${aws_subnet.traefik_pub_subnet.id}"
    depends_on              = ["aws_internet_gateway.traefik_gw"]
    tags {
        Name                = "manager"
        tool                = "terraform"
        demo                = "traefik"
        area                = "compute"
        role                = "manager"
    }
}

# Launch one or more instances to serve as worker nodes
resource "aws_instance" "worker" {
    ami                     = "${data.aws_ami.worker_ami.id}"
    count                   = "${var.num_wkr_nodes}"
    instance_type           = "${var.wkr_flavor}"
    key_name                = "${var.keypair}"
    vpc_security_group_ids  = ["${aws_security_group.web_sg.id}"]
    subnet_id               = "${aws_subnet.traefik_pub_subnet.id}"
    depends_on              = ["aws_internet_gateway.traefik_gw"]
    tags {
        tool                = "terraform"
        demo                = "traefik"
        area                = "compute"
        role                = "worker"
    }
}
