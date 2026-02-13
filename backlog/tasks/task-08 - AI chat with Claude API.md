---
id: 8
title: AI chat with Claude API
status: To Do
priority: critical
labels:
  - ai
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

Implement AI-powered coaching chat using Claude API. The AI has context about the user's recent interviews, activities, journal entries, goals, and mood trends to provide personalized suggestions and support.

## Plan

1. Implement `Conversation` and `Message` JPA entities
2. Create Claude API client service with streaming support
3. Implement context builder that aggregates user's recent data for AI context
4. Create `ChatController` with SSE or WebSocket for streaming responses
5. Build frontend ChatView with message thread and streaming display
6. Create Pinia store for conversation state
7. Write tests with mocked Claude API

## Acceptance Criteria

- [ ] User can start new conversations
- [ ] AI responses stream in real-time
- [ ] AI has context of user's recent data (interviews, activities, mood)
- [ ] Conversation history persisted
- [ ] Multiple conversation channels supported (WEB)
- [ ] All operations tested with mocked API
