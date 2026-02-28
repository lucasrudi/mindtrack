---
id: 25
title: Add SNYK_TOKEN and SONAR_TOKEN to infra github config
status: To Do
priority: high
labels:
  - infra
  - devops
  - security
created: 2026-02-28 00:00
type: chore
dependencies: []
---

## Description

Add `SNYK_TOKEN` and `SONAR_TOKEN` as managed GitHub Actions secrets in the Terraform GitHub config (`infra/modules/github/`). Currently these are set manually; they should be provisioned via Terraform alongside the other secrets so the full CI/CD setup is reproducible from code.

## Plan

1. Add `SNYK_TOKEN` and `SONAR_TOKEN` entries to the `actions_secrets` variable definition in `infra/modules/github/variables.tf`
2. Add corresponding secret resources in `infra/modules/github/main.tf` (or wherever `github_actions_secret` resources are defined)
3. Pass the values via `environments/prod.tfvars` or the Terraform apply command
4. Document the new variables in `docs/deployment.md` under GitHub Repository Secrets
5. Trigger `github-config-sync` workflow to apply changes

## Acceptance Criteria

- [ ] `SNYK_TOKEN` managed as a `github_actions_secret` resource in Terraform
- [ ] `SONAR_TOKEN` managed as a `github_actions_secret` resource in Terraform
- [ ] Values passed at apply time (not hardcoded in source)
- [ ] `docs/deployment.md` updated with the new secrets
- [ ] `github-config-sync` workflow applies successfully
