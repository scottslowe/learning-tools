# Launch a CentOS 7 instance to serve as bastion host
resource "aws_instance" "bastion" {
    ami                         = "${data.aws_ami.centos_ami.id}"
    instance_type               = "${var.flavor}"
    key_name                    = "${var.keypair}"
    vpc_security_group_ids      = ["${aws_security_group.bastion_sg.id}"]
    subnet_id                   = "${aws_subnet.bastion_net.id}"
    depends_on                  = ["aws_internet_gateway.bastion_gw"]
    tags {
        tool                    = "terraform"
        demo                    = "bastion-aws"
        area                    = "instances"
    }
}

# Launch a second CentOS instance to serve as a remote host
resource "aws_instance" "remote" {
    ami                         = "${data.aws_ami.centos_ami.id}"
    instance_type               = "${var.flavor}"
    key_name                    = "${var.keypair}"
    vpc_security_group_ids      = ["${aws_security_group.remote_sg.id}"]
    subnet_id                   = "${aws_subnet.bastion_net.id}"
    depends_on                  = ["aws_internet_gateway.bastion_gw"]
    associate_public_ip_address = false
    tags {
        tool                    = "terraform"
        demo                    = "bastion-aws"
        area                    = "instances"
    }
}
