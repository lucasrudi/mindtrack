---
id: 10
title: WhatsApp Business API integration
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

Implement WhatsApp Business API integration for spontaneous AI check-ins and user-initiated conversations. Similar to Telegram integration but via WhatsApp Business Cloud API.

## Plan

1. Implement WhatsApp Business API client service
2. Create webhook handler for incoming WhatsApp messages
3. Route WhatsApp messages through the AI chat pipeline
4. Implement scheduled check-in via EventBridge trigger
5. Link WhatsApp number to user profile
6. Write tests with mocked WhatsApp API

## Acceptance Criteria

- [ ] Bot responds to user messages via Claude API
- [ ] Scheduled check-in messages sent via WhatsApp
- [ ] Conversations via WhatsApp stored with WHATSAPP channel
- [ ] User can link/unlink WhatsApp number in profile
- [ ] Webhook verification works correctly
- [ ] All operations tested
