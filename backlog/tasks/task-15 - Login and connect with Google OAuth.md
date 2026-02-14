---
id: 15
title: Login and connect with Google OAuth
status: To Do
priority: critical
labels:
  - auth
  - backend
  - frontend
created: 2026-02-13 00:00
type: feature
dependencies:
  - task-1
  - task-14
---

## Description

Implement the full end-to-end Google OAuth2 login flow. Users click "Sign in with Google" on the landing page, authenticate via Google, receive a JWT token, and are redirected to the dashboard. First-time users are auto-registered with the USER role.

## Plan

1. Configure Spring Security OAuth2 client with Google provider
2. Implement OAuth2 success handler that creates/updates user and issues JWT
3. Create login API endpoint that initiates OAuth2 flow
4. Build frontend LoginView with Google sign-in button
5. Implement token storage in localStorage and Axios interceptor for JWT
6. Add auto-registration for first-time Google users (default role: USER)
7. Handle token refresh and expiry
8. Write tests for OAuth flow, JWT generation, and frontend auth store

## Acceptance Criteria

- [ ] "Sign in with Google" button triggers OAuth2 flow
- [ ] Successful Google auth redirects to dashboard with JWT
- [ ] First-time users auto-registered with USER role
- [ ] JWT stored securely and sent with all API requests
- [ ] Token refresh works before expiry
- [ ] Invalid/expired tokens redirect to login
- [ ] Logout clears token and redirects to landing page
- [ ] All tests pass
