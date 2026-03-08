variable "name_prefix" {
  description = "Name prefix for all resources"
  type        = string
}

variable "environment" {
  description = "Environment name (e.g. prod, staging)"
  type        = string
}

variable "aws_region" {
  description = "AWS region"
  type        = string
}

# Lambda
variable "lambda_function_name" {
  description = "Lambda function name"
  type        = string
}

# API Gateway
variable "api_gateway_id" {
  description = "API Gateway v2 API ID"
  type        = string
}

variable "api_gateway_stage" {
  description = "API Gateway stage name"
  type        = string
  default     = "$default"
}

# RDS Aurora
variable "rds_cluster_identifier" {
  description = "RDS Aurora cluster identifier"
  type        = string
}

variable "rds_instance_identifier" {
  description = "RDS Aurora instance identifier"
  type        = string
}

# S3
variable "audio_bucket_name" {
  description = "S3 audio bucket name"
  type        = string
}

variable "frontend_bucket_name" {
  description = "S3 frontend bucket name"
  type        = string
}

# CloudFront
variable "cloudfront_distribution_id" {
  description = "CloudFront distribution ID"
  type        = string
}

# Alarms
variable "alarm_email" {
  description = "Email address for alarm notifications (optional)"
  type        = string
  default     = ""
}

variable "alarm_actions_enabled" {
  description = "Whether alarm actions are enabled"
  type        = bool
  default     = true
}

# Thresholds
variable "lambda_error_threshold" {
  description = "Lambda error count threshold for alarm"
  type        = number
  default     = 5
}

variable "lambda_duration_threshold_ms" {
  description = "Lambda duration threshold (ms) for alarm"
  type        = number
  default     = 15000
}

variable "lambda_throttle_threshold" {
  description = "Lambda throttle count threshold for alarm"
  type        = number
  default     = 1
}

variable "api_gateway_5xx_threshold" {
  description = "API Gateway 5XX error count threshold"
  type        = number
  default     = 5
}

variable "api_gateway_latency_threshold_ms" {
  description = "API Gateway p99 latency threshold (ms)"
  type        = number
  default     = 10000
}

variable "rds_cpu_threshold" {
  description = "RDS CPU utilization threshold (percent)"
  type        = number
  default     = 80
}

variable "rds_connections_threshold" {
  description = "RDS database connections threshold"
  type        = number
  default     = 50
}

variable "rds_acu_threshold" {
  description = "RDS ACU utilization threshold"
  type        = number
  default     = 1.5
}
