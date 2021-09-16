// Create a demo memcached cluster
resource "aws_elasticache_cluster" "demo_elasticache_memcached_cluster" {
  cluster_id           = var.demo_elasticache_memcached_cluster_name
  engine               = "memcached"
  // Using smallest node type. Refer AWS docs for all supported node types : https://docs.aws.amazon.com/AmazonElastiCache/latest/red-ug/CacheNodes.SupportedTypes.html
  node_type            = "cache.t3.micro"
  num_cache_nodes      = 2
  parameter_group_name = "default.memcached1.4"
  port                 = 11211
}

// Create a demo redis cluster
resource "aws_elasticache_cluster" "demo_elasticache_redis_cluster" {
  cluster_id           = var.demo_elasticache_redis_cluster_name
  engine               = "redis"
  // Using smallest node type. Refer AWS docs for all supported node types : https://docs.aws.amazon.com/AmazonElastiCache/latest/red-ug/CacheNodes.SupportedTypes.html
  node_type            = "cache.t3.micro"
  num_cache_nodes      = 1
  parameter_group_name = "default.redis3.2"
  engine_version       = "3.2.10"
  port                 = 6379
}