---
id: 17
title: Review and fix all TODOs and FIXMEs
status: To Do
priority: high
labels:
  - backend
  - frontend
  - chore
created: 2026-02-22 00:00
type: chore
dependencies: []
---

## Description

Audit the entire codebase for TODO and FIXME comments and implement or resolve each one. Covers both backend (Java) and frontend (TypeScript/Vue).

## Plan

1. Run grep/search across backend and frontend for TODO and FIXME comments
2. Categorize findings by module/component and severity
3. Implement or resolve each item (feature gaps, placeholder code, deferred logic)
4. Remove resolved comment markers
5. Write or update tests for any implemented logic
6. Document any TODOs intentionally deferred with a tracking issue reference

## Acceptance Criteria

- [ ] All TODO and FIXME comments catalogued
- [ ] Each item resolved, implemented, or tracked via a GitHub issue
- [ ] No unresolved TODO/FIXME left in committed code
- [ ] Tests pass after all changes
- [ ] Code reviewed
