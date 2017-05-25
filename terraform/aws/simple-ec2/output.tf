output "instance_ip_address" {
    value = "${aws_instance.test-01.public_ip}"
}
