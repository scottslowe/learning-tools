resource "aws_internet_gateway" "demo_gw" {
  vpc_id = var.demo_vpc_id

  tags {
    tool = "terraform"
  }
}