# Create a launch configuration for the instance cluster
resource "aws_launch_configuration" "lc" {
    name                        = "${var.name}-lc"
    associate_public_ip_address = "${var.assign_pub_ip}"
    image_id                    = "${var.ami}"
    instance_type               = "${var.type}"
    key_name                    = "${var.ssh_key}"
    security_groups             = ["${var.sec_group_list}"]

    lifecycle {
        create_before_destroy   = true
    }
}

# Create an Auto Scaling group for the instance cluster
resource "aws_autoscaling_group" "asg" {
    name                        = "${var.name}-asg"
    min_size                    = "${var.min_size}"
    max_size                    = "${var.max_size}"
    desired_capacity            = "${var.min_size}"
    vpc_zone_identifier         = ["${var.subnet_list}"]
    launch_configuration        = "${aws_launch_configuration.lc.name}"

    lifecycle {
        create_before_destroy   = true
    }
}
