module "vpc20" {
    source                  = "./modules/vpc"

    name                    = "test-vpc-1"
    vpc_cidr_block          = "10.20.0.0/16"
    vpc_dns_hostnames       = "true"
    vpc_dns_support         = "true"

    subnet_cidr_block       = "10.20.1.0/24"
    subnet_map_pub_ip       = "true"    
}

module "vpc50" {
    source                  = "./modules/vpc"

    name                    = "test-vpc-2"
    vpc_cidr_block          = "10.50.0.0/16"
    vpc_dns_hostnames       = "true"
    vpc_dns_support         = "true"

    subnet_cidr_block       = "10.50.1.0/24"
    subnet_map_pub_ip       = "true"    
}
