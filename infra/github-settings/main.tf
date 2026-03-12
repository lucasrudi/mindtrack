terraform {
  backend "s3" {
    bucket         = "mindtrack-terraform-state"
    key            = "github/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "mindtrack-terraform-locks"
    encrypt        = true
  }
  required_providers {
    github = {
      source  = "integrations/github"
      version = "~> 6.0"
    }
  }
  required_version = ">= 1.7.0"
}

# Terraform 1.7+ supports for_each in import blocks.
import {
  to = module.github.github_repository.this
  id = var.repository_name
}

# Import GitHub's built-in labels that would otherwise conflict on create.
import {
  for_each = toset(["bug", "documentation", "enhancement"])
  to       = module.github.github_issue_label.labels[each.key]
  id       = "${var.repository_name}:${each.key}"
}

module "github" {
  source = "../modules/github"

  repository_name        = var.repository_name
  repository_description = var.repository_description
  visibility             = "public"
  topics                 = var.topics
  required_status_checks = var.required_status_checks
  required_approvals     = 1
  actions_secrets        = var.actions_secrets
  actions_variables      = var.actions_variables

  enable_branch_protection           = true
  enable_secret_scanning             = true
  enable_dependabot_security_updates = true

  # Private vulnerability reporting and default CodeQL setup are enabled on the
  # repository. Terraform management is not yet possible — see the comments in
  # modules/github/main.tf for the manual REST API / GitHub UI steps.
  enable_private_vulnerability_reporting = true
  enable_default_codeql_setup            = true
}
