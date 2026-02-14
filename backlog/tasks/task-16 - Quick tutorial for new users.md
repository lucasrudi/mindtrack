---
id: 16
title: Quick tutorial for new users
status: To Do
priority: medium
labels:
  - frontend
created: 2026-02-13 00:00
type: feature
dependencies:
  - task-15
---

## Description

Implement an interactive onboarding tutorial that guides first-time users through MindTrack's key features. Shown automatically after first login, with an option to skip and replay from profile settings.

## Plan

1. Design tutorial flow: welcome → log an interview → track an activity → write journal → set a goal → chat with AI
2. Implement step-by-step overlay/tooltip component (tour guide pattern)
3. Track tutorial completion status in user profile
4. Add "Replay Tutorial" option in profile settings
5. Write unit tests for tutorial component and state management

## Acceptance Criteria

- [ ] Tutorial shown automatically on first login
- [ ] Step-by-step walkthrough of key features (5-7 steps)
- [ ] Each step highlights the relevant UI area with tooltip/overlay
- [ ] User can skip tutorial at any point
- [ ] Tutorial completion saved to user profile (not shown again)
- [ ] "Replay Tutorial" available in profile settings
- [ ] Tutorial adapts to screen size (mobile-friendly)
- [ ] All components tested
