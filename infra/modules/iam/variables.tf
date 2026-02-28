variable "name_prefix" {
  description = "Prefix for resource names"
  type        = string
}

variable "github_org" {
  description = "GitHub organization or username that owns the repository"
  type        = string
}

variable "github_repo" {
  description = "GitHub repository name"
  type        = string
}

variable "create_oidc_provider" {
  description = "Whether to create the GitHub OIDC Identity Provider. Set to true only in one environment per AWS account to avoid conflicts."
  type        = bool
  default     = false
}

variable "audio_bucket_arn" {
  description = "ARN of the S3 audio bucket"
  type        = string
}

variable "secrets_arns" {
  description = "List of Secrets Manager secret ARNs"
  type        = list(string)
}
