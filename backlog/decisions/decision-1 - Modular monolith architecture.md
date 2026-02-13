---
id: 1
title: Modular monolith architecture
status: accepted
created: 2026-02-13 00:00
---

## Context

MindTrack needs an architecture that balances development simplicity with scalability. The application has 10 domain modules (auth, admin, interview, activity, journal, goals, ai, messaging, analytics, common) with a single developer.

## Options Considered

1. **Modular Monolith** — Single Spring Boot application with clean package-level module separation
2. **Microservices** — Separate services per domain with API gateway
3. **Backend-for-Frontend (BFF)** — Thin API layer + separate backend services

## Decision

Chose **Modular Monolith** (Option 1).

## Rationale

- Single developer; microservices overhead unjustified
- Clean module separation provides future extraction path
- Single deployment unit (AWS Lambda) simplifies infrastructure
- SnapStart on Lambda provides fast cold starts for Java
- Inter-module communication via direct method calls (no network overhead)
- Shared database simplifies transactions and data consistency
- Can evolve to microservices later by extracting modules

## Consequences

- All modules share a single database schema
- Module boundaries enforced by convention (package structure), not runtime isolation
- Scaling is uniform across all modules (Lambda scales the whole app)
- Need discipline to maintain clean module interfaces
