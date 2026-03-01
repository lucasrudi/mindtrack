# Therapist Role Assignment — Design Doc

**Date:** 2026-03-01
**Issue:** TBD (to be created before implementation)

## Overview

Allow users to designate themselves as therapists (or switch back to patient) during sign-up
(onboarding), from their profile, and via the existing admin panel. Role changes are immediate
and self-service — no approval queue needed. A therapist is also a person who may have their
own mental health goals, so all users go through the same wellness survey.

## Context

The `THERAPIST` role already exists in the database (seeded in V1) with the correct permissions.
The admin panel already supports role changes via `PATCH /api/admin/users/{id}/role` (both
backend and frontend are fully implemented). The missing pieces are:

1. Role selection during onboarding
2. Self-service role change from the user profile
3. Skippable wellness survey with resume support

## Backend

### V10 Migration

Add `survey_completed BOOLEAN NOT NULL DEFAULT FALSE` to `user_profiles`. This allows the
dashboard and profile to know whether to show the "complete your baseline" prompt.

### New endpoint: PATCH /api/auth/me/role

- **Controller:** `AuthController`
- **Auth:** any authenticated user
- **Body:** `SelfRoleRequest { role: "USER" | "THERAPIST" }` — validated with `@Pattern`, ADMIN
  is explicitly excluded
- **Behaviour:** updates `users.role_id`, generates a new JWT token with the updated role claim
- **Response:** `{ "token": "<new-jwt>" }` — frontend replaces the stored token immediately

### New endpoint: POST /api/onboarding/skip

- **Controller:** `OnboardingController` (existing)
- **Auth:** any authenticated user
- **Behaviour:** sets `onboardingCompleted = true`, leaves `surveyCompleted = false`
- **Response:** 200 OK

### Update: POST /api/onboarding/survey

Existing endpoint — add `surveyCompleted = true` to the user profile update alongside the
existing `onboardingCompleted = true` and goal generation.

## Frontend

### OnboardingView.vue

**Step 1 — Account type (replaces current "choose" mode):**

```
┌────────────────────────────┐  ┌────────────────────────────┐
│  👤  I'm a Patient         │  │  🩺  I'm a Therapist        │
│  Track my own mental health│  │  I work with patients       │
└────────────────────────────┘  └────────────────────────────┘
```

- "I'm a Patient": no-op (USER is the default role), advance to survey
- "I'm a Therapist": call `PATCH /api/auth/me/role { role: "THERAPIST" }`, store new token via
  `authStore.updateToken(token)`, advance to survey

**Step 2 — Survey (same for everyone):**
- Existing sliders + life area chips
- New "Skip for now" button: calls `POST /api/onboarding/skip`, redirects to dashboard
- Submit: calls `POST /api/onboarding/survey`, marks `surveyCompleted = true` in profile store,
  redirects to dashboard

### ProfileView.vue

**New section: Account Type**
- Displays current role as a readable label ("Patient" / "Therapist")
- Toggle button: "Switch to Therapist" / "Switch to Patient"
- On click: calls `PATCH /api/auth/me/role`, receives new token, calls `authStore.updateToken(token)`
- Role label and button update immediately after token refresh

**New section: Wellness Baseline**
- If `surveyCompleted = false`: shows "Not yet completed" badge + "Complete Survey" button
- If `surveyCompleted = true`: shows "Completed" badge + "Redo Survey" button
- Button opens the survey form inline (same fields as onboarding: mood, anxiety, sleep, life areas)
- Submit calls `POST /api/onboarding/survey`

### DashboardView.vue

- If `profileStore.profile?.surveyCompleted === false`: show a dismissible prompt card
  "Complete your wellness baseline →" that navigates to `/profile#wellness-baseline`
- Card is not shown once survey is completed

### Auth store

New `updateToken(token: string)` method:
- Stores the new token (localStorage + store state)
- Re-parses the JWT claims (role, email, userId)
- Updates `authStore.user.role` so the UI (nav, guards) reflects the change without re-login

### Profile store

- Add `surveyCompleted: boolean` to `UserProfile` interface
- `fetchProfile()` already calls `GET /api/profile/me` — backend response must include
  `surveyCompleted` from `user_profiles`

## Data Flow: Role Change

```
User clicks "Switch to Therapist"
  → PATCH /api/auth/me/role { role: "THERAPIST" }
  → Backend: update DB, generate new JWT
  → Frontend: authStore.updateToken(newToken)
  → JWT re-parsed: authStore.user.role = "THERAPIST"
  → UI updates immediately (nav items, route guards)
```

## Security Constraints

- `PATCH /api/auth/me/role` only accepts `USER` and `THERAPIST` — any other value returns 400
- A user cannot self-assign `ADMIN` under any circumstance
- Admins retain the ability to set any role (including demoting a therapist) via the existing
  admin panel

## What Is Not Changing

- Admin panel role management — already fully implemented, no changes needed
- Therapist-patient relationship mechanics — handled by existing therapist workflow (issue #13)
- JWT structure — same format, just updated role claim value
