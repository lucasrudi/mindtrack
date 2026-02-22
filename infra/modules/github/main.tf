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
  has_downloads   = false
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

  lifecycle {
    prevent_destroy = true
  }
}

# ------------------------------------
# Branch Protection
# ------------------------------------
resource "github_branch_protection" "main" {
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

  enforce_admins = false

  allows_deletions    = false
  allows_force_pushes = false
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
  for_each = var.actions_secrets

  repository      = github_repository.this.name
  secret_name     = each.key
  plaintext_value = each.value
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
