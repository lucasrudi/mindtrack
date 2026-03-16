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
