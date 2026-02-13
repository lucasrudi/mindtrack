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
