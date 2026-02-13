variable "name_prefix" {
  description = "Prefix for resource names"
  type        = string
}

variable "lambda_function_arn" {
  description = "ARN of the Lambda function to invoke"
  type        = string
}
