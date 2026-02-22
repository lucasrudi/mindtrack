---
id: 22
title: Set up GitHub Issues for task tracking with commit prefixes
status: To Do
priority: high
labels:
  - devops
  - chore
created: 2026-02-22 00:00
type: chore
dependencies: []
---

## Description

Create GitHub Issues for all existing backlog tasks and transition them to the appropriate state. Establish a convention where every commit is prefixed with the corresponding GitHub issue ID (e.g., `#17 feat: ...`). Update contribution guidelines accordingly.

## Plan

1. Create GitHub Issues for each existing backlog task (task-01 through task-24), mapping status to GitHub issue state (open/closed)
2. Add appropriate labels to each issue (matching backlog labels)
3. Close issues for tasks already completed (Done status)
4. Document commit prefix convention: `#<issue-id> <type>: <message>`
5. Update CONTRIBUTING.md or README with commit message convention
6. Update `.githooks/pre-commit` or add a commit-msg hook to validate issue ID prefix

## Acceptance Criteria

- [ ] GitHub Issues created for all backlog tasks
- [ ] Issue states match backlog task statuses
- [ ] Labels applied consistently
- [ ] Commit prefix convention documented in README/CONTRIBUTING
- [ ] commit-msg hook validates `#<id>` prefix format (optional but preferred)
- [ ] Team aligned on the workflow
