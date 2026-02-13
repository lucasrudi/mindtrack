---
id: 2
title: Admin panel with RBAC
status: To Do
priority: high
labels:
  - admin
  - backend
  - frontend
created: 2026-02-13 00:00
type: feature
dependencies:
  - task-1
---

## Description

Build an admin panel for user management and role-based access control configuration. Only users with ADMIN role can access this panel. Supports listing users, changing roles, enabling/disabling accounts, and managing permissions per role.

## Plan

1. Implement admin service for user CRUD operations
2. Implement RBAC configuration service for role-permission management
3. Create admin REST controller with proper authorization
4. Build frontend AdminView with user table, role editor, and permission matrix
5. Write tests for service, controller, and frontend components

## Acceptance Criteria

- [ ] Admin can list all users with pagination
- [ ] Admin can change a user's role
- [ ] Admin can enable/disable user accounts
- [ ] Admin can view and edit role-permission mappings
- [ ] Non-admin users get 403 when accessing admin endpoints
- [ ] Frontend shows admin panel only for ADMIN role
