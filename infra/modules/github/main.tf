terraform {
  required_providers {
    github = {
      source  = "integrations/github"
      version = "~> 6.0"
    }
  }
}

# ------------------------------------
# Repository Settings
# ------------------------------------
resource "github_repository" "this" {
  name        = var.repository_name
  description = var.repository_description
  visibility  = var.visibility

  has_issues      = true
  has_discussions = false
  has_projects    = true
  has_wiki        = false
  homepage_url    = var.homepage_url
  topics          = var.topics

  allow_merge_commit = false
  allow_squash_merge = true
  allow_rebase_merge = false
  allow_auto_merge   = true

  squash_merge_commit_title   = "PR_TITLE"
  squash_merge_commit_message = "COMMIT_MESSAGES"

  delete_branch_on_merge = true

  vulnerability_alerts = true

  security_and_analysis {
    secret_scanning {
      status = var.enable_secret_scanning ? "enabled" : "disabled"
    }
    secret_scanning_push_protection {
      status = var.enable_secret_scanning ? "enabled" : "disabled"
    }
  }

  lifecycle {
    prevent_destroy = true
  }
}

# ------------------------------------
# Branch Protection
# Requires GitHub Pro (or public repo) for private repositories.
# Set enable_branch_protection = false on GitHub Free with private repos.
# ------------------------------------
resource "github_branch_protection" "main" {
  count = var.enable_branch_protection ? 1 : 0

  repository_id = github_repository.this.node_id
  pattern       = "main"

  required_status_checks {
    strict   = true
    contexts = var.required_status_checks
  }

  required_pull_request_reviews {
    required_approving_review_count = var.required_approvals
    dismiss_stale_reviews           = true
    require_code_owner_reviews      = false
  }

  enforce_admins                  = true
  require_conversation_resolution = true

  allows_deletions    = false
  allows_force_pushes = false
}

# ------------------------------------
# Dependabot Security Updates
# Automatically opens PRs to fix vulnerable dependencies.
# Requires vulnerability_alerts = true on the repository.
# ------------------------------------
resource "github_repository_dependabot_security_updates" "this" {
  count = var.enable_dependabot_security_updates ? 1 : 0

  repository = github_repository.this.name
  enabled    = true

  depends_on = [github_repository.this]
}

# ------------------------------------
# Labels
# ------------------------------------
resource "github_issue_label" "labels" {
  for_each = var.labels

  repository  = github_repository.this.name
  name        = each.key
  color       = each.value.color
  description = each.value.description
}

# ------------------------------------
# GitHub Actions Secrets
# ------------------------------------
resource "github_actions_secret" "secrets" {
  for_each = nonsensitive(toset(keys(var.actions_secrets)))

  repository      = github_repository.this.name
  secret_name     = each.key
  plaintext_value = var.actions_secrets[each.key]
}

# ------------------------------------
# GitHub Actions Variables
# ------------------------------------
resource "github_actions_variable" "variables" {
  for_each = var.actions_variables

  repository    = github_repository.this.name
  variable_name = each.key
  value         = each.value
}

# ------------------------------------
# Private Vulnerability Reporting
# ------------------------------------
# The integrations/github Terraform provider v6.x does not expose private
# vulnerability reporting as a settable resource attribute. The Go client
# struct (PrivateVulnerabilityReporting) exists internally but is not surfaced
# in the Terraform schema for github_repository or any standalone resource.
#
# To manage this setting outside of Terraform use the GitHub REST API:
#   Enable:  PUT  https://api.github.com/repos/{owner}/{repo}/private-vulnerability-reporting
#   Disable: DELETE https://api.github.com/repos/{owner}/{repo}/private-vulnerability-reporting
#   Status:  GET  https://api.github.com/repos/{owner}/{repo}/private-vulnerability-reporting
#
# Or enable it in the GitHub UI under:
#   Settings > Security > Private vulnerability reporting
#
# var.enable_private_vulnerability_reporting = true is the desired state.
# Track provider support at: https://github.com/integrations/terraform-provider-github/issues

# ------------------------------------
# Default CodeQL Setup (Code Scanning)
# ------------------------------------
# The integrations/github Terraform provider v6.x does not include a resource
# for GitHub's default CodeQL setup. There is no github_repository_code_scanning
# or equivalent resource in the provider schema.
#
# To manage this setting outside of Terraform use the GitHub REST API:
#   PATCH https://api.github.com/repos/{owner}/{repo}/code-scanning/default-setup
#   Body: {"state": "configured", "query_suite": "default"}
#
# Or enable it in the GitHub UI under:
#   Security > Code scanning > Set up > Default
#
# var.enable_default_codeql_setup = true is the desired state.
# Track provider support at: https://github.com/integrations/terraform-provider-github/issues
