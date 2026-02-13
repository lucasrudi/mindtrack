variable "name_prefix" {
  description = "Prefix for resource names"
  type        = string
}

variable "min_capacity" {
  description = "Minimum ACU for Aurora Serverless v2"
  type        = number
}

variable "max_capacity" {
  description = "Maximum ACU for Aurora Serverless v2"
  type        = number
}

variable "lambda_sg_id" {
  description = "Security group ID of the Lambda function"
  type        = string
}
