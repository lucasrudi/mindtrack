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

variable "rds_sg_id" {
  description = "RDS security group ID"
  type        = string
}

variable "secrets_arns" {
  description = "List of Secrets Manager secret ARNs"
  type        = list(string)
}
