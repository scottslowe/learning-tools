// Define application variables
variable "demo_beanstalk_application_name" {
  type = "string"
  default = ""
  description = "AWS beanstalk application name"
}

variable "demo_beanstalk_application_description" {
  type = "string"
  default = ""
  description = "AWS beanstalk application description"
}

// Define environment variables
variable "demo_beanstalk_environment_name" {
  type = "string"
  default = ""
  description = "AWS beanstalk environment name"
}

variable "demo_beanstalk_environment_platform" {
  type = "string"
  default = "64bit Amazon Linux 2015.03 v2.0.3 running Go 1.4"
  description = "AWS beanstalk environment platform"
}