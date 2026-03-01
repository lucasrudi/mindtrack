# Therapist–Patient, Recording & Onboarding Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Implement bidirectional therapist–patient invite links, goal validation workflow, in-browser audio recording with async Whisper transcription, AI-driven onboarding, and dashboard extensions.

**Architecture:** Flyway migrations first, then backend feature by feature (goal validation → invites → onboarding → recording), then frontend. Each backend task ends with a passing test suite commit. Frontend tasks add routes and update components.

**Tech Stack:** Java 21, Spring Boot 3.5.3, Maven, Spring Data JPA, Flyway, Vue 3 Composition API, TypeScript, Pinia, JUnit 5 + Mockito, MockMvc.

**Design doc:** `docs/plans/2026-03-01-therapist-patient-recording-onboarding-design.md`

**How to run tests:**
- Backend: `cd backend && mvn test` (unit only, fast) or `mvn verify` (full)
- Frontend: `cd frontend && npm run test:unit`

---

## Task 1: Schema migrations V5–V9

**Files:**
- Create: `backend/src/main/resources/db/migration/V5__add_goal_validation_fields.sql`
- Create: `backend/src/main/resources/db/migration/V6__add_therapist_patient_pending_status.sql`
- Create: `backend/src/main/resources/db/migration/V7__add_interview_transcription_status.sql`
- Create: `backend/src/main/resources/db/migration/V8__add_onboarding_completed.sql`
- Create: `backend/src/main/resources/db/migration/V9__add_invite_tokens_table.sql`

**Step 1: Write V5 — goal validation fields**

```sql
-- V5__add_goal_validation_fields.sql
ALTER TABLE goals
  ADD COLUMN validation_status ENUM('PENDING_VALIDATION','VALIDATED','OVERRIDDEN','REJECTED')
      NOT NULL DEFAULT 'PENDING_VALIDATION',
  ADD COLUMN validated_by  BIGINT NULL,
  ADD COLUMN validated_at  TIMESTAMP NULL,
  ADD COLUMN created_by    BIGINT NULL;
```

**Step 2: Write V6 — add PENDING to therapist_patients.status**

```sql
-- V6__add_therapist_patient_pending_status.sql
ALTER TABLE therapist_patients
  MODIFY status ENUM('PENDING','ACTIVE','INACTIVE') NOT NULL DEFAULT 'PENDING';
```

**Step 3: Write V7 — interview transcription_status**

```sql
-- V7__add_interview_transcription_status.sql
ALTER TABLE interviews
  ADD COLUMN transcription_status
      ENUM('PENDING','IN_PROGRESS','COMPLETED','FAILED') NULL;
```

**Step 4: Write V8 — onboarding_completed flag**

```sql
-- V8__add_onboarding_completed.sql
ALTER TABLE user_profiles
  ADD COLUMN onboarding_completed BOOLEAN NOT NULL DEFAULT FALSE;
```

**Step 5: Write V9 — invite_tokens table**

```sql
-- V9__add_invite_tokens_table.sql
CREATE TABLE invite_tokens (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    token          VARCHAR(64) NOT NULL UNIQUE,
    initiator_id   BIGINT NOT NULL,
    initiator_role ENUM('PATIENT','THERAPIST') NOT NULL,
    used_at        TIMESTAMP NULL,
    expires_at     TIMESTAMP NOT NULL,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_it_initiator FOREIGN KEY (initiator_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**Step 6: Verify migrations run against Docker MySQL**

```bash
cd docker && docker compose up mysql -d
# wait for healthy, then:
cd backend && mvn flyway:migrate -Pflyway-docker
# Expected: Successfully applied 5 migrations (V5..V9)
```

> Note: The local H2 profile has `flyway.enabled=false` and `ddl-auto=create-drop`, so H2 picks up entity fields automatically — no migration needed for local dev.

**Step 7: Commit**

```bash
git add backend/src/main/resources/db/migration/
git commit -m "feat(schema): add V5-V9 Flyway migrations for goal validation, invite tokens, transcription status, and onboarding flag"
```

---

## Task 2: GoalValidationStatus enum + Goal entity fields

**Files:**
- Create: `backend/src/main/java/com/mindtrack/goals/model/GoalValidationStatus.java`
- Modify: `backend/src/main/java/com/mindtrack/goals/model/Goal.java`

**Step 1: Write the failing test** (add to `GoalMapperTest` once GoalResponse is updated — skip for now; test entity fields directly)

In `GoalServiceTest.java`, add:

```java
@Test
void shouldCreateGoalWithPendingValidationStatus() {
    GoalRequest request = createGoalRequest();
    when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> {
        Goal saved = inv.getArgument(0);
        saved.setId(1L);
        return saved;
    });

    GoalResponse result = goalService.create(1L, request);

    assertEquals(GoalValidationStatus.PENDING_VALIDATION, result.getValidationStatus());
}
```

**Step 2: Run to confirm it fails**

```bash
cd backend && mvn test -pl . -Dtest=GoalServiceTest#shouldCreateGoalWithPendingValidationStatus
# Expected: compilation error — GoalValidationStatus doesn't exist yet
```

**Step 3: Create `GoalValidationStatus.java`**

```java
package com.mindtrack.goals.model;

public enum GoalValidationStatus {
    PENDING_VALIDATION,
    VALIDATED,
    OVERRIDDEN,
    REJECTED
}
```

**Step 4: Add fields to `Goal.java`** — insert after the `status` field:

```java
@Enumerated(EnumType.STRING)
@Column(name = "validation_status", nullable = false)
private GoalValidationStatus validationStatus = GoalValidationStatus.PENDING_VALIDATION;

@Column(name = "validated_by")
private Long validatedBy;

@Column(name = "validated_at")
private LocalDateTime validatedAt;

@Column(name = "created_by")
private Long createdBy;
```

Add getters and setters for all four fields (follow the same pattern as the existing fields — plain `getXxx`/`setXxx` methods, no annotations needed).

**Step 5: Run test — confirm it still fails** (GoalResponse doesn't expose `validationStatus` yet)

```bash
cd backend && mvn test -pl . -Dtest=GoalServiceTest#shouldCreateGoalWithPendingValidationStatus
```

**Step 6: Commit entity only**

```bash
git add backend/src/main/java/com/mindtrack/goals/model/
git commit -m "feat(goals): add GoalValidationStatus enum and validation fields to Goal entity"
```

---

## Task 3: Update GoalResponse DTO and GoalMapper

**Files:**
- Modify: `backend/src/main/java/com/mindtrack/goals/dto/GoalResponse.java`
- Modify: `backend/src/main/java/com/mindtrack/goals/service/GoalMapper.java`
- Modify: `backend/src/main/java/com/mindtrack/goals/service/GoalService.java`
- Test: `backend/src/test/java/com/mindtrack/goals/service/GoalServiceTest.java`
- Test: `backend/src/test/java/com/mindtrack/goals/service/GoalMapperTest.java`

**Step 1: Add fields to `GoalResponse.java`** — after the existing `updatedAt` field:

```java
private GoalValidationStatus validationStatus;
private Long validatedBy;
private LocalDateTime validatedAt;
private Long createdBy;
```

Add getter/setter for each (same pattern as existing fields). Also add the import:
```java
import com.mindtrack.goals.model.GoalValidationStatus;
```

**Step 2: Update `GoalMapper.toGoalResponse()`** — add these lines after `response.setUpdatedAt(goal.getUpdatedAt())`:

```java
response.setValidationStatus(goal.getValidationStatus());
response.setValidatedBy(goal.getValidatedBy());
response.setValidatedAt(goal.getValidatedAt());
response.setCreatedBy(goal.getCreatedBy());
```

**Step 3: Update `GoalService.create()`** — after `goal.setUserId(userId)` add:

```java
goal.setValidationStatus(GoalValidationStatus.PENDING_VALIDATION);
goal.setCreatedBy(userId);
```

Also add the import at the top of the file:
```java
import com.mindtrack.goals.model.GoalValidationStatus;
```

**Step 4: Run the test that was written in Task 2**

```bash
cd backend && mvn test -pl . -Dtest=GoalServiceTest#shouldCreateGoalWithPendingValidationStatus
# Expected: PASS
```

**Step 5: Add a GoalMapper test** — in `GoalMapperTest.java`, add:

```java
@Test
void shouldMapValidationFieldsToResponse() {
    Goal goal = createGoal();
    goal.setValidationStatus(GoalValidationStatus.VALIDATED);
    goal.setValidatedBy(99L);
    goal.setValidatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
    goal.setCreatedBy(1L);

    GoalResponse response = mapper.toGoalResponse(goal);

    assertEquals(GoalValidationStatus.VALIDATED, response.getValidationStatus());
    assertEquals(99L, response.getValidatedBy());
    assertNotNull(response.getValidatedAt());
    assertEquals(1L, response.getCreatedBy());
}
```

**Step 6: Run mapper test**

```bash
cd backend && mvn test -pl . -Dtest=GoalMapperTest
# Expected: PASS
```

**Step 7: Run full goal module tests**

```bash
cd backend && mvn test -pl . -Dtest="GoalServiceTest,GoalMapperTest,GoalControllerTest"
# Expected: all PASS
```

**Step 8: Commit**

```bash
git add backend/src/main/java/com/mindtrack/goals/
git add backend/src/test/java/com/mindtrack/goals/
git commit -m "feat(goals): expose validation fields in GoalResponse and set PENDING_VALIDATION on create"
```

---

## Task 4: Therapist goal write endpoints

**Files:**
- Modify: `backend/src/main/java/com/mindtrack/therapist/service/TherapistService.java`
- Modify: `backend/src/main/java/com/mindtrack/therapist/controller/TherapistController.java`
- Modify: `backend/src/main/java/com/mindtrack/therapist/model/TherapistPatientStatus.java`
- Test: `backend/src/test/java/com/mindtrack/therapist/service/TherapistServiceTest.java`
- Test: `backend/src/test/java/com/mindtrack/therapist/controller/TherapistControllerTest.java`

**Step 1: Add PENDING to TherapistPatientStatus enum**

Open `TherapistPatientStatus.java` and change to:

```java
package com.mindtrack.therapist.model;

