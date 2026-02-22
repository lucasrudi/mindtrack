---
id: 3
title: Interview logging with structured notes
status: Done
priority: critical
labels:
  - backend
  - frontend
created: 2026-02-13 00:00
type: feature
dependencies:
  - task-1
---

## Description

Implement the interview logging module for recording psychiatrist sessions. Each interview captures structured data including date, mood before/after, topics discussed, medication changes, recommendations, and free-form notes.

## Plan

1. Implement `Interview` JPA entity in interview module
2. Create `InterviewRepository` with custom queries for date ranges and mood trends
3. Implement `InterviewService` with CRUD operations
4. Create `InterviewController` REST endpoints
5. Build frontend InterviewsView with list, create, and detail views
6. Create Pinia store for interview state management
7. Write tests for all layers

## Acceptance Criteria

- [ ] User can create a new interview with all structured fields
- [ ] User can view list of past interviews sorted by date
- [ ] User can view and edit interview details
- [ ] Mood before/after captured as integer scale (1-10)
- [ ] Topics stored as JSON array
- [ ] All CRUD operations tested
