// Create a demo S3 bucket
resource "aws_s3_bucket" "demo_s3" {
  bucket = var.demo_s3_name
  acl    = "private"

  # enable versioning on bucket
  versioning {
    enabled = true
  }

  tags = {
    Name  = "learning-tools"
    tools = "terraform"
  }
}