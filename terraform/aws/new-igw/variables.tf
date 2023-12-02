// note: this variable will be fed with coreos_vpc VPC ID which can be found at terraform/aws/new-vpc/main.tf
variable "demo_vpc_id" {
  type = "string"
  default = ""
  description = "AWS coreos_vpc VPC ID"
}