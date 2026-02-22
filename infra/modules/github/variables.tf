variable "repository_name" {
  description = "GitHub repository name"
  type        = string
}

variable "repository_description" {
  description = "GitHub repository description"
  type        = string
  default     = ""
}

variable "visibility" {
  description = "Repository visibility: public or private"
  type        = string
  default     = "private"
}

variable "homepage_url" {
  description = "Repository homepage URL"
  type        = string
  default     = ""
}

variable "topics" {
  description = "Repository topics/tags"
  type        = list(string)
  default     = []
}

variable "required_status_checks" {
  description = "List of required status check contexts for branch protection"
  type        = list(string)
  default = [
    "Backend Build & Test",
    "Frontend Build & Test",
    "Terraform Validate",
    "Branch Name Check",
    "Code Review"
  ]
}

variable "required_approvals" {
  description = "Number of required PR approvals"
  type        = number
  default     = 1
}

variable "labels" {
  description = "Map of issue labels to create"
  type = map(object({
    color       = string
    description = string
  }))
  default = {
    "backend" = {
      color       = "0075ca"
      description = "Backend (Java/Spring Boot) changes"
    }
    "frontend" = {
      color       = "7057ff"
      description = "Frontend (Vue.js/TypeScript) changes"
    }
    "infra" = {
      color       = "d73a4a"
      description = "Infrastructure (Terraform/AWS) changes"
    }
    "dependencies" = {
      color       = "0366d6"
      description = "Dependency updates"
    }
    "security" = {
      color       = "e4e669"
      description = "Security vulnerabilities"
    }
    "breaking" = {
      color       = "b60205"
      description = "Breaking changes"
    }
    "bug" = {
      color       = "d73a4a"
      description = "Something isn't working"
    }
    "enhancement" = {
      color       = "a2eeef"
      description = "New feature or request"
    }
    "documentation" = {
      color       = "0075ca"
      description = "Improvements or additions to documentation"
    }
  }
}

variable "actions_secrets" {
  description = "Map of GitHub Actions secrets to create (name => plaintext value). Values are sensitive."
  type        = map(string)
  default     = {}
}

variable "actions_variables" {
  description = "Map of GitHub Actions variables to create (name => value)"
  type        = map(string)
  default     = {}
}
