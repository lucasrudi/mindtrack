---
id: 32
title: Create daily clean build and verify workflow
status: Done
priority: medium
labels:
  - ci
  - devops
created: 2026-02-28 00:00
type: chore
dependencies: []
---

## Description

Add a scheduled GitHub Actions workflow that runs a full clean build and verification suite daily (backend, frontend, infra). This catches dependency drift, environment issues, and regressions that only appear with a clean Maven/npm cache — separate from the push-triggered `verify.yml`.

## Plan

1. Create `.github/workflows/daily-verify.yml` with `schedule: cron: '0 4 * * *'` (04:00 UTC daily)
2. Add `workflow_dispatch` for manual triggering
3. Run all three verify jobs (backend, frontend, infra) without using cached dependencies (`cache: false` or `mvn verify -U`)
4. Notify on failure (GitHub Actions built-in failure notification or Slack/email if configured)
5. Ensure the workflow uses the same Java and Node versions as `verify.yml`

## Acceptance Criteria

- [ ] Workflow runs daily at 04:00 UTC
- [ ] Workflow can be triggered manually via `workflow_dispatch`
- [ ] Backend: `mvn verify -U -B` (force dependency update)
- [ ] Frontend: `npm ci && npm run lint && npm run test:unit && npm run build`
- [ ] Infrastructure: terraform validate + tflint + tfsec
- [ ] Failure notification visible in GitHub Actions UI
- [ ] Workflow does not use cached artifacts from previous runs
