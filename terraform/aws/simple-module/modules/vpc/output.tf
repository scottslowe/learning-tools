output "new_vpc_id" {
    value                   = "${aws_vpc.vpc.id}"
}

output "new_subnet_id" {
    value                   = "${aws_subnet.subnet.id}"
}

output "new_subnet_az" {
    value                   = "${aws_subnet.subnet.availability_zone}"
}
