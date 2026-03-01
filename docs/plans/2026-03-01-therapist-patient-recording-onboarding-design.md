# Design: Therapist–Patient Workflow, Real-Time Recording & AI Onboarding

**Issue:** #13
**Date:** 2026-03-01
**Status:** Approved

---

## Overview

Three interconnected feature areas:

1. **Therapist–patient assignment** via bidirectional invite links
2. **Real-time interview recording** with async Whisper transcription
3. **AI-driven onboarding** (survey or chat), goal validation states, and progress dashboard extensions

---

## 1. Schema Changes

### New table: `invite_tokens`

```sql
CREATE TABLE invite_tokens (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    token          VARCHAR(64) NOT NULL UNIQUE,
    initiator_id   BIGINT NOT NULL,
    initiator_role ENUM('PATIENT', 'THERAPIST') NOT NULL,
    used_at        TIMESTAMP NULL,
    expires_at     TIMESTAMP NOT NULL,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_it_initiator FOREIGN KEY (initiator_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### `therapist_patients.status` — add `PENDING`

```sql
ALTER TABLE therapist_patients
  MODIFY status ENUM('PENDING', 'ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'PENDING';
```

### `goals` — add validation columns

```sql
ALTER TABLE goals
  ADD COLUMN validation_status ENUM('PENDING_VALIDATION','VALIDATED','OVERRIDDEN','REJECTED')
      NOT NULL DEFAULT 'PENDING_VALIDATION',
  ADD COLUMN validated_by  BIGINT NULL,
  ADD COLUMN validated_at  TIMESTAMP NULL,
  ADD COLUMN created_by    BIGINT NULL;
```

### `interviews` — add transcription status

```sql
ALTER TABLE interviews
  ADD COLUMN transcription_status
      ENUM('PENDING','IN_PROGRESS','COMPLETED','FAILED') NULL;
```

### `user_profiles` — add onboarding flag

```sql
ALTER TABLE user_profiles
  ADD COLUMN onboarding_completed BOOLEAN NOT NULL DEFAULT FALSE;
```

---

## 2. Therapist–Patient Assignment

### Invite token flow

Either side (patient or therapist) generates a token. The other side accepts via link.

| Step | Endpoint | Auth | Notes |
|------|----------|------|-------|
| Generate link | `POST /api/invites/generate` | Any authenticated user | Returns `{ token, url }` |
| Preview invite | `GET /api/invites/{token}` | None | Returns initiator name + role for accept screen |
| Accept invite | `POST /api/invites/{token}/accept` | Authenticated, opposite role | Creates `therapist_patients` row |
| Approve request | `POST /api/therapist/patients/{patientId}/accept` | THERAPIST | Sets status → `ACTIVE` |
| Reject request | `POST /api/therapist/patients/{patientId}/reject` | THERAPIST | Sets status → `INACTIVE` |

### Behaviour by initiator

- **Therapist generates link → patient accepts:** status goes straight to `ACTIVE` (therapist already consented)
- **Patient generates link → therapist accepts:** status set to `PENDING`; therapist must then explicitly approve via the portal

### Frontend

- New route `/invite/:token` — accept screen showing initiator name and role
- "My Therapist" card on patient dashboard showing assignment status
- "Invite a Patient" button on therapist portal generating a shareable link
- "Invite a Therapist" button on patient dashboard

---

## 3. Real-Time Recording & Whisper Transcription

### In-browser recording

Extend `AudioSection.vue`:

- **Record** → `MediaRecorder` captures `audio/webm`
- Live duration timer while recording
- **Stop** → produces `Blob`, POSTed to existing `POST /api/interviews/{id}/audio` (no new endpoint)
- UI states: `idle → recording → uploading → transcribing → done / failed`

### Async Whisper transcription

After audio is stored, `AudioService.uploadAudio()` sets `transcription_status = IN_PROGRESS` then fires an `@Async` method:

1. Downloads audio bytes from S3/LocalStack
2. POSTs to OpenAI Whisper API (`whisper-1` model)
3. On success: saves `transcriptionText`, sets `transcription_status = COMPLETED`
4. On failure: sets `transcription_status = FAILED`, logs error

### Frontend polling

After upload completes, the UI shows "Transcribing…". It re-fetches the interview after 20s. If still `IN_PROGRESS`, polls every 10s up to 3 more times, then shows "Transcription taking longer than expected — refresh later".

### Configuration

```yaml
mindtrack:
  ai:
    whisper-api-url: https://api.openai.com/v1/audio/transcriptions
    whisper-api-key: ${OPENAI_API_KEY:dummy-key-for-local}
```

---

## 4. Goal Validation

### Validation states

| `validation_status` | Patient sees | When set |
|---------------------|-------------|----------|
| `PENDING_VALIDATION` | ⬜ Grey tick — *Awaiting review* | Goal created by patient or AI/survey |
| `VALIDATED` | ✅ Green tick | Therapist approved as-is |
| `OVERRIDDEN` | ✏️ Blue chip — *Modified by therapist* | Therapist edited goal content |
| `REJECTED` | ❌ Red chip — *Not approved* | Therapist dismissed goal |

### Rules

- All patient-created goals start as `PENDING_VALIDATION`
- Goals created by therapist via the portal start as `VALIDATED`
- Therapist editing any field of a goal → auto-sets `OVERRIDDEN` + records `validated_by`, `validated_at`
- Therapist can validate or reject at any time regardless of current state

### New therapist write endpoints

| Endpoint | Action |
|----------|--------|
| `POST /api/therapist/patients/{patientId}/goals` | Create goal for patient (starts `VALIDATED`) |
| `PUT /api/therapist/patients/{patientId}/goals/{goalId}` | Edit goal (sets `OVERRIDDEN`) |
| `POST /api/therapist/patients/{patientId}/goals/{goalId}/validate` | → `VALIDATED` |
| `POST /api/therapist/patients/{patientId}/goals/{goalId}/reject` | → `REJECTED` |

### DTO changes

`GoalResponse` gains: `validationStatus`, `validatedBy`, `validatedAt`, `createdBy`.

---

## 5. Onboarding Flow

### First-login detection

On app startup, if `userProfile.onboardingCompleted = false`, router redirects to `/onboarding`.

### Onboarding screen

Patient chooses between two paths:

- **📋 Survey** (~3 min): 6–8 fixed questions (mood baseline, anxiety, sleep, life areas, challenges, goal categories). `OnboardingService.generateGoalsFromSurvey()` maps answers to 3–5 proposed goals.
- **💬 Chat with AI** (~5 min): Uses existing `ConversationService` with new `ConversationType.ONBOARDING`. System prompt guides Claude through ≤6 turns, ending with a JSON block of proposed goals. `OnboardingService.generateGoalsFromChat()` parses and persists goals.

Both paths:
- Create goals with `validation_status = PENDING_VALIDATION`
- Call `ProfileService` to set `onboarding_completed = true`
- Redirect to dashboard

### New backend module: `onboarding`

- `OnboardingController` — `POST /api/onboarding/survey`, `POST /api/onboarding/chat/start`, `POST /api/onboarding/chat/message`, `POST /api/onboarding/complete`
- `OnboardingService` — survey-to-goals mapping, AI chat goal extraction
- DTOs: `SurveyRequest`, `OnboardingGoalResponse`

No new DB tables — reuses `goals` and `conversations`.

---

## 6. Progress Dashboard Extensions

### Backend

`AnalyticsService.getDashboardSummary()` gains:
- `validatedGoals` — count of `VALIDATED` goals
- `pendingValidationGoals` — count of `PENDING_VALIDATION` goals

`getGoalProgress()` includes new validation statuses in breakdown.

No new endpoints — extends existing `/api/analytics/dashboard` response.

### Frontend

New summary cards on `DashboardView.vue`:

```
┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐
│ ✅ Validated │  │ ⬜ Pending   │  │ 🎯 Goals In Progress │
│     goals    │  │  validation  │  │                      │
└──────────────┘  └──────────────┘  └──────────────────────┘
```

- Goals list shows validation chip inline
- "Assign a therapist" banner shown when onboarding complete but no active therapist

---

## Implementation Order

1. Schema migrations (Flyway V5–V9)
2. Goal validation fields + therapist write endpoints
3. Invite token system (backend + `/invite/:token` frontend route)
4. Onboarding module (survey path first, then AI chat)
5. Real-time recording + Whisper transcription
6. Dashboard extensions
