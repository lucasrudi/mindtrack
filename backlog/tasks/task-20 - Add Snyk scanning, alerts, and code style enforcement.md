---
id: 20
title: Add Snyk scanning, alerts, and code style enforcement
status: To Do
priority: high
labels:
  - security
  - devops
  - ci-cd
  - backend
  - frontend
created: 2026-02-22 00:00
type: infrastructure
dependencies: []
---

## Description

Integrate Snyk for vulnerability scanning at build time and runtime. Make Snyk part of the verify stage (pre-push and release). Set up alerts and notifications for detected vulnerabilities. Update Checkstyle configuration to match IDE settings and enforce code style on the pre-commit hook.

## Plan

1. Add Snyk to `.githooks/pre-push` and GitHub Actions release workflow
2. Configure Snyk to scan Java dependencies (Maven), npm dependencies (frontend), and IaC (Terraform)
3. Set up Snyk alerts with notifications (email/Slack/GitHub issues) for new vulnerabilities
4. Review and update `config/checkstyle/checkstyle.xml` to match IntelliJ/VSCode IDE code style settings
5. Update `.githooks/pre-commit` to run Checkstyle on staged Java files
6. Verify `.snyk` policy file is correctly configured
7. Document Snyk setup and thresholds in README

## Acceptance Criteria

- [ ] Snyk runs automatically on pre-push and in release CI pipeline
- [ ] Snyk blocks push/release on high-severity vulnerabilities
- [ ] Alerts configured for new vulnerability discoveries
- [ ] Checkstyle config matches IntelliJ/VSCode IDE formatting rules
- [ ] Pre-commit hook enforces Checkstyle on staged Java files
- [ ] Existing code passes updated Checkstyle rules
- [ ] Snyk policy (`.snyk`) committed to repo
