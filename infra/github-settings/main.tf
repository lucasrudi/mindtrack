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
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
  required_version = ">= 1.7.0"
}

provider "aws" {
  region = "us-east-1"
}

# Read the GitHub PAT from AWS Secrets Manager.
# Bootstrap: store the token first with:
#   aws secretsmanager create-secret \
#     --name mindtrack/github-config-token \
#     --secret-string "ghp_..."
data "aws_secretsmanager_secret_version" "gh_config_token" {
  secret_id = "mindtrack/github-config-token"
}

# Import blocks ensure Terraform manages existing resources instead of re-creating them.
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
  topics                 = var.topics
  required_status_checks = var.required_status_checks
  required_approvals     = var.required_approvals
  actions_secrets = merge(var.actions_secrets, {
    GH_CONFIG_TOKEN = data.aws_secretsmanager_secret_version.gh_config_token.secret_string
  })
  actions_variables = var.actions_variables

  enable_branch_protection = true
}
