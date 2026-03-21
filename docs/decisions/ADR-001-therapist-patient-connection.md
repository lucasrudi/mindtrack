# ADR-001: Therapist-Patient Connection Approach

## Status
Accepted

## Context

Issue #321 asks for a flow where therapists can send a connection request to a user, the user can
accept or reject it, and both parties can see the request state in the product. Two implementation
approaches were evaluated as part of spike #344.

**Option A — Link-based (extend existing system):** The therapist generates an invite link
(or enters the patient's email to produce a targeted token) and sends it out-of-band. The patient
clicks the link, is shown a preview of who sent it, and accepts or rejects it in the existing
`InviteView.vue` page.

**Option B — Direct in-app request:** The therapist searches for a patient by email or name inside
the app and submits a connection request. The patient receives a notification in-app (and/or by
email) and accepts or rejects from a notification inbox. This approach requires the notification
delivery infrastructure described in #340, which does not yet exist.

## Decision

**Option A (link-based)** is chosen.

The backend for Option A is substantially complete: the `POST /api/invites/request` endpoint
already accepts a patient email, prevents duplicates, creates a `PENDING` `TherapistPatient` row,
mints a targeted `InviteToken`, and returns the invite URL. `InviteView.vue` already renders a
"Therapist request received" message and provides accept/reject buttons. The therapist dashboard
(`TherapistView.vue`) already has the email-input form and calls `POST /api/invites/request`.

Option B would require building the entire notification delivery stack (#340 — `notifications`
table, `NotificationService`, `EmailService`, SES wiring, navbar bell, notification inbox) before
any part of #321 could ship. That dependency has not been scheduled. Blocking #321 on that work
is not warranted when Option A satisfies all four acceptance criteria with only minor frontend and
backend additions.

## Existing Infrastructure

The following is already in the codebase and covers the core of Option A:

**Backend (`backend/src/main/java/com/mindtrack/therapist/`)**

- `InviteToken` entity — `token`, `initiatorId`, `initiatorRole` (`THERAPIST` | `PATIENT`),
  `recipientId` (nullable, populated by `requestPatient`), `expiresAt` (7-day window),
  `usedAt`, `createdAt`.
- `InviteTokenRepository` — includes `findByInitiatorIdAndRecipientIdAndUsedAtIsNullAndExpiresAtAfter`
  used to enforce the one-pending-request-per-pair constraint.
- `InviteService.requestPatient` — looks up patient by email, checks for an existing non-INACTIVE
  relationship and a live duplicate token, creates/resets the `TherapistPatient` row to `PENDING`,
  mints the token, and returns the invite URL.
- `InviteService.acceptInvite` / `rejectInvite` — mark the token as used and set the relationship
  to `ACTIVE` or `INACTIVE` respectively; enforce that only the intended recipient can respond when
  `recipientId` is set.
- `InviteService.previewInvite` — returns initiator name, role, and current relationship status
  for display before the patient responds.
- `InviteController` — `POST /api/invites/generate`, `POST /api/invites/request` (THERAPIST only),
  `GET /api/invites/{token}`, `POST /api/invites/{token}/accept`, `POST /api/invites/{token}/reject`.
- `TherapistPatient` entity — `therapistId`, `patientId`, `status` (`PENDING` | `ACTIVE` | `INACTIVE`),
  `calendarColor`, `createdAt`.
- `TherapistController.listPendingPatients` — `GET /api/therapist/patients/pending` returns
  `TherapistPatientStatus.PENDING` rows for the authenticated therapist.

**Frontend (`frontend/src/`)**

- `InviteView.vue` at `/invite/:token` — fetches preview on mount, shows initiator name/role and
  a `PENDING` status pill, and calls accept/reject; redirects unauthenticated users to login with a
  return URL.
- `TherapistView.vue` — "Request a patient" panel with email input, calls `store.requestPatient`,
  displays the returned invite URL, and shows a "Pending requests" table driven by
  `store.pendingPatients`.
- `therapist.ts` Pinia store — `fetchPendingPatients()`, `requestPatient(email)`.

## Gaps / Work Required

The following pieces are missing for #321 to be fully implemented:

1. **Patient-side status visibility (backend):** No endpoint exists that lets the authenticated
   patient list therapist connection requests directed at them. A new endpoint — e.g.
   `GET /api/patient/requests` — is needed, returning `TherapistPatient` rows where
   `patientId = currentUser.id` and `status = PENDING`. This requires adding
   `findByPatientIdAndStatus` to `TherapistPatientRepository`.

2. **Patient-side status visibility (frontend):** No patient-facing UI shows pending therapist
   requests. A "Pending therapist requests" section needs to be added to the patient dashboard (or
   a dedicated page), listing requests with accept/reject actions. Currently, acceptance is only
   possible by clicking the invite link — if a patient discards the link email they have no way to
   find the request again.

3. **Link delivery UX (therapist side):** After `POST /api/invites/request`, the backend returns
   the invite URL but only displays it as a raw hyperlink in `TherapistView.vue`. A copy-to-clipboard
   button would improve usability, though this is a polish item rather than a functional gap.

4. **No patient-side endpoint currently allows revocation/cancellation** of a PENDING request by
   the therapist. Whether this is needed for #321 should be confirmed against the issue.

5. **Token expiry and status divergence:** `InviteToken` expires after 7 days; after expiry the
   `TherapistPatient` row remains `PENDING` indefinitely. A scheduled job (or on-read check) should
   reset stale PENDING relationships to INACTIVE when the corresponding token has expired and the
   patient never responded. The existing `cleanupExpiredTokens` job deletes the token rows but does
   not update the `TherapistPatient` status.

## Consequences

- #321 can be implemented without any dependency on #340 (notification infrastructure).
- The out-of-band link delivery is a deliberate trade-off: therapists copy the link and share it
  via email, SMS, or messaging. This is lower friction than it sounds — therapists already have
  communication channels with prospective patients before the first session.
- When #340 ships, it can be layered on top: `InviteService.requestPatient` already has the
  patient's ID and email, making it straightforward to trigger a notification dispatch at that
  point. The link remains valid regardless.
- The approach avoids building a user-search endpoint (no partial name/email search exists today),
  which would raise privacy concerns and require additional design work.

## Related Issues
- #321 (implements this decision)
- #340 (not required; can enhance delivery UX after this ships)
- #344 (this spike)
