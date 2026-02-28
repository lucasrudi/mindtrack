---
id: 31
title: Add environment variables section to README
status: To Do
priority: low
labels:
  - docs
created: 2026-02-28 00:00
type: chore
dependencies: []
---

## Description

Add a section to `README.md` (or a dedicated `docs/environment-variables.md`) that lists all environment variables required to run MindTrack — for local development, CI, and production. Include variable name, description, example value, and where it is sourced from (Secrets Manager, GitHub Actions secret, `.env` file, etc.).

## Plan

1. Audit all `application.yml`, `application-local.yml`, `application-prod.yml` files for `${...}` environment variable references
2. Audit GitHub Actions workflows for `secrets.*` and `vars.*` references
3. Compile a complete table: `Variable | Description | Example | Source`
4. Add the table to `README.md` or create `docs/environment-variables.md` and link from README
5. Cross-reference with `docs/deployment.md` to avoid duplication

## Acceptance Criteria

- [ ] All environment variables documented (backend, frontend build-time, CI)
- [ ] Table includes: name, description, example value, source
- [ ] README.md links to the new section or file
- [ ] No variables missing — verified against application config files and workflows
