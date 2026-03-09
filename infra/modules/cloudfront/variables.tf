variable "name_prefix" {
  description = "Prefix for resource names"
  type        = string
}

variable "frontend_bucket_domain" {
  description = "Regional domain name of the frontend S3 bucket"
  type        = string
}

variable "frontend_bucket_id" {
  description = "ID of the frontend S3 bucket"
  type        = string
}

variable "domain_name" {
  description = "Custom domain name (optional)"
  type        = string
  default     = ""
}

variable "acm_certificate_arn" {
  description = "ACM certificate ARN for HTTPS (must be in us-east-1). If empty, uses default CloudFront certificate."
  type        = string
  default     = ""
}

variable "access_logs_bucket_domain" {
  description = "Domain name of the S3 access logs bucket for CloudFront logging"
  type        = string
}
