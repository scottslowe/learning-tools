resource "openstack_networking_network_v2" "tf-net" {
	name = "tf-net"
	admin_state_up = "true"
}

resource "openstack_networking_subnet_v2" "tf-subnet" {
	name = "tf-subnet"
	network_id = "${openstack_networking_network_v2.tf-net.id}"
	cidr = "192.168.200.0/24"
	ip_version = 4
	dns_nameservers = ["8.8.8.8","8.8.4.4"]
}

resource "openstack_networking_router_v2" "tf-router" {
	name = "tf-router"
	admin_state_up = "true"
	external_gateway = "${var.external_gateway}"
}

resource "openstack_networking_router_interface_v2" "tf-router-interface" {
	router_id = "${openstack_networking_router_v2.tf-router.id}"
	subnet_id = "${openstack_networking_subnet_v2.tf-subnet.id}"
}

resource "openstack_networking_floatingip_v2" "tf-fip" {
	pool = "${var.pool}"
	depends_on = ["openstack_networking_router_interface_v2.tf-router-interface"]
}

resource "openstack_compute_instance_v2" "tf-instance" {
	name = "tf-instance"
	image_name = "${var.image}"
	flavor_name = "${var.flavor}"
	key_pair = "${var.key_pair}"
	security_groups = ["default"]
	floating_ip = "${openstack_networking_floatingip_v2.tf-fip.address}"
	network {
		uuid = "${openstack_networking_network_v2.tf-net.id}"
	}
}
