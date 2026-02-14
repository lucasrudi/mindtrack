output "cluster_endpoint" {
  description = "Aurora cluster endpoint"
  value       = aws_rds_cluster.main.endpoint
}

output "cluster_port" {
  description = "Aurora cluster port"
  value       = aws_rds_cluster.main.port
}

output "cluster_identifier" {
  description = "Aurora cluster identifier"
  value       = aws_rds_cluster.main.cluster_identifier
}

output "instance_identifier" {
  description = "Aurora instance identifier"
  value       = aws_rds_cluster_instance.main.identifier
}

output "security_group_id" {
  description = "RDS security group ID"
  value       = aws_security_group.rds.id
}
