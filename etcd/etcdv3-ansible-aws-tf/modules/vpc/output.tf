output "vpc_id" {
    value                   = "${aws_vpc.vpc.id}"
}

output "subnet_id" {
    value                   = "${aws_subnet.subnet.*.id}"
}

output "subnet_az" {
    value                   = "${aws_subnet.subnet.*.availability_zone}"
}

output "default_sg_id" {
    value                   = "${aws_vpc.vpc.default_security_group_id}"
}
