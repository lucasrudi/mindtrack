---
id: 34
title: Create dev and test infrastructure environments with separate users and roles
status: To Do
priority: high
labels:
  - infra
  - security
  - devops
created: 2026-02-28 00:00
type: feature
dependencies: [25, 27]
---

## Description

Provision two isolated AWS environments — `dev` and `test` — each with its own Terraform workspace, IAM users, roles, and least-privilege policies. The `dev` environment is used by developers for local testing against live AWS services; the `test` environment is used by the CI pipeline for integration and end-to-end tests. Keeping them separate prevents test runs from corrupting developer state and allows different permission boundaries per environment.

## Plan

1. **Terraform workspaces / directory structure**
   - Add `infra/envs/dev/` and `infra/envs/test/` directories, each with a `main.tf` that calls the shared modules with environment-specific variable overrides
   - Use a shared S3 backend with separate state keys: `mindtrack/dev/terraform.tfstate` and `mindtrack/test/terraform.tfstate`

2. **IAM users and roles — dev**
   - Create IAM user `mindtrack-dev-ci` with programmatic access (used by local dev scripts)
   - Create IAM role `mindtrack-dev-app` assumed by Lambda in dev (scoped to dev resources only)
   - Attach policies: read/write to dev S3 bucket, dev Aurora, dev Secrets Manager paths (`/mindtrack/dev/*`), dev CloudWatch log group

3. **IAM users and roles — test**
   - Create IAM user `mindtrack-test-ci` with programmatic access (used by GitHub Actions integration tests)
   - Create IAM role `mindtrack-test-app` assumed by Lambda in test
   - Attach policies: read/write to test S3 bucket, test Aurora, test Secrets Manager paths (`/mindtrack/test/*`), test CloudWatch log group
   - Deny cross-environment access explicitly

4. **Resource naming convention**
   - All resources tagged and prefixed: `mindtrack-dev-*` and `mindtrack-test-*`
   - Aurora cluster, S3 buckets, Secrets Manager secrets, Lambda functions, CloudWatch log groups — each duplicated per environment

5. **GitHub Actions secrets**
   - Add `AWS_ACCESS_KEY_ID_TEST` and `AWS_SECRET_ACCESS_KEY_TEST` (for `mindtrack-test-ci`)
   - Add `AWS_ACCESS_KEY_ID_DEV` and `AWS_SECRET_ACCESS_KEY_DEV` (for `mindtrack-dev-ci`)
   - Manage via `infra/modules/github/` Terraform config (task 25)

6. **CI pipeline integration**
   - Update `verify.yml` and future `daily-verify.yml` to use test environment credentials
   - Update `deploy.yml` to deploy to dev on merge to `main`, prod on GitHub Release

7. **Documentation**
   - Update `docs/deployment.md` with environment matrix (dev / test / prod)
   - Add IAM policy summary showing what each role can and cannot access

## Acceptance Criteria

- [ ] `infra/envs/dev/` and `infra/envs/test/` Terraform configs exist and `terraform plan` completes without error for both
- [ ] Separate IAM users (`mindtrack-dev-ci`, `mindtrack-test-ci`) created with scoped programmatic credentials
- [ ] Separate IAM roles (`mindtrack-dev-app`, `mindtrack-test-app`) created for Lambda execution
- [ ] Dev role cannot access test resources and vice versa (explicit deny tested)
- [ ] All resources named and tagged with environment prefix
- [ ] GitHub Actions secrets provisioned for both environments
- [ ] CI pipeline uses test environment credentials (not prod)
- [ ] `docs/deployment.md` updated with environment matrix and IAM role summary
