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

module "github" {
  source = "../modules/github"

  repository_name        = var.repository_name
  repository_description = var.repository_description
  topics                 = var.topics
  required_status_checks = var.required_status_checks
  required_approvals     = var.required_approvals
  actions_secrets        = var.actions_secrets
}
