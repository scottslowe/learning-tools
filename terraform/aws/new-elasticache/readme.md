## AWS Elasticache

| Provider | Description |
|------|---------|
| aws | This code will create AWS Elasticache memcached and redis clusters |

## Resources

| Name | Type |
|------|------|
| [aws_elasticache_cluster.demo_elasticache_memcached_cluster](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/elasticache_cluster) | resource |
| [aws_elasticache_cluster.demo_elasticache_redis_cluster](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/elasticache_cluster) | resource |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| demo_elasticache_memcached_cluster_name | AWS memcached elasticache cluster name | `string` | `""` | yes |
| demo_elasticache_redis_cluster_name | AWS redis elasticache cluster name | `string` | `""` | yes |

## Outputs

| Name | Description |
|------|-------------|
| demo_elasticache_memcached_cluster_name | output memcached elasticache cluster name |
| demo_elasticache_redis_cluster_name | output redis elasticache cluster name |