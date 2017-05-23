# Launch a CentOS 7 instance to serve as bastion host
resource "aws_instance" "bastion" {
    ami                         = "${data.aws_ami.centos_ami.id}"
    instance_type               = "${var.flavor}"
    key_name                    = "${var.keypair}"
    vpc_security_group_ids      = ["${aws_security_group.bastion_sg.id}"]
    subnet_id                   = "${aws_subnet.bastion_net.id}"
    depends_on                  = ["aws_internet_gateway.bastion_gw"]
    tags {
        Name                    = "bastion"
        tool                    = "terraform"
        demo                    = "bastion-aws"
        area                    = "instances"
    }
}

# Launch a CentOS Atomic Host instance to serve as a private host
resource "aws_instance" "private" {
    ami                         = "${data.aws_ami.atomic_ami.id}"
    instance_type               = "${var.flavor}"
    key_name                    = "${var.keypair}"
    vpc_security_group_ids      = ["${aws_security_group.private_sg.id}"]
    subnet_id                   = "${aws_subnet.private_net.id}"
    depends_on                  = ["aws_internet_gateway.bastion_gw"]
    associate_public_ip_address = false
    tags {
        Name                    = "private"
        tool                    = "terraform"
        demo                    = "bastion-aws"
        area                    = "instances"
    }
}
