## AWS IGW

| Provider | Description |
|------|---------|
| aws | This code will create an AWS Internet Gateway |

## Resources

| Name | Type |
|------|------|
| [aws_internet_gateway.demo_igw](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/internet_gateway) | resource |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| demo_vpc_id | AWS coreos\_vpc VPC ID | `string` | `""` | yes |

## Outputs

| Name | Description |
|------|-------------|
| output_demo_igw_id | output IGW resource ID |