output "demo_beanstalk_application_name" {
  value = "${aws_elastic_beanstalk_application.demo_beanstalk_application.name}"
}

output "demo_beanstalk_environment_name" {
  value = "${aws_elastic_beanstalk_environment.demo_beanstalk_environment.name}"
}