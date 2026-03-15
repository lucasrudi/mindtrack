variable "name_prefix" {
  description = "Prefix for resource names"
  type        = string
}

variable "memory_size" {
  description = "Lambda function memory in MB"
  type        = number
}

variable "role_arn" {
  description = "ARN of the Lambda execution IAM role"
  type        = string
}

variable "rds_endpoint" {
  description = "RDS Aurora cluster endpoint"
  type        = string
}

variable "rds_port" {
  description = "RDS Aurora cluster port"
  type        = number
}

variable "secrets_arns" {
  description = "List of Secrets Manager secret ARNs"
  type        = list(string)
}

variable "vpc_id" {
  description = "VPC ID for the Lambda security group"
  type        = string
}

variable "subnet_ids" {
  description = "Private subnet IDs for VPC config"
  type        = list(string)
}

variable "vpc_cidr_block" {
  description = "VPC CIDR block for the MySQL egress rule"
  type        = string
}

variable "encryption_key_arn" {
  description = "ARN of the KMS key for application-level PII column encryption"
  type        = string
}
