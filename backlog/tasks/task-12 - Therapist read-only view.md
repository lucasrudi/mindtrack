---
id: 12
title: Therapist read-only view
status: To Do
priority: medium
labels:
  - backend
  - frontend
created: 2026-02-13 00:00
type: feature
dependencies:
  - task-1
  - task-3
  - task-5
---

## Description

Implement a read-only view for therapists to monitor their patient's progress. Therapists can view interviews, activities, goals, and shared journal entries but cannot modify any data.

## Plan

1. Implement therapist-patient relationship model
2. Add authorization rules for THERAPIST role (read-only on patient data)
3. Create therapist-specific endpoints with filtered views
4. Build frontend TherapistView with patient overview dashboard
5. Enforce read-only at service and controller layers
6. Write tests for authorization and data filtering

## Acceptance Criteria

- [ ] Therapist can view patient's interviews (read-only)
- [ ] Therapist can view patient's activities and logs (read-only)
- [ ] Therapist can view patient's goals (read-only)
- [ ] Therapist can only see journal entries marked as shared
- [ ] Therapist cannot access conversations
- [ ] Write/delete operations blocked for THERAPIST role
