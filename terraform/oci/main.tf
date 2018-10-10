resource "oci_core_vcn" "test_vcn" {
  cidr_block = "10.18.0.0/16"
  compartment_id = "${var.oci_tenancy_ocid}"
  display_name = "test-vcn"
  dns_label = "testvcn"
}

resource "oci_core_subnet" "test_subnet" {
  availability_domain = "${lookup(data.oci_identity_availability_domains.oci_ads.availability_domains[0],"name")}"
  cidr_block = "10.18.1.0/24"
  compartment_id = "${var.oci_tenancy_ocid}"
  vcn_id = "${oci_core_vcn.test_vcn.id}"
  dns_label = "testsubnet"
  route_table_id = "${oci_core_route_table.test_rt.id}"
}

resource "oci_core_internet_gateway" "test_ig" {
  compartment_id = "${var.oci_tenancy_ocid}"
  display_name = "test-ig"
  vcn_id = "${oci_core_vcn.test_vcn.id}"
}

resource "oci_core_route_table" "test_rt" {
  compartment_id = "${var.oci_tenancy_ocid}"
  vcn_id = "${oci_core_vcn.test_vcn.id}"
  display_name = "test-route-table"

  route_rules {
    destination = "0.0.0.0/0"
    destination_type = "CIDR_BLOCK"
    network_entity_id = "${oci_core_internet_gateway.test_ig.id}"
  }
}

resource "oci_core_instance" "test" {
  availability_domain = "${lookup(data.oci_identity_availability_domains.oci_ads.availability_domains[0],"name")}"
  compartment_id      = "${var.oci_tenancy_ocid}"
  display_name        = "test-instance"
  shape               = "${var.oci_compute_shape}"

  create_vnic_details {
    subnet_id        = "${oci_core_subnet.test_subnet.id}"
    display_name     = "primary_vnic"
    assign_public_ip = true
    hostname_label   = "test-instance"
  }

  source_details {
    source_type = "image"
    source_id   = "${data.oci_core_images.oci_base_image.images.0.id}"
  }

  metadata {
    ssh_authorized_keys = "${file(var.ssh_key)}"
  }
}
