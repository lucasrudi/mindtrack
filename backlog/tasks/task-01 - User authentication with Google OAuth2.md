---
id: 1
title: User authentication with Google OAuth2
status: Done
priority: critical
labels:
  - auth
  - backend
created: 2026-02-13 00:00
type: feature
---

## Description

Implement user authentication using Google OAuth2 with JWT session management. Users log in via Google, receive a JWT token, and use it for all subsequent API requests. Includes role-based access control (ADMIN, USER, THERAPIST).

## Plan

1. Implement `User`, `Role`, `Permission` JPA entities in auth module
2. Create `UserRepository`, `RoleRepository`, `PermissionRepository`
3. Implement `JwtTokenService` for token generation and validation
4. Configure Spring Security with OAuth2 client (Google provider)
5. Create `AuthController` with login, token refresh, and logout endpoints
6. Add `SecurityConfig` with endpoint-level authorization
7. Write unit and integration tests for all components

## Acceptance Criteria

- [ ] Google OAuth2 login flow works end-to-end
- [ ] JWT tokens are issued on successful login
- [ ] Token refresh endpoint works correctly
- [ ] Role-based access control enforced at endpoint level
- [ ] Invalid/expired tokens return 401
- [ ] All tests pass with >80% coverage

## Notes

- Use `io.jsonwebtoken:jjwt` library for JWT handling
- Store refresh tokens securely
- OAuth2 client credentials in Secrets Manager (prod) or application-local.yml (dev)
