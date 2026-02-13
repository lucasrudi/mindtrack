variable "name_prefix" {
  description = "Prefix for resource names"
  type        = string
}

variable "audio_bucket_arn" {
  description = "ARN of the S3 audio bucket"
  type        = string
}

variable "secrets_arns" {
  description = "List of Secrets Manager secret ARNs"
  type        = list(string)
}
