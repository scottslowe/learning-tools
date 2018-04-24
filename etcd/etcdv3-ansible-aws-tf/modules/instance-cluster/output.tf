output "cluster_instance_public_addresses" {
    value                   = "${aws_instance.instance.*.public_ip}"
}

output "cluster_instance_private_addresses" {
    value                   = "${aws_instance.instance.*.private_ip}"
}

output "cluster_instance_ids" {
    value                   = "${aws_instance.instance.*.id}"
}
