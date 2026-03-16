variable "name_prefix" {
  description = "Prefix for resource names"
  type        = string
}

variable "lambda_sg_id" {
  description = "Security group ID of the Lambda function"
  type        = string
}

variable "vpc_id" {
  description = "VPC ID for the RDS security group"
  type        = string
}

variable "subnet_ids" {
  description = "Private subnet IDs for the DB subnet group"
  type        = list(string)
}

variable "enable_performance_insights" {
  description = "Whether to enable RDS Performance Insights for the instance"
  type        = bool
  default     = false
}
