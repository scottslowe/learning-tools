output "manager_pub_ip" {
    value = ["${aws_instance.manager.public_ip}"]
}

output "worker_pub_ips" {
    value = ["${aws_instance.worker.*.public_ip}"]
}
