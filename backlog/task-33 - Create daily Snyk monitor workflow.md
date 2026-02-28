---
id: 33
title: Create daily Snyk monitor workflow
status: Done
priority: medium
labels:
  - ci
  - security
  - devops
created: 2026-02-28 00:00
type: chore
dependencies: [25]
---

## Description

Add a scheduled GitHub Actions workflow that runs `snyk monitor` daily to continuously track the project's dependencies in the Snyk dashboard. Unlike `snyk test` (which blocks on vulnerabilities), `snyk monitor` sends a snapshot to Snyk for ongoing monitoring and alerting without failing the build.

## Plan

1. Create `.github/workflows/snyk-monitor.yml` with `schedule: cron: '0 3 * * *'` (03:00 UTC daily)
2. Add `workflow_dispatch` for manual triggering
3. Add steps:
   - `snyk monitor --file=backend/pom.xml --project-name=mindtrack-backend`
   - `snyk monitor --file=frontend/package.json --project-name=mindtrack-frontend`
4. Use `SNYK_TOKEN` secret (already in repo)
5. Add `continue-on-error: true` so a Snyk API issue doesn't fail the daily run
6. Document the monitoring workflow in `docs/testing.md`

## Acceptance Criteria

- [ ] Workflow runs daily at 03:00 UTC
- [ ] Workflow can be triggered manually via `workflow_dispatch`
- [ ] `snyk monitor` snapshots sent for both backend and frontend
- [ ] Projects appear in Snyk dashboard under the correct project names
- [ ] Workflow does not fail the build on Snyk API errors (`continue-on-error: true`)
- [ ] `docs/testing.md` updated with Snyk monitoring notes
