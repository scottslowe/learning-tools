output "vpc_id" {
    value = aws_vpc.velero-test-vpc.id
}

output "pub_subnet_ids" {
    value = [aws_subnet.pub-subnet[*].id]
}

output "priv_subnet_ids" {
    value = [aws_subnet.priv-subnet[*].id]
}

output "bastion_pub_ip" {
    value = aws_instance.bastion.public_ip
}

output "mgmt_a_cp_priv_ip" {
    value = aws_instance.cp-mgmt-a.private_ip
}

output "wkr_a_cp_priv_ip" {
    value = aws_instance.wkr-mgmt-a.private_ip
}

output "mgmt_a_elb_dns" {
    value = aws_elb.mgmt-a-elb.dns_name
}

output "mgmt_b_cp_priv_ip" {
    value = aws_instance.cp-mgmt-b.private_ip
}

output "wkr_b_cp_priv_ip" {
    value = aws_instance.wkr-mgmt-b.private_ip
}

output "mgmt_b_elb_dns" {
    value = aws_elb.mgmt-b-elb.dns_name
}
