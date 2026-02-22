---
id: 18
title: Integration and functional tests for the backend
status: To Do
priority: high
labels:
  - backend
  - testing
created: 2026-02-22 00:00
type: feature
dependencies: []
---

## Description

Create a comprehensive suite of integration and functional tests for all backend modules (auth, admin, interview, activity, journal, goals, ai, messaging, analytics, common). Tests should cover REST endpoints, service logic, database interactions, and security rules.

## Plan

1. Set up Spring Boot Test infrastructure with H2 in-memory DB and MockMvc
2. Write integration tests for auth module (OAuth2 flow, JWT validation, role-based access)
3. Write integration tests for each feature module (interview, activity, journal, goals, ai, messaging, analytics)
4. Write functional end-to-end API tests covering happy paths and error scenarios
5. Configure test coverage reporting in Maven (JaCoCo)
6. Add integration test phase to CI pipeline

## Acceptance Criteria

- [ ] Integration tests cover all REST endpoints (happy path + error cases)
- [ ] Security rules tested (unauthenticated, unauthorized, authorized)
- [ ] Database interactions verified (CRUD operations per module)
- [ ] Tests run in isolation with no external dependencies
- [ ] Code coverage meets minimum threshold (80%)
- [ ] Tests integrated into `mvn verify` lifecycle
- [ ] CI pipeline runs tests on every push
