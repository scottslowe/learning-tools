// note: this variable will be fed with coreos_subnet subnet ID which can be found at terraform/aws/new-vpc/main.tf
variable "coreos_subnet_id" {
  type = "string"
  default = ""
  description = "AWS coreos_vpc subnet ID"
}