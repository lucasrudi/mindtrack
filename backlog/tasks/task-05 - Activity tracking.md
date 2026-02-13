---
id: 5
title: Activity tracking
status: To Do
priority: high
labels:
  - backend
  - frontend
created: 2026-02-13 00:00
type: feature
dependencies:
  - task-1
---

## Description

Implement activity tracking for homework assigned by therapists, daily habits, and custom user-defined activities. Each activity can be logged daily with completion status, notes, and mood rating.

## Plan

1. Implement `Activity` and `ActivityLog` JPA entities
2. Create repositories with queries for active activities and daily logs
3. Implement `ActivityService` for CRUD and daily logging
4. Create `ActivityController` REST endpoints
5. Build frontend ActivitiesView with activity list, daily checklist, and log history
6. Create Pinia store for activity state
7. Write tests for all layers

## Acceptance Criteria

- [ ] User can create activities of type HOMEWORK, HABIT, or CUSTOM
- [ ] Activities can be linked to a specific interview
- [ ] User can log daily activity completion with notes and mood
- [ ] Activity list shows active/inactive filter
- [ ] Daily checklist view for quick logging
- [ ] All operations tested
