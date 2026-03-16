output "cluster_endpoint" {
  description = "RDS instance endpoint"
  value       = aws_db_instance.main.address
}

output "cluster_port" {
  description = "RDS instance port"
  value       = aws_db_instance.main.port
}

output "cluster_identifier" {
  description = "RDS instance identifier"
  value       = aws_db_instance.main.identifier
}

output "instance_identifier" {
  description = "RDS instance identifier"
  value       = aws_db_instance.main.identifier
}

output "security_group_id" {
  description = "RDS security group ID"
  value       = aws_security_group.rds.id
}