public enum TherapistPatientStatus {
    PENDING,
    ACTIVE,
    INACTIVE
}
```

Also update `TherapistPatient.java` — change the default value:
```java
private TherapistPatientStatus status = TherapistPatientStatus.ACTIVE;
```
(Leave as ACTIVE — the entity default is still ACTIVE; PENDING will be set by the invite system.)

**Step 2: Write failing service tests** — add to `TherapistServiceTest.java`:

```java
@Test
void shouldCreateGoalForPatientAsValidated() {
    when(therapistPatientRepository.existsByTherapistIdAndPatientIdAndStatus(
            1L, 2L, TherapistPatientStatus.ACTIVE)).thenReturn(true);
    when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> {
        Goal g = inv.getArgument(0);
        g.setId(10L);
        return g;
    });

    GoalRequest request = new GoalRequest();
    request.setTitle("Therapist goal");

    GoalResponse result = therapistService.createGoalForPatient(1L, 2L, request);

    assertEquals(10L, result.getId());
    assertEquals(GoalValidationStatus.VALIDATED, result.getValidationStatus());
    assertEquals(1L, result.getCreatedBy()); // therapist is creator
}

@Test
void shouldValidateGoal() {
    Goal goal = createGoal(10L, 2L);
    goal.setValidationStatus(GoalValidationStatus.PENDING_VALIDATION);
    when(therapistPatientRepository.existsByTherapistIdAndPatientIdAndStatus(
            1L, 2L, TherapistPatientStatus.ACTIVE)).thenReturn(true);
    when(goalRepository.findByIdAndUserId(10L, 2L)).thenReturn(Optional.of(goal));
    when(goalRepository.save(any(Goal.class))).thenReturn(goal);

    GoalResponse result = therapistService.validateGoal(1L, 2L, 10L);

    assertEquals(GoalValidationStatus.VALIDATED, result.getValidationStatus());
    assertEquals(1L, result.getValidatedBy());
}

@Test
void shouldRejectGoal() {
    Goal goal = createGoal(10L, 2L);
    when(therapistPatientRepository.existsByTherapistIdAndPatientIdAndStatus(
            1L, 2L, TherapistPatientStatus.ACTIVE)).thenReturn(true);
    when(goalRepository.findByIdAndUserId(10L, 2L)).thenReturn(Optional.of(goal));
    when(goalRepository.save(any(Goal.class))).thenReturn(goal);

    GoalResponse result = therapistService.rejectGoal(1L, 2L, 10L);

    assertEquals(GoalValidationStatus.REJECTED, result.getValidationStatus());
}

@Test
void shouldEditGoalAndSetOverridden() {
    Goal goal = createGoal(10L, 2L);
    goal.setValidationStatus(GoalValidationStatus.PENDING_VALIDATION);
    GoalRequest request = new GoalRequest();
    request.setTitle("Updated by therapist");

    when(therapistPatientRepository.existsByTherapistIdAndPatientIdAndStatus(
            1L, 2L, TherapistPatientStatus.ACTIVE)).thenReturn(true);
    when(goalRepository.findByIdAndUserId(10L, 2L)).thenReturn(Optional.of(goal));
    when(goalRepository.save(any(Goal.class))).thenReturn(goal);

    GoalResponse result = therapistService.editGoalForPatient(1L, 2L, 10L, request);

    assertEquals(GoalValidationStatus.OVERRIDDEN, result.getValidationStatus());
    assertEquals(1L, result.getValidatedBy());
}
```

Add a helper at the bottom of the test class:
```java
private Goal createGoal(Long goalId, Long userId) {
    Goal goal = new Goal();
    goal.setId(goalId);
    goal.setUserId(userId);
    goal.setTitle("Test goal");
    goal.setStatus(GoalStatus.NOT_STARTED);
    goal.setValidationStatus(GoalValidationStatus.PENDING_VALIDATION);
    goal.setCreatedAt(LocalDateTime.now());
    goal.setUpdatedAt(LocalDateTime.now());
    return goal;
}
```

**Step 3: Run tests to confirm they fail**

```bash
cd backend && mvn test -pl . -Dtest=TherapistServiceTest
# Expected: compilation error — methods don't exist yet
```

**Step 4: Add methods to `TherapistService.java`**

Add these four methods (and ensure `GoalValidationStatus`, `GoalRequest`, `LocalDateTime` are imported):

```java
@Transactional
public GoalResponse createGoalForPatient(Long therapistId, Long patientId, GoalRequest request) {
    LOG.info("Therapist {} creating goal for patient {}", therapistId, patientId);
    validateTherapistPatientRelationship(therapistId, patientId);

    Goal goal = new Goal();
    goal.setUserId(patientId);
    goal.setCreatedAt(LocalDateTime.now());
    goal.setUpdatedAt(LocalDateTime.now());
    goal.setValidationStatus(GoalValidationStatus.VALIDATED);
    goal.setCreatedBy(therapistId);
    goal.setValidatedBy(therapistId);
    goal.setValidatedAt(LocalDateTime.now());
    goalMapper.applyRequest(request, goal);

    Goal saved = goalRepository.save(goal);
    return goalMapper.toGoalResponse(saved);
}

@Transactional
public GoalResponse editGoalForPatient(Long therapistId, Long patientId,
                                        Long goalId, GoalRequest request) {
    LOG.info("Therapist {} editing goal {} for patient {}", therapistId, goalId, patientId);
    validateTherapistPatientRelationship(therapistId, patientId);

    Goal goal = goalRepository.findByIdAndUserId(goalId, patientId)
            .orElseThrow(() -> new IllegalArgumentException("Goal not found: " + goalId));
    goalMapper.applyRequest(request, goal);
    goal.setValidationStatus(GoalValidationStatus.OVERRIDDEN);
    goal.setValidatedBy(therapistId);
    goal.setValidatedAt(LocalDateTime.now());
    goal.setUpdatedAt(LocalDateTime.now());

    Goal saved = goalRepository.save(goal);
    return goalMapper.toGoalResponse(saved);
}

@Transactional
public GoalResponse validateGoal(Long therapistId, Long patientId, Long goalId) {
    LOG.info("Therapist {} validating goal {} for patient {}", therapistId, goalId, patientId);
    validateTherapistPatientRelationship(therapistId, patientId);

    Goal goal = goalRepository.findByIdAndUserId(goalId, patientId)
            .orElseThrow(() -> new IllegalArgumentException("Goal not found: " + goalId));
    goal.setValidationStatus(GoalValidationStatus.VALIDATED);
    goal.setValidatedBy(therapistId);
    goal.setValidatedAt(LocalDateTime.now());
    goal.setUpdatedAt(LocalDateTime.now());

    Goal saved = goalRepository.save(goal);
    return goalMapper.toGoalResponse(saved);
}

@Transactional
public GoalResponse rejectGoal(Long therapistId, Long patientId, Long goalId) {
    LOG.info("Therapist {} rejecting goal {} for patient {}", therapistId, goalId, patientId);
    validateTherapistPatientRelationship(therapistId, patientId);

    Goal goal = goalRepository.findByIdAndUserId(goalId, patientId)
            .orElseThrow(() -> new IllegalArgumentException("Goal not found: " + goalId));
    goal.setValidationStatus(GoalValidationStatus.REJECTED);
    goal.setValidatedBy(therapistId);
    goal.setValidatedAt(LocalDateTime.now());
    goal.setUpdatedAt(LocalDateTime.now());

    Goal saved = goalRepository.save(goal);
    return goalMapper.toGoalResponse(saved);
}
```

Also add the import at the top of `TherapistService.java`:
```java
import com.mindtrack.goals.dto.GoalRequest;
import com.mindtrack.goals.model.GoalValidationStatus;
import java.time.LocalDateTime;
```

**Step 5: Run service tests — confirm they pass**

```bash
cd backend && mvn test -pl . -Dtest=TherapistServiceTest
# Expected: all PASS
```

**Step 6: Add controller endpoints** — add to `TherapistController.java`:

```java
@PostMapping("/patients/{patientId}/goals")
public ResponseEntity<GoalResponse> createGoalForPatient(
        @PathVariable Long patientId,
        @RequestBody @Valid GoalRequest request,
        Authentication authentication) {
    Long therapistId = (Long) authentication.getPrincipal();
    return ResponseEntity.status(201)
            .body(therapistService.createGoalForPatient(therapistId, patientId, request));
}

@PutMapping("/patients/{patientId}/goals/{goalId}")
public ResponseEntity<GoalResponse> editGoalForPatient(
        @PathVariable Long patientId,
        @PathVariable Long goalId,
        @RequestBody @Valid GoalRequest request,
        Authentication authentication) {
    Long therapistId = (Long) authentication.getPrincipal();
    return ResponseEntity.ok(
            therapistService.editGoalForPatient(therapistId, patientId, goalId, request));
}

@PostMapping("/patients/{patientId}/goals/{goalId}/validate")
public ResponseEntity<GoalResponse> validateGoal(
        @PathVariable Long patientId,
        @PathVariable Long goalId,
        Authentication authentication) {
    Long therapistId = (Long) authentication.getPrincipal();
    return ResponseEntity.ok(therapistService.validateGoal(therapistId, patientId, goalId));
}

@PostMapping("/patients/{patientId}/goals/{goalId}/reject")
public ResponseEntity<GoalResponse> rejectGoal(
        @PathVariable Long patientId,
        @PathVariable Long goalId,
        Authentication authentication) {
    Long therapistId = (Long) authentication.getPrincipal();
    return ResponseEntity.ok(therapistService.rejectGoal(therapistId, patientId, goalId));
}
```

Add missing imports to the controller:
```java
import com.mindtrack.goals.dto.GoalRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
```

**Step 7: Add controller tests** — add to `TherapistControllerTest.java`:

```java
private static UsernamePasswordAuthenticationToken mockTherapistAuth() {
    return new UsernamePasswordAuthenticationToken(
            1L, null, List.of(new SimpleGrantedAuthority("ROLE_THERAPIST")));
}

