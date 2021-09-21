## AWS NAT GW

| Provider | Description |
|------|---------|
| aws | This code will create an AWS NAT Gateway |

## Resources

| Name | Type |
|------|------|
| [aws_eip.demo_gw_eip](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/eip) | resource |
| [aws_nat_gateway.demo_natgw](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/nat_gateway) | resource |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| coreos_subnet_id | AWS coreos\_vpc subnet ID | `string` | `""` | yes |

## Outputs

| Name | Description |
|------|-------------|
| output_demo_natgw_id | output NAT GW resource ID |