---
id: 6
title: Journal entries
status: To Do
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

Implement free-form journal writing with mood tracking and tagging. Users can write daily entries, tag them with topics, rate their mood, and optionally share entries with their therapist.

## Plan

1. Implement `JournalEntry` JPA entity
2. Create `JournalEntryRepository` with search by date, tags, and mood
3. Implement `JournalService` with CRUD and sharing functionality
4. Create `JournalController` REST endpoints
5. Build frontend JournalView with editor, list, and search
6. Create Pinia store for journal state
7. Write tests for all layers

## Acceptance Criteria

- [ ] User can create, edit, and delete journal entries
- [ ] Entries support rich text content
- [ ] Mood rating (1-10) captured per entry
- [ ] Tags stored as JSON array with search support
- [ ] Entries can be shared/unshared with therapist
- [ ] All operations tested