@Test
void shouldCreateGoalForPatient() throws Exception {
    GoalRequest request = new GoalRequest();
    request.setTitle("New therapy goal");
    GoalResponse response = new GoalResponse();
    response.setId(5L);
    response.setTitle("New therapy goal");
    response.setValidationStatus(GoalValidationStatus.VALIDATED);

    when(therapistService.createGoalForPatient(eq(1L), eq(2L), any()))
            .thenReturn(response);

    mockMvc.perform(post("/api/therapist/patients/2/goals")
                    .with(csrf())
                    .with(authentication(mockTherapistAuth()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.validationStatus").value("VALIDATED"));
}

@Test
void shouldValidateGoalForPatient() throws Exception {
    GoalResponse response = new GoalResponse();
    response.setId(10L);
    response.setValidationStatus(GoalValidationStatus.VALIDATED);
    when(therapistService.validateGoal(1L, 2L, 10L)).thenReturn(response);

    mockMvc.perform(post("/api/therapist/patients/2/goals/10/validate")
                    .with(csrf())
                    .with(authentication(mockTherapistAuth())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.validationStatus").value("VALIDATED"));
}

@Test
void shouldRejectGoalForPatient() throws Exception {
    GoalResponse response = new GoalResponse();
    response.setId(10L);
    response.setValidationStatus(GoalValidationStatus.REJECTED);
    when(therapistService.rejectGoal(1L, 2L, 10L)).thenReturn(response);

    mockMvc.perform(post("/api/therapist/patients/2/goals/10/reject")
                    .with(csrf())
                    .with(authentication(mockTherapistAuth())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.validationStatus").value("REJECTED"));
}
```

**Step 8: Run all therapist tests**

```bash
cd backend && mvn test -pl . -Dtest="TherapistServiceTest,TherapistControllerTest,TherapistMapperTest"
# Expected: all PASS
```

**Step 9: Commit**

```bash
git add backend/src/main/java/com/mindtrack/therapist/
git add backend/src/main/java/com/mindtrack/goals/
git add backend/src/test/java/com/mindtrack/therapist/
git commit -m "feat(therapist): add goal write endpoints — create, edit, validate, reject for patient goals"
```

---

## Task 5: Invite token backend

**Files:**
- Create: `backend/src/main/java/com/mindtrack/therapist/model/InviteToken.java`
- Create: `backend/src/main/java/com/mindtrack/therapist/model/InitiatorRole.java`
- Create: `backend/src/main/java/com/mindtrack/therapist/repository/InviteTokenRepository.java`
- Create: `backend/src/main/java/com/mindtrack/therapist/dto/InviteGenerateResponse.java`
- Create: `backend/src/main/java/com/mindtrack/therapist/dto/InvitePreviewResponse.java`
- Create: `backend/src/main/java/com/mindtrack/therapist/service/InviteService.java`
- Create: `backend/src/main/java/com/mindtrack/therapist/controller/InviteController.java`
- Create: `backend/src/test/java/com/mindtrack/therapist/service/InviteServiceTest.java`
- Create: `backend/src/test/java/com/mindtrack/therapist/controller/InviteControllerTest.java`

**Step 1: Create `InitiatorRole.java`**

```java
package com.mindtrack.therapist.model;

public enum InitiatorRole {
    PATIENT,
    THERAPIST
}
```

**Step 2: Create `InviteToken.java`**

```java
package com.mindtrack.therapist.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "invite_tokens")
public class InviteToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Column(name = "initiator_id", nullable = false)
    private Long initiatorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "initiator_role", nullable = false)
    private InitiatorRole initiatorRole;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public InviteToken() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Long getInitiatorId() { return initiatorId; }
    public void setInitiatorId(Long initiatorId) { this.initiatorId = initiatorId; }
    public InitiatorRole getInitiatorRole() { return initiatorRole; }
    public void setInitiatorRole(InitiatorRole initiatorRole) { this.initiatorRole = initiatorRole; }
    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
```

**Step 3: Create `InviteTokenRepository.java`**

```java
package com.mindtrack.therapist.repository;

import com.mindtrack.therapist.model.InviteToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InviteTokenRepository extends JpaRepository<InviteToken, Long> {
    Optional<InviteToken> findByToken(String token);
}
```

**Step 4: Create DTOs**

`InviteGenerateResponse.java`:
```java
package com.mindtrack.therapist.dto;

public record InviteGenerateResponse(String token, String url) {}
```

`InvitePreviewResponse.java`:
```java
package com.mindtrack.therapist.dto;

public record InvitePreviewResponse(String initiatorName, String initiatorRole) {}
```

**Step 5: Write failing `InviteServiceTest.java`**

```java
package com.mindtrack.therapist.service;

import com.mindtrack.auth.repository.UserRepository;
import com.mindtrack.common.model.User;
import com.mindtrack.therapist.dto.InviteGenerateResponse;
import com.mindtrack.therapist.dto.InvitePreviewResponse;
import com.mindtrack.therapist.model.InitiatorRole;
import com.mindtrack.therapist.model.InviteToken;
import com.mindtrack.therapist.model.TherapistPatient;
import com.mindtrack.therapist.model.TherapistPatientStatus;
import com.mindtrack.therapist.repository.InviteTokenRepository;
import com.mindtrack.therapist.repository.TherapistPatientRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InviteServiceTest {

    @Mock private InviteTokenRepository inviteTokenRepository;
    @Mock private TherapistPatientRepository therapistPatientRepository;
    @Mock private UserRepository userRepository;

    private InviteService inviteService;

    @BeforeEach
    void setUp() {
        inviteService = new InviteService(inviteTokenRepository,
                therapistPatientRepository, userRepository, "http://localhost:3000");
    }

    @Test
    void shouldGenerateInviteToken() {
        when(inviteTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InviteGenerateResponse response = inviteService.generateToken(1L, InitiatorRole.THERAPIST);

        assertNotNull(response.token());
        assertEquals(64, response.token().length());
        assertTrue(response.url().contains(response.token()));
    }

    @Test
    void shouldPreviewInvite() {
        InviteToken token = makeToken(InitiatorRole.THERAPIST, 1L);
        User user = makeUser(1L, "Dr. Smith");
        when(inviteTokenRepository.findByToken("abc")).thenReturn(Optional.of(token));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        InvitePreviewResponse response = inviteService.previewInvite("abc");

        assertEquals("Dr. Smith", response.initiatorName());
        assertEquals("THERAPIST", response.initiatorRole());
    }

    @Test
    void shouldThrowWhenTokenExpired() {
        InviteToken token = makeToken(InitiatorRole.THERAPIST, 1L);
        token.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(inviteTokenRepository.findByToken("expired")).thenReturn(Optional.of(token));

        assertThrows(IllegalArgumentException.class,
                () -> inviteService.previewInvite("expired"));
    }

    @Test
    void therapistInitiatesPatientAcceptsGoesActiveImmediately() {
        InviteToken token = makeToken(InitiatorRole.THERAPIST, 10L);
        User acceptor = makeUser(20L, "Patient");
        acceptor.setRoleName("USER");

        when(inviteTokenRepository.findByToken("t1")).thenReturn(Optional.of(token));
        when(userRepository.findById(20L)).thenReturn(Optional.of(acceptor));
        when(therapistPatientRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(inviteTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        inviteService.acceptInvite("t1", 20L, InitiatorRole.PATIENT);

        ArgumentCaptor<TherapistPatient> captor = ArgumentCaptor.forClass(TherapistPatient.class);
        verify(therapistPatientRepository).save(captor.capture());
        assertEquals(TherapistPatientStatus.ACTIVE, captor.getValue().getStatus());
    }

    @Test
    void patientInitiatesTherapistAcceptsGoesPending() {
        InviteToken token = makeToken(InitiatorRole.PATIENT, 20L);
        User acceptor = makeUser(10L, "Dr. Smith");
        acceptor.setRoleName("THERAPIST");

        when(inviteTokenRepository.findByToken("t2")).thenReturn(Optional.of(token));
        when(userRepository.findById(10L)).thenReturn(Optional.of(acceptor));
        when(therapistPatientRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(inviteTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        inviteService.acceptInvite("t2", 10L, InitiatorRole.THERAPIST);

        ArgumentCaptor<TherapistPatient> captor = ArgumentCaptor.forClass(TherapistPatient.class);
        verify(therapistPatientRepository).save(captor.capture());
        assertEquals(TherapistPatientStatus.PENDING, captor.getValue().getStatus());
    }

    private InviteToken makeToken(InitiatorRole role, Long initiatorId) {
        InviteToken t = new InviteToken();
        t.setToken("tok-" + initiatorId);
        t.setInitiatorRole(role);
        t.setInitiatorId(initiatorId);
        t.setExpiresAt(LocalDateTime.now().plusDays(7));
        t.setCreatedAt(LocalDateTime.now());
        return t;
    }

    private User makeUser(Long id, String name) {
        User u = new User();
        u.setId(id);
        u.setName(name);
        return u;
    }
}
```

**Step 6: Run to confirm it fails (class doesn't exist)**

```bash
cd backend && mvn test -pl . -Dtest=InviteServiceTest
# Expected: compilation error
```

**Step 7: Create `InviteService.java`**

> Note: `User` has `getId()`, `getName()`. Check what role method is available — `TherapistService` accesses `user.getRole()` through the user object. The role field may need to be accessed via the `Role` entity. Look at how `UserService` works for reference, or use `authentication.getAuthorities()` in the controller and pass the role string. For simplicity, InviteService will accept the caller's role as a parameter rather than doing a DB lookup.

```java
package com.mindtrack.therapist.service;

import com.mindtrack.auth.repository.UserRepository;
import com.mindtrack.common.model.User;
import com.mindtrack.therapist.dto.InviteGenerateResponse;
import com.mindtrack.therapist.dto.InvitePreviewResponse;
import com.mindtrack.therapist.model.InitiatorRole;
import com.mindtrack.therapist.model.InviteToken;
import com.mindtrack.therapist.model.TherapistPatient;
import com.mindtrack.therapist.model.TherapistPatientStatus;
import com.mindtrack.therapist.repository.InviteTokenRepository;
import com.mindtrack.therapist.repository.TherapistPatientRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InviteService {

    private static final Logger LOG = LoggerFactory.getLogger(InviteService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final InviteTokenRepository inviteTokenRepository;
    private final TherapistPatientRepository therapistPatientRepository;
    private final UserRepository userRepository;
    private final String frontendUrl;

    public InviteService(InviteTokenRepository inviteTokenRepository,
                         TherapistPatientRepository therapistPatientRepository,
                         UserRepository userRepository,
                         @Value("${mindtrack.auth.frontend-url}") String frontendUrl) {
        this.inviteTokenRepository = inviteTokenRepository;
        this.therapistPatientRepository = therapistPatientRepository;
        this.userRepository = userRepository;
        this.frontendUrl = frontendUrl;
    }

    @Transactional
    public InviteGenerateResponse generateToken(Long initiatorId, InitiatorRole role) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = HexFormat.of().formatHex(bytes); // 64-char hex

        InviteToken inviteToken = new InviteToken();
        inviteToken.setToken(token);
        inviteToken.setInitiatorId(initiatorId);
        inviteToken.setInitiatorRole(role);
        inviteToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        inviteToken.setCreatedAt(LocalDateTime.now());
        inviteTokenRepository.save(inviteToken);

        String url = frontendUrl + "/invite/" + token;
        LOG.info("Generated invite token for {} (role={})", initiatorId, role);
        return new InviteGenerateResponse(token, url);
    }

    public InvitePreviewResponse previewInvite(String token) {
        InviteToken inviteToken = findValidToken(token);
        User initiator = userRepository.findById(inviteToken.getInitiatorId())
                .orElseThrow(() -> new IllegalArgumentException("Initiator not found"));
        return new InvitePreviewResponse(initiator.getName(),
                inviteToken.getInitiatorRole().name());
    }

    @Transactional
    public void acceptInvite(String token, Long acceptorId, InitiatorRole acceptorRole) {
        InviteToken inviteToken = findValidToken(token);

        if (inviteToken.getInitiatorRole() == acceptorRole) {
            throw new IllegalArgumentException("Cannot accept your own role's invite");
        }

        Long therapistId;
        Long patientId;
        TherapistPatientStatus status;

        if (inviteToken.getInitiatorRole() == InitiatorRole.THERAPIST) {
            // Therapist initiated → patient accepts → ACTIVE immediately
            therapistId = inviteToken.getInitiatorId();
            patientId = acceptorId;
            status = TherapistPatientStatus.ACTIVE;
        } else {
            // Patient initiated → therapist accepts → PENDING (therapist must approve)
            therapistId = acceptorId;
            patientId = inviteToken.getInitiatorId();
            status = TherapistPatientStatus.PENDING;
        }

        TherapistPatient rel = new TherapistPatient(therapistId, patientId, status);
        therapistPatientRepository.save(rel);

        inviteToken.setUsedAt(LocalDateTime.now());
        inviteTokenRepository.save(inviteToken);

        LOG.info("Accepted invite: therapist={} patient={} status={}", therapistId, patientId, status);
    }

    private InviteToken findValidToken(String token) {
        InviteToken inviteToken = inviteTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invite token not found"));
        if (inviteToken.getUsedAt() != null) {
            throw new IllegalArgumentException("Invite token already used");
        }
        if (inviteToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invite token expired");
        }
        return inviteToken;
    }
}
```

**Step 8: Run InviteServiceTest**

```bash
cd backend && mvn test -pl . -Dtest=InviteServiceTest
# Expected: all PASS (fix compilation errors in User mock if needed — check User has setId/getName)
```

**Step 9: Create `InviteController.java`**

```java
package com.mindtrack.therapist.controller;

import com.mindtrack.therapist.dto.InviteGenerateResponse;
import com.mindtrack.therapist.dto.InvitePreviewResponse;
import com.mindtrack.therapist.model.InitiatorRole;
import com.mindtrack.therapist.service.InviteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invites")
public class InviteController {

    private final InviteService inviteService;

    public InviteController(InviteService inviteService) {
        this.inviteService = inviteService;
    }

    @PostMapping("/generate")
    public ResponseEntity<InviteGenerateResponse> generate(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        InitiatorRole role = resolveRole(authentication);
        return ResponseEntity.ok(inviteService.generateToken(userId, role));
    }

    @GetMapping("/{token}")
    public ResponseEntity<InvitePreviewResponse> preview(@PathVariable String token) {
        return ResponseEntity.ok(inviteService.previewInvite(token));
    }

    @PostMapping("/{token}/accept")
    public ResponseEntity<Void> accept(@PathVariable String token, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        InitiatorRole role = resolveRole(authentication);
        inviteService.acceptInvite(token, userId, role);
        return ResponseEntity.ok().build();
    }

    private InitiatorRole resolveRole(Authentication authentication) {
        boolean isTherapist = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_THERAPIST"::equals);
        return isTherapist ? InitiatorRole.THERAPIST : InitiatorRole.PATIENT;
    }
}
```

Also add approve/reject endpoints to `TherapistController.java` for PENDING → ACTIVE / INACTIVE:

```java
@PostMapping("/patients/{patientId}/accept")
public ResponseEntity<Void> acceptPatient(@PathVariable Long patientId,
                                           Authentication authentication) {
    Long therapistId = (Long) authentication.getPrincipal();
    therapistService.setPatientStatus(therapistId, patientId, TherapistPatientStatus.ACTIVE);
    return ResponseEntity.ok().build();
}

@PostMapping("/patients/{patientId}/reject")
public ResponseEntity<Void> rejectPatient(@PathVariable Long patientId,
                                           Authentication authentication) {
    Long therapistId = (Long) authentication.getPrincipal();
    therapistService.setPatientStatus(therapistId, patientId, TherapistPatientStatus.INACTIVE);
    return ResponseEntity.ok().build();
}
```

Add to `TherapistService.java`:

```java
@Transactional
public void setPatientStatus(Long therapistId, Long patientId, TherapistPatientStatus newStatus) {
    TherapistPatient rel = therapistPatientRepository
            .findByTherapistIdAndPatientId(therapistId, patientId)
            .orElseThrow(() -> new IllegalArgumentException("Relationship not found"));
    rel.setStatus(newStatus);
    therapistPatientRepository.save(rel);
    LOG.info("Set therapist-patient status: therapist={} patient={} status={}",
            therapistId, patientId, newStatus);
}
```

Add to `TherapistPatientRepository.java`:
```java
Optional<TherapistPatient> findByTherapistIdAndPatientId(Long therapistId, Long patientId);
```

**Step 10: Run all tests**

```bash
cd backend && mvn test
# Expected: all PASS
```

**Step 11: Commit**

```bash
git add backend/src/main/java/com/mindtrack/therapist/
git add backend/src/test/java/com/mindtrack/therapist/
git commit -m "feat(invites): invite token system — generate, preview, accept with role-based status logic"
```

---

## Task 6: Therapist patient-list exposes PENDING, add `listPendingPatients`

**Files:**
- Modify: `backend/src/main/java/com/mindtrack/therapist/service/TherapistService.java`
- Modify: `backend/src/main/java/com/mindtrack/therapist/controller/TherapistController.java`

**Step 1: Add `listPendingPatients()` to TherapistService**

```java
public List<PatientSummaryResponse> listPendingPatients(Long therapistId) {
    LOG.info("Listing pending patients for therapist {}", therapistId);
    return therapistPatientRepository
            .findByTherapistIdAndStatus(therapistId, TherapistPatientStatus.PENDING)
            .stream()
            .map(rel -> {
                User patient = userRepository.findById(rel.getPatientId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Patient not found: " + rel.getPatientId()));
                return therapistMapper.toPatientSummary(patient, 0, 0, 0, null);
            })
            .toList();
}
```

**Step 2: Add endpoint to TherapistController**

```java
@GetMapping("/patients/pending")
public ResponseEntity<List<PatientSummaryResponse>> listPendingPatients(
        Authentication authentication) {
    Long therapistId = (Long) authentication.getPrincipal();
    return ResponseEntity.ok(therapistService.listPendingPatients(therapistId));
}
```

**Step 3: Run tests**

```bash
cd backend && mvn test -pl . -Dtest="TherapistServiceTest,TherapistControllerTest"
# Expected: all PASS
```

**Step 4: Commit**

```bash
git add backend/src/main/java/com/mindtrack/therapist/
git commit -m "feat(therapist): expose pending patient queue and accept/reject endpoints"
```

---

## Task 7: Onboarding — backend module

**Files:**
- Create: `backend/src/main/java/com/mindtrack/onboarding/dto/SurveyRequest.java`
- Create: `backend/src/main/java/com/mindtrack/onboarding/dto/OnboardingChatRequest.java`
- Create: `backend/src/main/java/com/mindtrack/onboarding/service/OnboardingService.java`
- Create: `backend/src/main/java/com/mindtrack/onboarding/controller/OnboardingController.java`
- Modify: `backend/src/main/java/com/mindtrack/profile/model/UserProfile.java`
- Modify: `backend/src/main/java/com/mindtrack/profile/service/ProfileService.java`
- Create: `backend/src/test/java/com/mindtrack/onboarding/service/OnboardingServiceTest.java`
- Modify: `backend/src/main/java/com/mindtrack/ai/model/ConversationType.java`

**Step 1: Add `ONBOARDING` to `ConversationType.java`**

```java
public enum ConversationType {
    QUICK_CHECKIN,
    COACHING,
    SESSION_SUMMARY,
    /** Guided first-login chat to propose initial goals. Budget: ~1000 tokens. */
    ONBOARDING
}
```

**Step 2: Add `onboardingCompleted` to `UserProfile.java`** — after `tutorialCompleted`:

```java
@Column(name = "onboarding_completed", nullable = false)
private boolean onboardingCompleted;
```

Add getter/setter:
```java
public boolean isOnboardingCompleted() { return onboardingCompleted; }
public void setOnboardingCompleted(boolean onboardingCompleted) {
    this.onboardingCompleted = onboardingCompleted;
}
```

**Step 3: Add `completeOnboarding()` to `ProfileService.java`**

```java
@Transactional
public void completeOnboarding(Long userId) {
    LOG.info("Marking onboarding complete for user {}", userId);
    UserProfile profile = profileRepository.findByUserId(userId)
            .orElseGet(() -> createDefaultProfile(userId));
    profile.setOnboardingCompleted(true);
    profileRepository.save(profile);
}
```

**Step 4: Create `SurveyRequest.java`**

```java
package com.mindtrack.onboarding.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class SurveyRequest {

    @NotNull
    private Integer moodBaseline;     // 1–10

    @NotNull
    private Integer anxietyLevel;     // 1–10

    @NotNull
    private Integer sleepQuality;     // 1–10

    private List<String> lifeAreas;   // e.g. ["work", "relationships", "health"]
    private List<String> challenges;  // free-text challenges
    private List<String> goalCategories; // desired goal categories

    public Integer getMoodBaseline() { return moodBaseline; }
    public void setMoodBaseline(Integer moodBaseline) { this.moodBaseline = moodBaseline; }
    public Integer getAnxietyLevel() { return anxietyLevel; }
    public void setAnxietyLevel(Integer anxietyLevel) { this.anxietyLevel = anxietyLevel; }
    public Integer getSleepQuality() { return sleepQuality; }
    public void setSleepQuality(Integer sleepQuality) { this.sleepQuality = sleepQuality; }
    public List<String> getLifeAreas() { return lifeAreas; }
    public void setLifeAreas(List<String> lifeAreas) { this.lifeAreas = lifeAreas; }
    public List<String> getChallenges() { return challenges; }
    public void setChallenges(List<String> challenges) { this.challenges = challenges; }
    public List<String> getGoalCategories() { return goalCategories; }
    public void setGoalCategories(List<String> goalCategories) { this.goalCategories = goalCategories; }
}
```

**Step 5: Write failing `OnboardingServiceTest.java`**

```java
package com.mindtrack.onboarding.service;

import com.mindtrack.goals.model.GoalValidationStatus;
import com.mindtrack.goals.repository.GoalRepository;
import com.mindtrack.goals.service.GoalMapper;
import com.mindtrack.onboarding.dto.SurveyRequest;
import com.mindtrack.profile.service.ProfileService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnboardingServiceTest {

    @Mock private GoalRepository goalRepository;
    @Mock private ProfileService profileService;

    private OnboardingService onboardingService;

    @BeforeEach
    void setUp() {
        GoalMapper goalMapper = new GoalMapper();
        onboardingService = new OnboardingService(goalRepository, goalMapper, profileService);
    }

    @Test
    void shouldCreateGoalsFromSurveyWithPendingValidation() {
        SurveyRequest request = new SurveyRequest();
        request.setMoodBaseline(5);
        request.setAnxietyLevel(7);
        request.setSleepQuality(4);
        request.setLifeAreas(List.of("work", "health"));
        request.setChallenges(List.of("stress at work"));
        request.setGoalCategories(List.of("wellness"));

        when(goalRepository.save(any())).thenAnswer(inv -> {
            var g = inv.getArgument(0,
                    com.mindtrack.goals.model.Goal.class);
            g.setId((long) (Math.random() * 1000));
            return g;
        });

        var goals = onboardingService.generateGoalsFromSurvey(1L, request);

        assertFalse(goals.isEmpty());
        assertTrue(goals.size() >= 1 && goals.size() <= 5);
        goals.forEach(g ->
                assertEquals(GoalValidationStatus.PENDING_VALIDATION, g.getValidationStatus()));
        verify(profileService).completeOnboarding(1L);
    }

    @Test
    void shouldCreateAtLeastOneGoalPerLifeArea() {
        SurveyRequest request = new SurveyRequest();
        request.setMoodBaseline(6);
        request.setAnxietyLevel(5);
        request.setSleepQuality(6);
        request.setLifeAreas(List.of("fitness", "mindfulness"));

        when(goalRepository.save(any())).thenAnswer(inv -> {
            var g = inv.getArgument(0,
                    com.mindtrack.goals.model.Goal.class);
            g.setId(1L);
            return g;
        });

        var goals = onboardingService.generateGoalsFromSurvey(1L, request);

        assertTrue(goals.size() >= 2);
    }
}
```

**Step 6: Run to confirm failure**

```bash
cd backend && mvn test -pl . -Dtest=OnboardingServiceTest
# Expected: compilation error
```

**Step 7: Create `OnboardingService.java`**

```java
package com.mindtrack.onboarding.service;

import com.mindtrack.goals.dto.GoalResponse;
import com.mindtrack.goals.model.Goal;
import com.mindtrack.goals.model.GoalStatus;
import com.mindtrack.goals.model.GoalValidationStatus;
import com.mindtrack.goals.repository.GoalRepository;
import com.mindtrack.goals.service.GoalMapper;
import com.mindtrack.onboarding.dto.SurveyRequest;
import com.mindtrack.profile.service.ProfileService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OnboardingService {

    private static final Logger LOG = LoggerFactory.getLogger(OnboardingService.class);

    private final GoalRepository goalRepository;
    private final GoalMapper goalMapper;
    private final ProfileService profileService;

    public OnboardingService(GoalRepository goalRepository, GoalMapper goalMapper,
                             ProfileService profileService) {
        this.goalRepository = goalRepository;
        this.goalMapper = goalMapper;
        this.profileService = profileService;
    }

    @Transactional
    public List<GoalResponse> generateGoalsFromSurvey(Long userId, SurveyRequest request) {
        LOG.info("Generating goals from survey for user {}", userId);
        List<Goal> proposed = new ArrayList<>();

        // Mood / anxiety — propose a journaling goal if mood is low
        if (request.getMoodBaseline() != null && request.getMoodBaseline() < 6) {
            proposed.add(buildGoal(userId, "Daily mood journaling",
                    "Track your mood every day to identify patterns", "Mental Health"));
        }

        // Sleep — propose sleep hygiene goal if poor
        if (request.getSleepQuality() != null && request.getSleepQuality() < 6) {
            proposed.add(buildGoal(userId, "Improve sleep routine",
                    "Establish a consistent bedtime and wind-down routine", "Wellness"));
        }

        // Life areas — one goal per area
        if (request.getLifeAreas() != null) {
            for (String area : request.getLifeAreas()) {
                String title = "Improve " + area.toLowerCase();
                String desc = "Set and work towards specific improvements in: " + area;
                proposed.add(buildGoal(userId, title, desc, area));
            }
        }

        // Cap at 5 goals
        List<Goal> capped = proposed.subList(0, Math.min(proposed.size(), 5));
        List<GoalResponse> saved = new ArrayList<>();
        for (Goal goal : capped) {
            saved.add(goalMapper.toGoalResponse(goalRepository.save(goal)));
        }

        profileService.completeOnboarding(userId);
        LOG.info("Created {} onboarding goals for user {}", saved.size(), userId);
        return saved;
    }

    private Goal buildGoal(Long userId, String title, String description, String category) {
        Goal goal = new Goal();
        goal.setUserId(userId);
        goal.setTitle(title);
        goal.setDescription(description);
        goal.setCategory(category);
        goal.setStatus(GoalStatus.NOT_STARTED);
        goal.setValidationStatus(GoalValidationStatus.PENDING_VALIDATION);
        goal.setCreatedBy(userId);
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());
        return goal;
    }
}
```

**Step 8: Run OnboardingServiceTest**

```bash
cd backend && mvn test -pl . -Dtest=OnboardingServiceTest
# Expected: all PASS
```

**Step 9: Create `OnboardingController.java`**

```java
package com.mindtrack.onboarding.controller;

import com.mindtrack.goals.dto.GoalResponse;
import com.mindtrack.onboarding.dto.SurveyRequest;
import com.mindtrack.onboarding.service.OnboardingService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @PostMapping("/survey")
    public ResponseEntity<List<GoalResponse>> survey(
            @RequestBody @Valid SurveyRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(onboardingService.generateGoalsFromSurvey(userId, request));
    }
}
```

**Step 10: Run all tests**

```bash
cd backend && mvn test
# Expected: all PASS
```

**Step 11: Commit**

```bash
git add backend/src/main/java/com/mindtrack/
git add backend/src/test/java/com/mindtrack/onboarding/
git commit -m "feat(onboarding): survey-based goal generation, onboardingCompleted flag, ONBOARDING conversation type"
```

---

## Task 8: Whisper transcription — backend

**Files:**
- Modify: `backend/src/main/java/com/mindtrack/interview/model/Interview.java`
- Modify: `backend/src/main/java/com/mindtrack/interview/service/AudioService.java`
- Create: `backend/src/main/java/com/mindtrack/interview/config/WhisperProperties.java`
- Create: `backend/src/main/java/com/mindtrack/interview/model/TranscriptionStatus.java`
- Modify: `backend/src/main/resources/application-local.yml`
- Modify: `backend/src/main/resources/application-docker.yml`
- Test: `backend/src/test/java/com/mindtrack/interview/service/AudioServiceTest.java`

**Step 1: Create `TranscriptionStatus.java`**

```java
package com.mindtrack.interview.model;

public enum TranscriptionStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}
```

**Step 2: Add `transcriptionStatus` to `Interview.java`** — after `audioExpiresAt`:

```java
@Enumerated(EnumType.STRING)
@Column(name = "transcription_status")
private TranscriptionStatus transcriptionStatus;
```

Add getter/setter. Also add import:
```java
import com.mindtrack.interview.model.TranscriptionStatus;
```

**Step 3: Create `WhisperProperties.java`**

```java
package com.mindtrack.interview.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mindtrack.ai")
public class WhisperProperties {

    private String whisperApiUrl = "https://api.openai.com/v1/audio/transcriptions";
    private String whisperApiKey = "dummy-key-for-local";

    public String getWhisperApiUrl() { return whisperApiUrl; }
    public void setWhisperApiUrl(String whisperApiUrl) { this.whisperApiUrl = whisperApiUrl; }
    public String getWhisperApiKey() { return whisperApiKey; }
    public void setWhisperApiKey(String whisperApiKey) { this.whisperApiKey = whisperApiKey; }
}
```

**Step 4: Add Whisper config to `application-local.yml`** — under `mindtrack.ai`:

```yaml
mindtrack:
  ai:
    whisper-api-url: https://api.openai.com/v1/audio/transcriptions
    whisper-api-key: ${OPENAI_API_KEY:dummy-key-for-local}
```

Also add to `application-docker.yml`:

```yaml
mindtrack:
  ai:
    whisper-api-url: https://api.openai.com/v1/audio/transcriptions
    whisper-api-key: ${OPENAI_API_KEY:dummy-key-for-local}
```

**Step 5: Enable `@Async` in Spring Boot** — check if `MindTrackApplication.java` already has `@EnableAsync`. If not, add it:

```java
@SpringBootApplication
@EnableAsync
public class MindTrackApplication { ... }
```

Add import: `import org.springframework.scheduling.annotation.EnableAsync;`

**Step 6: Write a failing test for async transcription**

Add to `AudioServiceTest.java`:

```java
@Test
void shouldSetTranscriptionStatusToInProgressAfterUpload() throws Exception {
    // Arrange
    Interview interview = new Interview();
    interview.setId(1L);
    interview.setUserId(1L);
    when(interviewRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(interview));
    when(storageService.generateAccessUrl(any())).thenReturn("http://s3/audio.webm");
    doNothing().when(storageService).upload(any(), any(), any(), anyLong());

    MockMultipartFile file = new MockMultipartFile(
            "file", "rec.webm", "audio/webm", new byte[100]);

    audioService.uploadAudio(1L, 1L, file);

    assertEquals(TranscriptionStatus.IN_PROGRESS, interview.getTranscriptionStatus());
}
```

**Step 7: Run to confirm failure**

```bash
cd backend && mvn test -pl . -Dtest=AudioServiceTest#shouldSetTranscriptionStatusToInProgressAfterUpload
# Expected: FAIL — transcriptionStatus is null
```

**Step 8: Update `AudioService.uploadAudio()`** — after `interview.setTranscriptionText(null)` add:

```java
interview.setTranscriptionStatus(TranscriptionStatus.IN_PROGRESS);
```

Then after `interviewRepository.save(interview)`, trigger async transcription:

```java
transcribeAsync(interview.getId(), interview.getAudioS3Key(), userId);
```

Add the async method and Whisper HTTP call to `AudioService.java`:

```java
private final WhisperProperties whisperProperties;
private final RestTemplate restTemplate;

// Update constructor to accept WhisperProperties and RestTemplate:
public AudioService(InterviewRepository interviewRepository, StorageService storageService,
                    StorageProperties storageProperties, WhisperProperties whisperProperties,
                    RestTemplate restTemplate) {
    this.interviewRepository = interviewRepository;
    this.storageService = storageService;
    this.storageProperties = storageProperties;
    this.whisperProperties = whisperProperties;
    this.restTemplate = restTemplate;
}

@Async
public void transcribeAsync(Long interviewId, String s3Key, Long userId) {
    LOG.info("Starting async Whisper transcription for interview {}", interviewId);
    try {
        byte[] audioBytes = storageService.download(s3Key);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + whisperProperties.getWhisperApiKey());
        headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);

        org.springframework.util.MultiValueMap<String, Object> body =
                new org.springframework.util.LinkedMultiValueMap<>();
        body.add("file", new org.springframework.core.io.ByteArrayResource(audioBytes) {
            @Override public String getFilename() { return "audio.webm"; }
        });
        body.add("model", "whisper-1");

        org.springframework.http.HttpEntity<org.springframework.util.MultiValueMap<String, Object>>
                requestEntity = new org.springframework.http.HttpEntity<>(body, headers);

        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> response = restTemplate.postForObject(
                whisperProperties.getWhisperApiUrl(), requestEntity, java.util.Map.class);

        String text = response != null ? (String) response.get("text") : null;

        interviewRepository.findById(interviewId).ifPresent(interview -> {
            interview.setTranscriptionText(text);
            interview.setTranscriptionStatus(TranscriptionStatus.COMPLETED);
            interview.setUpdatedAt(java.time.LocalDateTime.now());
            interviewRepository.save(interview);
            LOG.info("Transcription completed for interview {}", interviewId);
        });

    } catch (Exception e) {
        LOG.error("Whisper transcription failed for interview {}: {}", interviewId, e.getMessage());
        interviewRepository.findById(interviewId).ifPresent(interview -> {
            interview.setTranscriptionStatus(TranscriptionStatus.FAILED);
            interview.setUpdatedAt(java.time.LocalDateTime.now());
            interviewRepository.save(interview);
        });
    }
}
```

Also add `download(String key)` to `StorageService` interface and implement it in `S3StorageService` and `LocalStorageService`.

`StorageService.java` — add:
```java
byte[] download(String key);
```

`S3StorageService.java` — add implementation using S3 client `getObject`.

`LocalStorageService.java` — add implementation using `Files.readAllBytes`.

Add a `@Bean RestTemplate` to any `@Configuration` class, e.g. create `backend/src/main/java/com/mindtrack/interview/config/RestTemplateConfig.java`:

```java
package com.mindtrack.interview.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

**Step 9: Run AudioService tests**

```bash
cd backend && mvn test -pl . -Dtest=AudioServiceTest
# Expected: all PASS (mock RestTemplate in test setup)
```

**Step 10: Run all tests**

```bash
cd backend && mvn test
# Expected: all PASS
```

**Step 11: Commit**

```bash
git add backend/src/main/java/com/mindtrack/interview/
git add backend/src/test/java/com/mindtrack/interview/
git commit -m "feat(audio): add async Whisper transcription with IN_PROGRESS/COMPLETED/FAILED status tracking"
```

---

## Task 9: Dashboard summary extensions

**Files:**
- Modify: `backend/src/main/java/com/mindtrack/analytics/dto/DashboardSummaryResponse.java`
- Modify: `backend/src/main/java/com/mindtrack/analytics/service/AnalyticsService.java`
- Modify: `backend/src/main/java/com/mindtrack/goals/repository/GoalRepository.java`
- Test: `backend/src/test/java/com/mindtrack/analytics/service/AnalyticsServiceTest.java`

**Step 1: Add fields to `DashboardSummaryResponse.java`** — after `activeGoals`:

```java
private long validatedGoals;
private long pendingValidationGoals;
```

Add getter/setter for each.

**Step 2: Add repository query to `GoalRepository.java`**

```java
long countByUserIdAndValidationStatus(Long userId, GoalValidationStatus validationStatus);
```

Add import: `import com.mindtrack.goals.model.GoalValidationStatus;`

**Step 3: Write failing test** — add to `AnalyticsServiceTest.java`:

```java
@Test
void shouldCountValidatedAndPendingGoals() {
    when(goalRepository.countByUserIdAndValidationStatus(1L, GoalValidationStatus.VALIDATED))
            .thenReturn(3L);
    when(goalRepository.countByUserIdAndValidationStatus(1L,
            GoalValidationStatus.PENDING_VALIDATION)).thenReturn(2L);
    // ... other mocks as in existing test
    when(journalEntryRepository.findByUserIdAndEntryDateBetweenOrderByEntryDateDesc(any(), any(), any()))
            .thenReturn(List.of());
    when(activityLogRepository.findByActivity_UserIdAndLogDateBetween(any(), any(), any()))
            .thenReturn(List.of());
    when(goalRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

    DashboardSummaryResponse summary = analyticsService.getDashboardSummary(
            1L, LocalDate.now().minusDays(7), LocalDate.now());

    assertEquals(3L, summary.getValidatedGoals());
    assertEquals(2L, summary.getPendingValidationGoals());
}
```

**Step 4: Update `AnalyticsService.getDashboardSummary()`** — after the `activeGoals` line add:

```java
summary.setValidatedGoals(
        goalRepository.countByUserIdAndValidationStatus(
                userId, GoalValidationStatus.VALIDATED));
summary.setPendingValidationGoals(
        goalRepository.countByUserIdAndValidationStatus(
                userId, GoalValidationStatus.PENDING_VALIDATION));
```

Add import: `import com.mindtrack.goals.model.GoalValidationStatus;`

**Step 5: Run analytics tests**

```bash
cd backend && mvn test -pl . -Dtest="AnalyticsServiceTest,AnalyticsControllerTest"
# Expected: all PASS
```

**Step 6: Run full test suite**

```bash
cd backend && mvn test
# Expected: all PASS
```

**Step 7: Commit**

```bash
git add backend/src/main/java/com/mindtrack/analytics/
git add backend/src/main/java/com/mindtrack/goals/repository/
git add backend/src/test/java/com/mindtrack/analytics/
git commit -m "feat(analytics): add validated/pending-validation goal counts to dashboard summary"
```

---

## Task 10: Frontend — onboarding route + guard

**Files:**
- Modify: `frontend/src/router/index.ts`
- Create: `frontend/src/views/OnboardingView.vue`
- Modify: `frontend/src/stores/profile.ts` (add `onboardingCompleted` field)

**Step 1: Add `onboardingCompleted` to profile store type**

In `frontend/src/stores/profile.ts`, find the profile interface/type and add:
```typescript
onboardingCompleted: boolean
tutorialCompleted: boolean
```

**Step 2: Add route to `router/index.ts`** — add before the closing `]` of the routes array:

```typescript
{
  path: '/onboarding',
  name: 'onboarding',
  component: () => import('@/views/OnboardingView.vue'),
  meta: { requiresAuth: true },
},
{
  path: '/invite/:token',
  name: 'invite',
  component: () => import('@/views/InviteView.vue'),
  meta: { requiresAuth: false },
},
```

**Step 3: Add onboarding guard to `beforeEach`** — after the `isTherapist` check:

```typescript
if (
  to.meta.requiresAuth &&
  auth.isAuthenticated &&
  to.name !== 'onboarding' &&
  auth.profile &&
  !auth.profile.onboardingCompleted
) {
  return { name: 'onboarding' }
}
```

> Note: The guard uses `auth.profile.onboardingCompleted`. Check how the profile is fetched in the auth store. If it's a separate profile store, adjust accordingly. Look at how `DashboardView.vue` calls `profileStore.fetchProfile()` — you may need to fetch the profile in the `beforeEach` or ensure it's loaded first. The simpler approach: only redirect if profile is already loaded and flag is false. If profile isn't loaded yet, let it through (dashboard will handle redirect after fetch).

**Step 4: Create `OnboardingView.vue`**

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const mode = ref<'choose' | 'survey' | 'done'>('choose')

// Survey fields
const moodBaseline = ref(5)
const anxietyLevel = ref(5)
const sleepQuality = ref(5)
const lifeAreas = ref<string[]>([])
const submitting = ref(false)
const error = ref<string | null>(null)

const LIFE_AREA_OPTIONS = ['Work', 'Relationships', 'Health', 'Fitness', 'Mindfulness', 'Hobbies']

function toggleArea(area: string) {
  const idx = lifeAreas.value.indexOf(area)
  if (idx >= 0) lifeAreas.value.splice(idx, 1)
  else lifeAreas.value.push(area)
}

async function submitSurvey() {
  submitting.value = true
  error.value = null
  try {
    await fetch('/api/onboarding/survey', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        moodBaseline: moodBaseline.value,
        anxietyLevel: anxietyLevel.value,
        sleepQuality: sleepQuality.value,
        lifeAreas: lifeAreas.value,
      }),
    })
    mode.value = 'done'
    setTimeout(() => router.push({ name: 'dashboard' }), 1500)
  } catch {
    error.value = 'Something went wrong. Please try again.'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="onboarding-view">
    <div class="onboarding-card">
      <!-- Choose path -->
      <div v-if="mode === 'choose'">
        <h1 class="onboarding-title">Welcome to MindTrack</h1>
        <p class="onboarding-subtitle">
          Let's set up your first goals. How would you like to start?
        </p>
        <div class="path-options">
          <button class="path-btn" @click="mode = 'survey'">
            <span class="path-icon">📋</span>
            <strong>Quick Survey</strong>
            <span class="path-desc">~3 minutes · Answer a few questions</span>
          </button>
        </div>
      </div>

      <!-- Survey -->
      <div v-else-if="mode === 'survey'">
        <h2 class="onboarding-title">Tell us about yourself</h2>
        <div v-if="error" class="error-msg">{{ error }}</div>

        <div class="field-group">
          <label class="field-label">Current mood (1–10): {{ moodBaseline }}</label>
          <input v-model.number="moodBaseline" type="range" min="1" max="10" class="slider" />
        </div>
        <div class="field-group">
          <label class="field-label">Anxiety level (1–10): {{ anxietyLevel }}</label>
          <input v-model.number="anxietyLevel" type="range" min="1" max="10" class="slider" />
        </div>
        <div class="field-group">
          <label class="field-label">Sleep quality (1–10): {{ sleepQuality }}</label>
          <input v-model.number="sleepQuality" type="range" min="1" max="10" class="slider" />
        </div>
        <div class="field-group">
          <label class="field-label">Life areas to improve (select all that apply)</label>
          <div class="chip-group">
            <button
              v-for="area in LIFE_AREA_OPTIONS"
              :key="area"
              class="chip"
              :class="{ active: lifeAreas.includes(area) }"
              @click="toggleArea(area)"
            >
              {{ area }}
            </button>
          </div>
        </div>

        <button class="submit-btn" :disabled="submitting" @click="submitSurvey">
          {{ submitting ? 'Setting up your goals...' : 'Create My Goals' }}
        </button>
      </div>

      <!-- Done -->
      <div v-else class="done-state">
        <span class="done-icon">✅</span>
        <p>Your goals are ready! Redirecting to your dashboard...</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.onboarding-view {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-gray-50, #f9fafb);
  padding: var(--space-4);
}
.onboarding-card {
  background: white;
  border-radius: 16px;
  padding: var(--space-8, 2rem);
  max-width: 520px;
  width: 100%;
  box-shadow: 0 4px 24px rgba(0,0,0,0.08);
}
.onboarding-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--color-gray-900);
  margin-bottom: var(--space-2);
}
.onboarding-subtitle {
  color: var(--color-gray-600);
  margin-bottom: var(--space-6, 1.5rem);
}
.path-options { display: flex; gap: var(--space-4); flex-wrap: wrap; }
.path-btn {
  flex: 1;
  min-width: 200px;
  border: 2px solid var(--color-gray-200);
  border-radius: 12px;
  padding: var(--space-5);
  background: white;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  text-align: left;
  transition: border-color 0.2s;
}
.path-btn:hover { border-color: var(--color-primary); }
.path-icon { font-size: 2rem; }
.path-desc { font-size: 0.875rem; color: var(--color-gray-500); }
.field-group { margin-bottom: var(--space-5); }
.field-label { display: block; font-weight: 500; color: var(--color-gray-700); margin-bottom: var(--space-2); }
.slider { width: 100%; }
.chip-group { display: flex; flex-wrap: wrap; gap: var(--space-2); margin-top: var(--space-2); }
.chip {
  padding: var(--space-1) var(--space-3);
  border-radius: 99px;
  border: 1px solid var(--color-gray-300);
  background: white;
  cursor: pointer;
  font-size: 0.875rem;
  transition: all 0.15s;
}
.chip.active { background: var(--color-primary); color: white; border-color: var(--color-primary); }
.submit-btn {
  width: 100%;
  padding: var(--space-3);
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  margin-top: var(--space-4);
}
.submit-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.error-msg { color: var(--color-error); background: #fef2f2; padding: var(--space-2) var(--space-3); border-radius: 8px; margin-bottom: var(--space-4); }
.done-state { text-align: center; padding: var(--space-8) 0; }
.done-icon { font-size: 3rem; display: block; margin-bottom: var(--space-3); }
</style>
```

**Step 5: Create `InviteView.vue`**

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const token = route.params.token as string
const preview = ref<{ initiatorName: string; initiatorRole: string } | null>(null)
const loading = ref(true)
const accepting = ref(false)
const error = ref<string | null>(null)

onMounted(async () => {
  try {
    const res = await fetch(`/api/invites/${token}`)
    if (!res.ok) throw new Error('Invalid or expired invite link')
    preview.value = await res.json()
  } catch (e: any) {
    error.value = e.message
  } finally {
    loading.value = false
  }
})

async function accept() {
  if (!auth.isAuthenticated) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  accepting.value = true
  try {
    const res = await fetch(`/api/invites/${token}/accept`, { method: 'POST' })
    if (!res.ok) throw new Error('Failed to accept invite')
    router.push({ name: 'dashboard' })
  } catch (e: any) {
    error.value = e.message
  } finally {
    accepting.value = false
  }
}
</script>

<template>
  <div class="invite-view">
    <div class="invite-card">
      <div v-if="loading" class="loading">Loading invite...</div>
      <div v-else-if="error" class="error-msg">{{ error }}</div>
      <div v-else-if="preview">
        <h1 class="invite-title">You've been invited</h1>
        <p class="invite-body">
          <strong>{{ preview.initiatorName }}</strong>
          ({{ preview.initiatorRole === 'THERAPIST' ? 'Therapist' : 'Patient' }})
          has invited you to connect on MindTrack.
        </p>
        <button class="accept-btn" :disabled="accepting" @click="accept">
          {{ accepting ? 'Accepting...' : 'Accept Invite' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.invite-view {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-gray-50, #f9fafb);
}
.invite-card {
  background: white;
  border-radius: 16px;
  padding: 2.5rem;
  max-width: 440px;
  width: 100%;
  box-shadow: 0 4px 24px rgba(0,0,0,0.08);
  text-align: center;
}
.invite-title { font-size: 1.5rem; font-weight: 700; margin-bottom: 1rem; }
.invite-body { color: var(--color-gray-600); margin-bottom: 1.5rem; line-height: 1.6; }
.accept-btn {
  padding: 0.75rem 2rem;
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
}
.accept-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.error-msg { color: var(--color-error); }
.loading { color: var(--color-gray-500); }
</style>
```

**Step 6: Run frontend lint and tests**

```bash
cd frontend && npm run lint && npm run test:unit
# Expected: PASS (new views don't have unit tests yet — that's fine)
```

**Step 7: Commit**

```bash
git add frontend/src/router/index.ts
git add frontend/src/views/OnboardingView.vue
git add frontend/src/views/InviteView.vue
git commit -m "feat(frontend): add /onboarding and /invite/:token routes with onboarding guard"
```

---

## Task 11: Frontend — AudioSection.vue recording mode

**Files:**
- Modify: `frontend/src/components/interview/AudioSection.vue`
- Modify: `frontend/src/stores/interviews.ts` (ensure `transcriptionStatus` is in `AudioResponse`)

**Step 1: Add `transcriptionStatus` to the `AudioResponse` type in `interviews.ts`**

Find the `AudioResponse` interface/type and add:
```typescript
transcriptionStatus: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED' | null
```

**Step 2: Replace `AudioSection.vue` script section** — the template stays mostly the same; we add the record button and recording states. Replace the `<script setup>` block with:

```typescript
<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useInterviewsStore, type AudioResponse } from '@/stores/interviews'

const props = defineProps<{
  interviewId: number
  hasAudio: boolean
}>()
const emit = defineEmits<{ audioChanged: [] }>()

const store = useInterviewsStore()
const audioData = ref<AudioResponse | null>(null)
const uploading = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)
const uploadError = ref<string | null>(null)

// Recording state
type RecordState = 'idle' | 'recording' | 'uploading' | 'transcribing' | 'done' | 'failed'
const recordState = ref<RecordState>('idle')
const recordDuration = ref(0)
let mediaRecorder: MediaRecorder | null = null
let durationInterval: ReturnType<typeof setInterval> | null = null
let pollCount = 0

const ALLOWED_FORMATS = ['audio/mpeg','audio/wav','audio/x-m4a','audio/flac','audio/ogg','audio/webm']
const MAX_SIZE_MB = 50
const MAX_SIZE_BYTES = MAX_SIZE_MB * 1024 * 1024

onMounted(async () => {
  if (props.hasAudio) await loadAudio()
})

onUnmounted(() => {
  if (durationInterval) clearInterval(durationInterval)
  mediaRecorder?.stop()
})

async function loadAudio() {
  try {
    audioData.value = await store.getAudioUrl(props.interviewId)
    if (audioData.value?.transcriptionStatus === 'IN_PROGRESS') {
      scheduleTranscriptionPoll()
    }
  } catch { /* error in store */ }
}

async function startRecording() {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    const chunks: BlobPart[] = []
    mediaRecorder = new MediaRecorder(stream, { mimeType: 'audio/webm' })
    mediaRecorder.ondataavailable = (e) => chunks.push(e.data)
    mediaRecorder.onstop = async () => {
      stream.getTracks().forEach(t => t.stop())
      const blob = new Blob(chunks, { type: 'audio/webm' })
      await uploadBlob(blob)
    }
    mediaRecorder.start()
    recordState.value = 'recording'
    recordDuration.value = 0
    durationInterval = setInterval(() => recordDuration.value++, 1000)
  } catch {
    uploadError.value = 'Microphone access denied.'
  }
}

function stopRecording() {
  if (durationInterval) { clearInterval(durationInterval); durationInterval = null }
  mediaRecorder?.stop()
  recordState.value = 'uploading'
}

async function uploadBlob(blob: Blob) {
  const file = new File([blob], 'recording.webm', { type: 'audio/webm' })
  try {
    audioData.value = await store.uploadAudio(props.interviewId, file)
    emit('audioChanged')
    recordState.value = 'transcribing'
    pollCount = 0
    setTimeout(() => pollTranscription(), 20000)
  } catch {
    uploadError.value = 'Upload failed. Please try again.'
    recordState.value = 'failed'
  }
}

async function pollTranscription() {
  try {
    const data = await store.getAudioUrl(props.interviewId)
    audioData.value = data
    if (data?.transcriptionStatus === 'COMPLETED' || data?.transcriptionStatus === 'FAILED') {
      recordState.value = data.transcriptionStatus === 'COMPLETED' ? 'done' : 'failed'
      return
    }
  } catch { /* ignore */ }
  pollCount++
  if (pollCount < 4) {
    setTimeout(() => pollTranscription(), 10000)
  } else {
    uploadError.value = 'Transcription taking longer than expected — refresh later.'
    recordState.value = 'done'
  }
}

function scheduleTranscriptionPoll() {
  pollCount = 0
  setTimeout(() => pollTranscription(), 10000)
}

function triggerUpload() { fileInput.value?.click() }

async function handleFileSelect(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return
  uploadError.value = null
  if (!ALLOWED_FORMATS.includes(file.type)) {
    uploadError.value = 'Unsupported format. Use MP3, WAV, M4A, FLAC, OGG, or WebM.'
    return
  }
  if (file.size > MAX_SIZE_BYTES) {
    uploadError.value = `File too large. Maximum size is ${MAX_SIZE_MB} MB.`
    return
  }
  uploading.value = true
  try {
    audioData.value = await store.uploadAudio(props.interviewId, file)
    emit('audioChanged')
    scheduleTranscriptionPoll()
  } catch {
    uploadError.value = 'Upload failed. Please try again.'
  } finally {
    uploading.value = false
    if (fileInput.value) fileInput.value.value = ''
  }
}

async function handleDelete() {
  try {
    await store.deleteAudio(props.interviewId)
    audioData.value = null
    recordState.value = 'idle'
    emit('audioChanged')
  } catch { /* error in store */ }
}

function formatDuration(seconds: number): string {
  const m = Math.floor(seconds / 60).toString().padStart(2, '0')
  const s = (seconds % 60).toString().padStart(2, '0')
  return `${m}:${s}`
}
</script>
```

**Step 3: Update the template** — replace the upload section `<div v-if="!audioData">` block:

```html
<!-- No audio yet: record or upload -->
<div v-if="!audioData && recordState === 'idle'" class="audio-upload">
  <input
    ref="fileInput"
    type="file"
    accept=".mp3,.wav,.m4a,.flac,.ogg,.webm"
    class="file-input-hidden"
    @change="handleFileSelect"
  />
  <button class="btn btn-record" @click="startRecording">🎙 Start Recording</button>
  <div class="upload-area" @click="triggerUpload">
    <span class="upload-icon">📁</span>
    <p class="upload-text">Or click to upload a file</p>
    <p class="upload-hint">MP3, WAV, M4A, FLAC, OGG, WebM — max {{ MAX_SIZE_MB }} MB</p>
  </div>
</div>

<!-- Recording in progress -->
<div v-if="recordState === 'recording'" class="record-active">
  <span class="record-dot" />
  <span class="record-timer">{{ formatDuration(recordDuration) }}</span>
  <button class="btn btn-stop" @click="stopRecording">⏹ Stop</button>
</div>

<!-- Uploading / transcribing -->
<div v-if="recordState === 'uploading' || recordState === 'transcribing'" class="status-msg">
  <span v-if="recordState === 'uploading'">Uploading...</span>
  <span v-else>Transcribing… this may take up to a minute.</span>
</div>

<!-- Failed -->
<div v-if="recordState === 'failed'" class="status-msg error">
  Transcription failed. The audio was saved — you can refresh to check again.
</div>
```

Add these CSS classes to the `<style scoped>` block:

```css
.btn-record {
  display: block;
  width: 100%;
  margin-bottom: var(--space-3);
  padding: var(--space-3);
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: var(--border-radius);
  font-weight: 600;
  cursor: pointer;
}
.record-active {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
}
.record-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: red;
  animation: pulse 1s infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}
.record-timer {
  font-size: 1.25rem;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}
.btn-stop {
  margin-left: auto;
  padding: var(--space-2) var(--space-4);
  background: #fee2e2;
  color: var(--color-error);
  border: none;
  border-radius: var(--border-radius);
  font-weight: 600;
  cursor: pointer;
}
.status-msg {
  padding: var(--space-4);
  text-align: center;
  color: var(--color-gray-600);
}
.status-msg.error { color: var(--color-error); }
```

**Step 4: Run frontend lint**

```bash
cd frontend && npm run lint
# Expected: PASS (fix any lint errors)
```

**Step 5: Commit**

```bash
git add frontend/src/components/interview/AudioSection.vue
git add frontend/src/stores/interviews.ts
git commit -m "feat(audio): add in-browser MediaRecorder recording to AudioSection with transcription status polling"
```

---

## Task 12: Frontend — Dashboard validation chips + therapist banner

**Files:**
- Modify: `frontend/src/views/DashboardView.vue`
- Modify: `frontend/src/views/GoalsView.vue` (or GoalDetailView.vue — add validation chip)

**Step 1: Add validation summary cards to `DashboardView.vue`**

In the template, after the existing summary cards, add:

```html
<div v-if="store.summary" class="validation-cards">
  <div class="summary-card">
    <span class="card-icon">✅</span>
    <span class="card-count">{{ store.summary.validatedGoals }}</span>
    <span class="card-label">Validated Goals</span>
  </div>
  <div class="summary-card">
    <span class="card-icon">⬜</span>
    <span class="card-count">{{ store.summary.pendingValidationGoals }}</span>
    <span class="card-label">Pending Review</span>
  </div>
</div>
```

**Step 2: Add CSS for validation cards** (in `<style scoped>`):

```css
.validation-cards {
  display: flex;
  gap: var(--space-4);
  margin-top: var(--space-4);
  flex-wrap: wrap;
}
.summary-card {
  flex: 1;
  min-width: 140px;
  background: white;
  border: 1px solid var(--color-gray-200);
  border-radius: 12px;
  padding: var(--space-4);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-1);
}
.card-icon { font-size: 1.5rem; }
.card-count { font-size: 2rem; font-weight: 700; color: var(--color-gray-900); }
.card-label { font-size: 0.75rem; color: var(--color-gray-500); text-align: center; }
```

**Step 3: Add validation chip to goals list**

In `GoalsView.vue` (or wherever goals are listed), find the goal item template and add after the status badge:

```html
<span
  v-if="goal.validationStatus"
  class="validation-chip"
  :class="`validation-chip--${goal.validationStatus.toLowerCase()}`"
>
  {{ validationLabel(goal.validationStatus) }}
</span>
```

In the `<script setup>`:

```typescript
function validationLabel(status: string): string {
  const labels: Record<string, string> = {
    PENDING_VALIDATION: '⬜ Awaiting review',
    VALIDATED: '✅ Validated',
    OVERRIDDEN: '✏️ Modified',
    REJECTED: '❌ Not approved',
  }
  return labels[status] ?? status
}
```

**Step 4: Add CSS for chips** (in `<style scoped>`):

```css
.validation-chip {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 99px;
  font-size: 0.7rem;
  font-weight: 500;
}
.validation-chip--pending_validation { background: #f3f4f6; color: #6b7280; }
.validation-chip--validated { background: #d1fae5; color: #065f46; }
.validation-chip--overridden { background: #dbeafe; color: #1e40af; }
.validation-chip--rejected { background: #fee2e2; color: #991b1b; }
```

**Step 5: Run lint**

```bash
cd frontend && npm run lint
# Expected: PASS
```

**Step 6: Commit**

```bash
git add frontend/src/views/DashboardView.vue
git add frontend/src/views/GoalsView.vue
git commit -m "feat(frontend): add validation summary cards to dashboard and goal validation chips to goals list"
```

---

## Task 13: Final integration — run all tests and verify

**Step 1: Run backend full test suite**

```bash
cd backend && mvn verify
# Expected: all tests PASS, no Checkstyle errors
```

Fix any Checkstyle issues (120-char line limit, Google-style formatting). Common fixes:
- Long method chains → break onto new lines
- Imports in wrong order → let IDE organize (`Ctrl+Alt+O` in IntelliJ)
- Missing blank line between sections

**Step 2: Run frontend lint and tests**

```bash
cd frontend && npm run lint && npm run test:unit
# Expected: PASS
```

**Step 3: Start the app locally and manually test**

```bash
# Terminal 1
cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=local

# Terminal 2
cd frontend && npm run dev
```

Manual checklist:
- [ ] New user → redirected to `/onboarding`
- [ ] Survey submitted → redirected to dashboard, goals appear with `⬜ Awaiting review`
- [ ] `/invite/:token` page shows initiator name
- [ ] Therapist portal shows pending queue
- [ ] Interview detail → "Start Recording" button appears, records, uploads
- [ ] Dashboard shows validated/pending counts

**Step 4: Final commit if any fixes made**

```bash
cd backend && git add -p && git commit -m "fix: address integration issues found during manual testing"
```

---

## Summary of migrations needed

| Version | Description |
|---------|-------------|
| V5 | Goal validation fields (`validation_status`, `validated_by`, `validated_at`, `created_by`) |
| V6 | Add `PENDING` to `therapist_patients.status` enum |
| V7 | Add `transcription_status` to `interviews` |
| V8 | Add `onboarding_completed` to `user_profiles` |
| V9 | Create `invite_tokens` table |

## Key things to know before coding

1. **Java style**: All classes use plain getters/setters — no Lombok. Follow the pattern in `Goal.java` exactly.
2. **Test style**: Unit tests = `@ExtendWith(MockitoExtension.class)` + `@Mock` + manually instantiate the class under test. Controller tests = `@SpringBootTest` + `@AutoConfigureMockMvc` + `@ActiveProfiles("local")` + `@MockitoBean`.
3. **No Flyway in local dev**: The H2 profile uses `ddl-auto=create-drop` — it picks up entity fields automatically. Migrations only run against MySQL (Docker or prod).
4. **`@Async` requires `@EnableAsync`** on the main application class. Check `MindTrackApplication.java` before adding.
5. **Security**: All `/api/therapist/**` endpoints require `ROLE_THERAPIST`. `/api/invites/{token}` (preview) is public. `/api/invites/generate` and `/api/invites/{token}/accept` require any authenticated user. Add these to the Spring Security config if needed.
6. **`TherapistPatient` default**: The entity constructor defaults to `ACTIVE`. The invite service sets the correct status explicitly when creating the relationship.
7. **Frontend API calls**: Use `fetch('/api/...')` — the Vite dev server proxies `/api` to `localhost:8080`.
