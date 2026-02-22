---
id: 19
title: Externalize environment config and provision via Terraform
status: Done
priority: high
labels:
  - infrastructure
  - backend
  - devops
created: 2026-02-22 00:00
type: infrastructure
dependencies: []
---

## Description

Externalize all environment-specific configuration from the application (DB credentials, API keys, OAuth secrets, etc.) so they are injected via environment variables or a config file. Provision and manage these as GitHub Actions secrets/variables using the existing GitHub Terraform module. Update README with setup instructions.

## Plan

1. Audit all hardcoded or profile-specific config values in backend (application*.yml) and frontend
2. Replace with environment variable references using Spring's `${ENV_VAR}` syntax and Vite's `import.meta.env`
3. Create a `.env.example` file documenting all required variables
4. Extend `infra/modules/github/` Terraform to declare GitHub Actions secrets and variables
5. Update CI workflow to consume secrets from GitHub Actions environment
6. Update README with local setup instructions and required env variables

## Acceptance Criteria

- [ ] No secrets or environment-specific values hardcoded in source files
- [ ] All required env vars documented in `.env.example`
- [ ] GitHub Actions secrets/variables managed via Terraform
- [ ] Local dev setup instructions updated in README
- [ ] Application starts correctly with env-injected config locally and in CI
- [ ] Terraform plan applies cleanly
