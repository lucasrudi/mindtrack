---
id: 30
title: Create full deploy stage workflow
status: To Do
priority: high
labels:
  - ci
  - infra
  - devops
created: 2026-02-28 00:00
type: feature
dependencies: [25, 26, 27]
---

## Description

Create or consolidate a comprehensive deploy workflow triggered on GitHub Release published. The workflow should: apply Terraform infrastructure changes, deploy the backend Lambda, deploy the frontend to S3/CloudFront, create a GitHub Release (via release-please), and publish the documentation site.

## Plan

1. Review existing `.github/workflows/deploy.yml` and `.github/workflows/release.yml`
2. Add/update stages in sequence:
   - **Infrastructure:** `terraform apply` via OIDC credentials
   - **Backend:** build JAR, deploy to AWS Lambda (update function code)
   - **Frontend:** `npm run build`, sync to S3, invalidate CloudFront
   - **Documentation:** generate Javadoc + frontend docs, publish to GitHub Pages or S3
3. Add job dependencies (`needs:`) to enforce correct ordering
4. Gate the deploy on all verify jobs passing
5. Update `docs/deployment.md` with the automated deploy flow description

## Acceptance Criteria

- [ ] Terraform apply runs and completes without error on release
- [ ] Backend Lambda updated with new JAR on every release
- [ ] Frontend deployed to S3 and CloudFront cache invalidated
- [ ] Documentation site updated on every release
- [ ] Deploy workflow gated on passing CI (verify jobs)
- [ ] Rollback procedure documented in `docs/deployment.md`
- [ ] Workflow tested end-to-end on a real release
