---
id: 27
title: Protect main branch and update infra github config
status: To Do
priority: high
labels:
  - infra
  - devops
  - security
created: 2026-02-28 00:00
type: chore
dependencies: [25]
---

## Description

Configure branch protection rules for `main` via Terraform (`infra/modules/github/`) to enforce required status checks, prevent force pushes, and require PR reviews. Ensure the Terraform config reflects the actual enforced state of the repository.

## Plan

1. Review current `github_branch_protection` resource in `infra/modules/github/main.tf`
2. Enable the following protections:
   - Required status checks: `Branch Name Check`, `Code Review`, `Backend Verify`, `Frontend Verify`, `Infrastructure Verify`
   - Dismiss stale reviews on new commits
   - Prevent force pushes to `main`
   - Prevent branch deletion
3. Apply via `github-config-sync` workflow
4. Verify protections are active in GitHub Settings > Branches

## Acceptance Criteria

- [ ] `main` branch protection rule managed entirely via Terraform
- [ ] Force pushes to `main` blocked
- [ ] All required CI checks enforced before merge
- [ ] Branch deletion disabled for `main`
- [ ] `github-config-sync` workflow applies successfully with no drift
- [ ] CLAUDE.md updated with branch protection notes if relevant
