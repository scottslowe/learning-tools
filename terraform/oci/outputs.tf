output "instance_public_ip" {
  value = "${oci_core_instance.test.public_ip}"
}
