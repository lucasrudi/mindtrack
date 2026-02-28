---
id: 29
title: Make Snyk and lint soft-fail builds with warnings
status: To Do
priority: medium
labels:
  - ci
  - security
created: 2026-02-28 00:00
type: chore
dependencies: []
---

## Description

Currently Snyk and lint failures block the CI pipeline entirely. Change both to soft-fail: report issues as warnings in the CI log and job summary, but do not fail the overall build. This prevents dependency CVEs or lint warnings from blocking deployments while still surfacing the issues visibly.

## Plan

1. In `feature.yml` and `verify.yml`, add `continue-on-error: true` to Snyk test steps (backend and frontend)
2. Add a step after each Snyk test to echo a warning summary if the scan found issues
3. For lint, change `npm run lint` to allow warnings: update `eslint` config or use `--max-warnings` with a high threshold, or mark the lint step with `continue-on-error: true`
4. Ensure Snyk and lint results are still visible in the GitHub Actions job summary (use `::warning::` annotations or upload a report artifact)
5. Update `.snyk` policy and document the soft-fail approach in `docs/testing.md`

## Acceptance Criteria

- [ ] Snyk failures do not block CI — pipeline continues and marks step as warning
- [ ] Lint failures do not block CI — pipeline continues and marks step as warning
- [ ] Issues remain visible in GitHub Actions job summary and logs
- [ ] `docs/testing.md` updated to describe soft-fail behaviour
- [ ] Pre-push hook behaviour unchanged (still enforces Snyk locally)
