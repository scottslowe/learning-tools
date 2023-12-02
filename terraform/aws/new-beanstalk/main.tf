resource "aws_elastic_beanstalk_application" "demo_beanstalk_application" {
  name        = var.demo_beanstalk_application_name
  description = var.demo_beanstalk_application_description
}

resource "aws_elastic_beanstalk_environment" "demo_beanstalk_environment" {
  name                = var.demo_beanstalk_environment_name
  application         = aws_elastic_beanstalk_application.demo_beanstalk_application.name
  // For all supported AWS BeanStalk Platforms / Stacks refer : https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/concepts.platforms.html
  solution_stack_name = var.demo_beanstalk_environment_platform
}