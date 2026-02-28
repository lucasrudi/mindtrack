---
id: 28
title: Create a GitHub Action to run Renovate weekly
status: Done
priority: medium
labels:
  - ci
  - devops
  - dependencies
created: 2026-02-28 00:00
type: chore
dependencies: []
---

## Description

Add a scheduled GitHub Actions workflow that runs Renovate on a weekly cadence to keep dependencies up to date. `renovate.json` already exists in the repo; this task wires it into a self-hosted or GitHub-app-triggered workflow so dependency PRs are opened automatically each week.

## Plan

1. Create `.github/workflows/renovate.yml` with a `schedule: cron` trigger (e.g. Mondays at 06:00 UTC)
2. Use the `renovatebot/github-action` action (or `renovate` Docker image) to run Renovate with the existing `renovate.json` config
3. Configure the workflow to use a `RENOVATE_TOKEN` secret (PAT with `repo` scope, or use the GitHub App)
4. Add `RENOVATE_TOKEN` to the GitHub secrets table in `docs/deployment.md`
5. Test by triggering the workflow manually (`workflow_dispatch`)

## Acceptance Criteria

- [ ] Renovate workflow runs on weekly schedule (Monday 06:00 UTC)
- [ ] Workflow can be triggered manually via `workflow_dispatch`
- [ ] Renovate opens dependency update PRs using existing `renovate.json` config
- [ ] `RENOVATE_TOKEN` documented in `docs/deployment.md`
- [ ] No duplicate PRs opened for already-open Renovate PRs
