// Create a Elastic IP for NAT GW
resource "aws_eip" "demo_gw_eip" {
  vpc          = true
  tags = {
    tool = "terraform"
  }
}

// Create a NAT GW in public1 and public2 subnets
resource "aws_nat_gateway" "ease_prod_vpc_nat_gws" {
  count         = length(var.ease_prod_vpc_public_subnets)
  allocation_id = aws_eip.demo_gw_eip.id
  # feed coreos_subnet public subnet ID
  subnet_id     = var.coreos_subnet_id

  tags = {
    tool = "terraform"
  }
  depends_on = [aws_eip.demo_gw_eip]
}