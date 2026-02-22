---
id: 13
title: User profile configuration
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

Implement user profile configuration including display name, avatar, timezone, notification preferences, and linked messaging accounts (Telegram, WhatsApp).

## Plan

1. Implement `UserProfile` JPA entity
2. Create `UserProfileRepository`
3. Implement `ProfileService` for CRUD operations
4. Create `ProfileController` REST endpoints
5. Build frontend ProfileView with settings form
6. Create Pinia store for profile state
7. Write tests for all layers

## Acceptance Criteria

- [ ] User can set display name and avatar
- [ ] User can configure timezone
- [ ] User can set notification preferences (JSON)
- [ ] User can link/unlink Telegram chat ID
- [ ] User can link/unlink WhatsApp number
- [ ] Profile changes persisted correctly
- [ ] All operations tested
