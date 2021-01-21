variable "keypair" {
  type        = string
  description = "SSH keypair to use to connect to instances"
}

variable "cp_instance_type" {
  type        = string
  description = "AWS type to use when creating manager instances"
  default     = "t2.medium"
}

variable "wkr_instance_type" {
  type        = string
  description = "AWS type to use when creating worker instances"
  default     = "t2.large"
}

variable "bastion_instance_type" {
  type        = string
  description = "AWS type to use when creating SSH bastion instances"
  default     = "t2.small"
}

variable "user_region" {
  type        = string
  description = "AWS region to use for new resources"
}

variable "num_wkr_nodes" {
  type        = number
  description = "Number of worker nodes to create"
  default     = 1
}

variable "base_cidr" {
  type        = string
  description = "Base CIDR for the infrastructure"
}
