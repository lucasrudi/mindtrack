---
id: 9
title: Telegram bot integration
status: Done
priority: medium
labels:
  - messaging
  - backend
created: 2026-02-13 00:00
type: feature
dependencies:
  - task-8
---

## Description

Implement Telegram bot integration for spontaneous AI check-ins and user-initiated conversations. The bot can proactively message users for daily check-ins (triggered by EventBridge schedule) and respond to user messages via Claude API.

## Plan

1. Implement Telegram Bot API client service
2. Create webhook handler for incoming Telegram messages
3. Route Telegram messages through the AI chat pipeline
4. Implement scheduled check-in logic via EventBridge trigger
5. Link Telegram chat ID to user profile
6. Write tests with mocked Telegram API

## Acceptance Criteria

- [ ] Bot responds to user messages via Claude API
- [ ] Scheduled daily check-in messages sent at configured time
- [ ] Conversations via Telegram stored with TELEGRAM channel
- [ ] User can link/unlink Telegram account in profile
- [ ] Bot handles unknown users gracefully
- [ ] All operations tested
