output "bastion_pub_ip" {
    value = ["${aws_instance.bastion.public_ip}"]
}

output "bastion_priv_ip" {
    value = ["${aws_instance.bastion.private_ip}"]
}

output "remote_priv_ip" {
    value = ["${aws_instance.private.private_ip}"]
}
