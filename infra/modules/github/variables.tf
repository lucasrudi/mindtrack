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
  default     = 2
}

variable "enable_branch_protection" {
  description = "Enable branch protection rules. Requires GitHub Pro for private repositories; set to false on GitHub Free."
  type        = bool
  default     = true
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

variable "enable_secret_scanning" {
  description = "Enable secret scanning and push protection. Works on public repos without GitHub Advanced Security."
  type        = bool
  default     = true
}

variable "enable_dependabot_security_updates" {
  description = "Enable Dependabot security updates (automatic PRs for vulnerable dependencies). Requires vulnerability_alerts = true."
  type        = bool
  default     = true
}

variable "actions_secrets" {
  description = "Map of GitHub Actions secrets to create (name => plaintext value). Values are sensitive."
  type        = map(string)
  default     = {}
  sensitive   = true
}

variable "actions_variables" {
  description = "Map of GitHub Actions variables to create (name => value)"
  type        = map(string)
  default     = {}
}

variable "enable_private_vulnerability_reporting" {
  description = "Enable private vulnerability reporting so security researchers can disclose vulnerabilities privately. NOTE: The integrations/github Terraform provider v6.x does not expose this setting as a resource attribute. Enable it manually via GitHub UI (Security > Private vulnerability reporting) or via the REST API: PUT /repos/{owner}/{repo}/private-vulnerability-reporting. This variable is reserved for when provider support is added."
  type        = bool
  default     = true
}

variable "enable_default_codeql_setup" {
  description = "Enable GitHub's default CodeQL code scanning setup (no custom workflow required). NOTE: The integrations/github Terraform provider v6.x does not expose a resource for CodeQL default setup. Enable it manually via GitHub UI (Security > Code scanning > Set up > Default) or via the REST API: PATCH /repos/{owner}/{repo}/code-scanning/default-setup with {\"state\":\"configured\"}. This variable is reserved for when provider support is added."
  type        = bool
  default     = true
}
