output "instance_public_ip" {
    value = "${aws_instance.coreos01.public_ip}"
}
