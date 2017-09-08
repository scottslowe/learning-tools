# Create an ELB
resource "aws_elb" "elb" {
    name                        = "elb-test"
    subnets                     = ["subnet-69e39630","subnet-e0702d97","subnet-c4e9d6a1"]
    cross_zone_load_balancing   = true
    connection_draining         = true
    security_groups             = ["${var.secgrp}"]

    listener {
        instance_port           = "22"
        instance_protocol       = "tcp"
        lb_port                 = "22"
        lb_protocol             = "tcp"
    }

    tags {
        Name                    = "elb-test"
    }
}

# Create a launch configuration
resource "aws_launch_configuration" "lc" {
    name                        = "lc-test"
    image_id                    = "${data.aws_ami.atomic_ami.id}"
    instance_type               = "${var.flavor}"
    security_groups             = ["${var.secgrp}"]
    key_name                    = "${var.keypair}"

    lifecycle {
        create_before_destroy   = true
    }
}

# Create an Auto Scaling group
resource "aws_autoscaling_group" "asg" {
    name                        = "asg-test"
    availability_zones          = ["us-west-2a","us-west-2b","us-west-2c"]
    min_size                    = "2"
    max_size                    = "2"
    desired_capacity            = "2"
    launch_configuration        = "${aws_launch_configuration.lc.name}"
    load_balancers              = ["${aws_elb.elb.name}"]

    lifecycle {
        create_before_destroy   = true
    }
}
