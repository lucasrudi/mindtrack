output "vpc_id" {
  description = "VPC ID"
  value       = aws_vpc.main.id
}

output "private_subnet_ids" {
  description = "IDs of the two private subnets"
  value       = aws_subnet.private[*].id
}

output "vpc_cidr_block" {
  description = "VPC CIDR block (used for Lambda→RDS egress rule)"
  value       = aws_vpc.main.cidr_block
}
