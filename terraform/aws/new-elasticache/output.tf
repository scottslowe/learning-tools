output "demo_elasticache_memcached_cluster_name" {
  value = "${aws_elasticache_cluster.demo_elasticache_memcached_cluster.cluster_id}"
}

output "demo_elasticache_redis_cluster_name" {
  value = "${aws_elasticache_cluster.demo_elasticache_redis_cluster.cluster_id}"
}