## AWS BeanStalk

| Provider | Description |
|------|---------|
| aws | This code will create AWS BeanStalk application and environment |

## Resources

| Name | Type |
|------|------|
| [aws_elastic_beanstalk_application.demo_beanstalk_application](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/elastic_beanstalk_application) | resource |
| [aws_elastic_beanstalk_environment.demo_beanstalk_environment](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/elastic_beanstalk_environment) | resource |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| demo_beanstalk_application_name | AWS beanstalk application name | `string` | `""` | yes |
| demo_beanstalk_application_description | AWS beanstalk application description | `string` | `""` | no |
| demo_beanstalk_environment_name | AWS beanstalk environment name | `string` | `""` | yes |
| demo_beanstalk_environment_platform | AWS beanstalk environment platform | `string` | `"64bit Amazon Linux 2015.03 v2.0.3 running Go 1.4"` | yes |

## Outputs

| Name | Description |
|------|-------------|
| demo_beanstalk_application_name | output beanstalk application name |
| demo_beanstalk_environment_name | output beanstalk environment name |