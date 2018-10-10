data "oci_identity_availability_domains" "oci_ads" {
  compartment_id = "${var.oci_tenancy_ocid}"
}

data "oci_core_images" "oci_base_image" {
  compartment_id = "${var.oci_tenancy_ocid}"
  display_name   = "${var.oci_image_name}"
}
