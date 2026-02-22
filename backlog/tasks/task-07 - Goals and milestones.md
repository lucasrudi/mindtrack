---
id: 7
title: Goals and milestones
status: Done
priority: medium
labels:
  - backend
  - frontend
created: 2026-02-13 00:00
type: feature
dependencies:
  - task-1
---

## Description

Implement goal setting and milestone tracking. Users can create goals with categories, target dates, and break them into milestones. Goals have statuses (ACTIVE, COMPLETED, ABANDONED) and milestones track completion dates.

## Plan

1. Implement `Goal` and `Milestone` JPA entities
2. Create repositories with queries for active goals and upcoming milestones
3. Implement `GoalService` for CRUD and milestone management
4. Create `GoalController` REST endpoints
5. Build frontend GoalsView with goal list, milestone timeline, and progress bars
6. Create Pinia store for goals state
7. Write tests for all layers

## Acceptance Criteria

- [ ] User can create goals with title, description, category, and target date
- [ ] Goals can be broken into milestones
- [ ] Milestones can be marked as completed
- [ ] Goal status transitions work correctly
- [ ] Progress bars reflect milestone completion percentage
- [ ] All operations tested
