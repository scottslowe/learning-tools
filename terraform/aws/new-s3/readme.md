## AWS S3

| Provider | Description |
|------|---------|
| aws | This code will create an AWS S3 bucket |

## Resources

| Name | Type |
|------|------|
| [aws_s3_bucket.demo_s3](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket) | resource |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| demo_s3_name | demo S3 bucket for terraform in learning-tools | `string` | `"learning-tools-demo-s3"` | yes |

## Outputs

| Name | Description |
|------|-------------|
| output_demo_s3_id | output S3 bucket resource ID |