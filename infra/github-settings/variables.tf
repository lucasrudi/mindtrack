variable "repository_name" {
  type = string
}

variable "repository_description" {
  type    = string
  default = "Personal mental health tracking application"
}

variable "topics" {
  type    = list(string)
  default = ["mental-health", "spring-boot", "vue", "terraform", "aws"]
}

variable "required_status_checks" {
  type = list(string)
  default = [
    "Backend Build & Test",
    "Frontend Build & Test",
    "Terraform Validate",
    "Branch Name Check",
    "Code Review"
  ]
}

variable "required_approvals" {
  type    = number
  default = 0
}

variable "actions_secrets" {
  description = "Map of GitHub Actions secrets (name => plaintext value). Pass at apply time, never hardcode."
  type        = map(string)
  default     = {}
  sensitive   = true
}
