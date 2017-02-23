# Create a new Ubuntu 14.04 instance using values from vars.tf and data.tf
resource "aws_instance" "test-01" {
    ami                     = "${data.aws_ami.ubuntu-1404-ami.id}"
    instance_type           = "${var.flavor}"
    key_name                = "${var.keypair}"
    vpc_security_group_ids  = ["${var.sec-group}"]
    subnet_id               = "${data.aws_subnet.default-subnet.id}"
    tags {
        Name                = "terraform"
    }
}
