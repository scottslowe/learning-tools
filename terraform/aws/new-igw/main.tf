resource "aws_internet_gateway" "demo_igw" {
  vpc_id = var.demo_vpc_id

  tags {
    tool = "terraform"
  }
}