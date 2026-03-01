# Therapist Role Assignment Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Allow users to designate themselves as therapists (or switch back) during onboarding, from their profile, and keep the existing admin panel working — all with immediate JWT refresh so the role takes effect without re-login.

**Architecture:** New `PATCH /api/auth/me/role` endpoint returns a fresh JWT. `POST /api/onboarding/skip` marks onboarding complete without the survey. `survey_completed` column on `user_profiles` drives a dashboard prompt until the survey is done. Frontend stores handle token refresh and profile state.

**Tech Stack:** Java 21 · Spring Boot 3.5.3 · JPA · JJWT · Vue 3 Composition API · Pinia · Vitest · MockMvc · JUnit 5 · Mockito

---

### Task 1: V10 migration + `survey_completed` in entity, DTO, and mapper

Also fixes a pre-existing gap: `ProfileResponse` and `ProfileMapper` are missing `onboardingCompleted`, which would cause the onboarding router guard to misbehave.

**Files:**
- Create: `backend/src/main/resources/db/migration/V10__add_survey_completed.sql`
- Modify: `backend/src/main/java/com/mindtrack/profile/model/UserProfile.java`
- Modify: `backend/src/main/java/com/mindtrack/profile/dto/ProfileResponse.java`
- Modify: `backend/src/main/java/com/mindtrack/profile/service/ProfileMapper.java`
- Test: `backend/src/test/java/com/mindtrack/profile/service/ProfileMapperTest.java` (create)

**Step 1: Write the failing test**

Create `backend/src/test/java/com/mindtrack/profile/service/ProfileMapperTest.java`:

```java
package com.mindtrack.profile.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindtrack.profile.dto.ProfileResponse;
import com.mindtrack.profile.model.UserProfile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfileMapperTest {

    private final ProfileMapper mapper = new ProfileMapper(new ObjectMapper());

    @Test
    void shouldMapSurveyCompletedToResponse() {
        UserProfile profile = new UserProfile();
        profile.setUserId(1L);
        profile.setSurveyCompleted(true);
        profile.setOnboardingCompleted(true);

        ProfileResponse response = mapper.toResponse(profile);

        assertTrue(response.isSurveyCompleted());
        assertTrue(response.isOnboardingCompleted());
    }

    @Test
    void shouldDefaultSurveyCompletedToFalse() {
        UserProfile profile = new UserProfile();
        profile.setUserId(1L);

        ProfileResponse response = mapper.toResponse(profile);

        assertFalse(response.isSurveyCompleted());
        assertFalse(response.isOnboardingCompleted());
    }
}
```

**Step 2: Run test to verify it fails**

```bash
cd backend && mvn test -pl . -Dtest=ProfileMapperTest -q 2>&1 | tail -20
```
Expected: FAIL — `setSurveyCompleted` method not found on `UserProfile`.

**Step 3: Create the migration**

Create `backend/src/main/resources/db/migration/V10__add_survey_completed.sql`:

```sql
ALTER TABLE user_profiles
    ADD COLUMN survey_completed BOOLEAN NOT NULL DEFAULT FALSE;
```

**Step 4: Add `surveyCompleted` and fix `onboardingCompleted` in `UserProfile.java`**

In `UserProfile.java`, add after the `onboardingCompleted` field (line ~46):

```java
    @Column(name = "survey_completed", nullable = false)
    private boolean surveyCompleted;
```

Add getter/setter after the existing `onboardingCompleted` getter/setter (after line ~128):

```java
    public boolean isSurveyCompleted() {
        return surveyCompleted;
    }

    public void setSurveyCompleted(boolean surveyCompleted) {
        this.surveyCompleted = surveyCompleted;
    }
```

**Step 5: Add `surveyCompleted` and `onboardingCompleted` to `ProfileResponse.java`**

In `ProfileResponse.java`, add two fields after `tutorialCompleted` (after line ~19):

```java
    private boolean onboardingCompleted;
    private boolean surveyCompleted;
```

Add getters/setters after the `tutorialCompleted` getter/setter (after line ~106):

```java
    public boolean isOnboardingCompleted() {
        return onboardingCompleted;
    }

    public void setOnboardingCompleted(boolean onboardingCompleted) {
        this.onboardingCompleted = onboardingCompleted;
    }

    public boolean isSurveyCompleted() {
        return surveyCompleted;
    }

    public void setSurveyCompleted(boolean surveyCompleted) {
        this.surveyCompleted = surveyCompleted;
    }
```

**Step 6: Update `ProfileMapper.toResponse()` to map both fields**

In `ProfileMapper.java`, in the `toResponse` method after `response.setTutorialCompleted(...)` (line ~44):

```java
        response.setOnboardingCompleted(profile.isOnboardingCompleted());
        response.setSurveyCompleted(profile.isSurveyCompleted());
```

**Step 7: Run tests to verify they pass**

```bash
cd backend && mvn test -pl . -Dtest=ProfileMapperTest -q 2>&1 | tail -10
```
Expected: BUILD SUCCESS, 2 tests passing.

**Step 8: Run full backend suite**

```bash
cd backend && mvn verify -q 2>&1 | tail -20
```
Expected: BUILD SUCCESS.

**Step 9: Commit**

```bash
git add backend/src/main/resources/db/migration/V10__add_survey_completed.sql \
        backend/src/main/java/com/mindtrack/profile/model/UserProfile.java \
        backend/src/main/java/com/mindtrack/profile/dto/ProfileResponse.java \
        backend/src/main/java/com/mindtrack/profile/service/ProfileMapper.java \
        backend/src/test/java/com/mindtrack/profile/service/ProfileMapperTest.java
git commit -m "#15 feat(profile): add survey_completed field and fix onboardingCompleted mapping"
```

---

### Task 2: `ProfileService.completeSurvey()` + `skipOnboarding()`, update `OnboardingService`

**Files:**
- Modify: `backend/src/main/java/com/mindtrack/profile/service/ProfileService.java`
- Modify: `backend/src/main/java/com/mindtrack/onboarding/service/OnboardingService.java`
- Modify: `backend/src/test/java/com/mindtrack/onboarding/service/OnboardingServiceTest.java`

**Step 1: Write failing test for `completeSurvey`**

In `OnboardingServiceTest.java`, update the existing `shouldCreateGoalsFromSurveyWithPendingValidation` test to verify `completeSurvey` instead of `completeOnboarding`:

Replace:
```java
        verify(profileService).completeOnboarding(1L);
```
With:
```java
        verify(profileService).completeSurvey(1L);
```

**Step 2: Run test to verify it fails**

```bash
cd backend && mvn test -pl . -Dtest=OnboardingServiceTest -q 2>&1 | tail -20
```
Expected: FAIL — `completeSurvey` method not found on `ProfileService`.

**Step 3: Add `completeSurvey()` and `skipOnboarding()` to `ProfileService.java`**

In `ProfileService.java`, add after `completeOnboarding()` (after line ~61):

```java
    /**
     * Marks both the survey and onboarding as completed for the given user.
     */
    @Transactional
    public void completeSurvey(Long userId) {
        LOG.info("Marking survey and onboarding complete for user {}", userId);
        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultProfile(userId));
        profile.setOnboardingCompleted(true);
        profile.setSurveyCompleted(true);
        profileRepository.save(profile);
    }

    /**
     * Marks onboarding as skipped (complete without survey) for the given user.
     */
    @Transactional
    public void skipOnboarding(Long userId) {
        LOG.info("Marking onboarding skipped for user {}", userId);
        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultProfile(userId));
        profile.setOnboardingCompleted(true);
        profileRepository.save(profile);
    }
```

**Step 4: Update `OnboardingService.generateGoalsFromSurvey()` to call `completeSurvey`**

In `OnboardingService.java`, replace:
```java
        profileService.completeOnboarding(userId);
```
With:
```java
        profileService.completeSurvey(userId);
```

**Step 5: Run tests to verify they pass**

```bash
cd backend && mvn test -pl . -Dtest=OnboardingServiceTest -q 2>&1 | tail -10
```
Expected: BUILD SUCCESS, 2 tests passing.

**Step 6: Run full backend suite**

```bash
cd backend && mvn verify -q 2>&1 | tail -10
```
Expected: BUILD SUCCESS.

**Step 7: Commit**

```bash
git add backend/src/main/java/com/mindtrack/profile/service/ProfileService.java \
        backend/src/main/java/com/mindtrack/onboarding/service/OnboardingService.java \
        backend/src/test/java/com/mindtrack/onboarding/service/OnboardingServiceTest.java
git commit -m "#15 feat(onboarding): add completeSurvey and skipOnboarding to ProfileService"
```

---

### Task 3: `POST /api/onboarding/skip` endpoint

**Files:**
- Modify: `backend/src/main/java/com/mindtrack/onboarding/controller/OnboardingController.java`
- Create: `backend/src/test/java/com/mindtrack/onboarding/controller/OnboardingControllerTest.java`

**Step 1: Write the failing test**

Create `backend/src/test/java/com/mindtrack/onboarding/controller/OnboardingControllerTest.java`:

```java
package com.mindtrack.onboarding.controller;

import com.mindtrack.onboarding.service.OnboardingService;
import com.mindtrack.profile.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class OnboardingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private OnboardingService onboardingService;

    private static UsernamePasswordAuthenticationToken mockAuth(Long userId) {
        return new UsernamePasswordAuthenticationToken(
                userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void shouldSkipOnboardingAndReturn200() throws Exception {
        mockMvc.perform(post("/api/onboarding/skip")
                        .with(authentication(mockAuth(1L))))
                .andExpect(status().isOk());

        verify(profileService).skipOnboarding(1L);
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/onboarding/skip"))
                .andExpect(status().isUnauthorized());
    }
}
```

**Step 2: Run test to verify it fails**

```bash
cd backend && mvn test -pl . -Dtest=OnboardingControllerTest -q 2>&1 | tail -20
```
Expected: FAIL — 404 for `/api/onboarding/skip`.

**Step 3: Add `ProfileService` injection and `skip` endpoint to `OnboardingController.java`**

In `OnboardingController.java`, add `ProfileService` import and field, and the new endpoint:

Replace the class from line 1:

```java
package com.mindtrack.onboarding.controller;

import com.mindtrack.goals.dto.GoalResponse;
import com.mindtrack.onboarding.dto.SurveyRequest;
import com.mindtrack.onboarding.service.OnboardingService;
import com.mindtrack.profile.service.ProfileService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the onboarding survey flow.
 */
@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;
    private final ProfileService profileService;

    public OnboardingController(OnboardingService onboardingService,
                                ProfileService profileService) {
        this.onboardingService = onboardingService;
        this.profileService = profileService;
    }

    /**
     * Submits the onboarding survey and returns generated goal suggestions.
     */
    @PostMapping("/survey")
    public ResponseEntity<List<GoalResponse>> survey(
            @RequestBody @Valid SurveyRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(onboardingService.generateGoalsFromSurvey(userId, request));
    }

    /**
     * Skips the onboarding survey, marking onboarding complete without generating goals.
     */
    @PostMapping("/skip")
    public ResponseEntity<Void> skip(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        profileService.skipOnboarding(userId);
        return ResponseEntity.ok().build();
    }
}
```

**Step 4: Run tests to verify they pass**

```bash
cd backend && mvn test -pl . -Dtest=OnboardingControllerTest -q 2>&1 | tail -10
```
Expected: BUILD SUCCESS, 2 tests passing.

**Step 5: Run full backend suite**

```bash
cd backend && mvn verify -q 2>&1 | tail -10
```
Expected: BUILD SUCCESS.

**Step 6: Commit**

```bash
git add backend/src/main/java/com/mindtrack/onboarding/controller/OnboardingController.java \
        backend/src/test/java/com/mindtrack/onboarding/controller/OnboardingControllerTest.java
git commit -m "#15 feat(onboarding): add POST /api/onboarding/skip endpoint"
```

---

### Task 4: `UserService.changeRole()` + `PATCH /api/auth/me/role` in `AuthController`

**Files:**
- Modify: `backend/src/main/java/com/mindtrack/auth/service/UserService.java`
- Create: `backend/src/main/java/com/mindtrack/auth/dto/SelfRoleRequest.java`
- Modify: `backend/src/main/java/com/mindtrack/auth/controller/AuthController.java`
- Modify: `backend/src/test/java/com/mindtrack/auth/controller/AuthControllerTest.java`

**Step 1: Write the failing test**

In `AuthControllerTest.java`, add after the existing tests (before the closing `}`):

```java
    @Test
    void shouldChangeRoleToTherapistAndReturnNewToken() throws Exception {
        User user = createUser(1L, "test@example.com", "Test User", "THERAPIST");
        when(userService.changeRole(1L, "THERAPIST")).thenReturn(user);

        mockMvc.perform(patch("/api/auth/me/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"THERAPIST\"}")
                        .with(authentication(mockAuth(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("THERAPIST"));
    }

    @Test
    void shouldRejectSelfAssignAdminRole() throws Exception {
        mockMvc.perform(patch("/api/auth/me/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}")
                        .with(authentication(mockAuth(1L))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn401OnRoleChangeWhenNotAuthenticated() throws Exception {
        mockMvc.perform(patch("/api/auth/me/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"THERAPIST\"}"))
                .andExpect(status().isUnauthorized());
    }
```

Also add the missing import at the top of `AuthControllerTest.java`:

```java
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
```

**Step 2: Run test to verify it fails**

```bash
cd backend && mvn test -pl . -Dtest=AuthControllerTest -q 2>&1 | tail -20
```
Expected: FAIL — 404 for `PATCH /api/auth/me/role` and `changeRole` method not found.

**Step 3: Create `SelfRoleRequest.java`**

Create `backend/src/main/java/com/mindtrack/auth/dto/SelfRoleRequest.java`:

```java
package com.mindtrack.auth.dto;

import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for self-service role change. Only USER and THERAPIST are allowed.
 */
public class SelfRoleRequest {

    @Pattern(regexp = "USER|THERAPIST", message = "Role must be USER or THERAPIST")
    private String role;

    public SelfRoleRequest() {
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
```

**Step 4: Add `changeRole()` to `UserService.java`**

In `UserService.java`, add after `findByEmail()` (after line ~83):

```java
    /**
     * Changes the role of a user. Only USER and THERAPIST are permitted.
     */
    @Transactional
    public User changeRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
        user.setRole(role);
        return userRepository.save(user);
    }
```

**Step 5: Add `JwtService` to `AuthController` and add the new endpoint**

Replace `AuthController.java` entirely:

```java
package com.mindtrack.auth.controller;

import com.mindtrack.auth.dto.AuthResponse;
import com.mindtrack.auth.dto.SelfRoleRequest;
import com.mindtrack.auth.dto.UserInfo;
import com.mindtrack.auth.service.JwtService;
import com.mindtrack.auth.service.UserService;
import com.mindtrack.common.model.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication controller for user info, token validation, and self-service role change.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    /**
     * Returns the current authenticated user's information.
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfo> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        Long userId = (Long) authentication.getPrincipal();
        return userService.findById(userId)
                .map(user -> ResponseEntity.ok(toUserInfo(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Changes the current user's role to USER or THERAPIST and returns a refreshed JWT.
     */
    @PatchMapping("/me/role")
    public ResponseEntity<AuthResponse> changeRole(
            @Valid @RequestBody SelfRoleRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        User user = userService.changeRole(userId, request.getRole());
        String token = jwtService.generateToken(user.getId(), user.getEmail(),
                user.getRole().getName());
        return ResponseEntity.ok(
                new AuthResponse(token, user.getEmail(), user.getName(), user.getRole().getName()));
    }

    private UserInfo toUserInfo(User user) {
        return new UserInfo(user.getId(), user.getEmail(), user.getName(), user.getRole().getName());
    }
}
```

Note: `AuthControllerTest` mocks `UserService` — also add `JwtService` mock to the test. In `AuthControllerTest.java`, add:

```java
    @MockitoBean
    private JwtService jwtService;
```

And add `when(jwtService.generateToken(anyLong(), anyString(), anyString())).thenReturn("new-token");` to the `shouldChangeRoleToTherapistAndReturnNewToken` test setup. Also add the imports:

```java
import com.mindtrack.auth.service.JwtService;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
```

The updated `shouldChangeRoleToTherapistAndReturnNewToken` test:

```java
    @Test
    void shouldChangeRoleToTherapistAndReturnNewToken() throws Exception {
        User user = createUser(1L, "test@example.com", "Test User", "THERAPIST");
        when(userService.changeRole(1L, "THERAPIST")).thenReturn(user);
        when(jwtService.generateToken(anyLong(), anyString(), anyString())).thenReturn("new-token");

        mockMvc.perform(patch("/api/auth/me/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"THERAPIST\"}")
                        .with(authentication(mockAuth(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-token"))
                .andExpect(jsonPath("$.role").value("THERAPIST"));
    }
```

**Step 6: Run tests to verify they pass**

```bash
cd backend && mvn test -pl . -Dtest=AuthControllerTest -q 2>&1 | tail -10
```
Expected: BUILD SUCCESS, 5 tests passing (3 existing + 3 new).

**Step 7: Run full backend suite**

```bash
cd backend && mvn verify -q 2>&1 | tail -10
```
Expected: BUILD SUCCESS.

**Step 8: Commit**

```bash
git add backend/src/main/java/com/mindtrack/auth/dto/SelfRoleRequest.java \
        backend/src/main/java/com/mindtrack/auth/service/UserService.java \
        backend/src/main/java/com/mindtrack/auth/controller/AuthController.java \
        backend/src/test/java/com/mindtrack/auth/controller/AuthControllerTest.java
git commit -m "#15 feat(auth): add PATCH /api/auth/me/role for self-service role change"
```

---

### Task 5: Frontend auth store — `updateToken()`

**Files:**
- Modify: `frontend/src/stores/auth.ts`
- Modify: `frontend/src/stores/__tests__/auth.test.ts`

**Step 1: Write the failing test**

In `auth.test.ts`, add a new `describe('updateToken')` block before the closing `})`:

```typescript
  describe('updateToken', () => {
    it('replaces token, persists to localStorage, and re-fetches user', async () => {
      const store = useAuthStore()
      store.setToken('old-token')

      const module = await import('@/services/api')
      const api = module.default as unknown as { get: ReturnType<typeof vi.fn> }
      const userData = { id: '1', email: 'test@test.com', name: 'Test', role: 'THERAPIST' }
      api.get.mockResolvedValueOnce({ data: userData })

      await store.updateToken('new-token')

      expect(store.token).toBe('new-token')
      expect(localStorageMock.setItem).toHaveBeenCalledWith('mindtrack_token', 'new-token')
      expect(store.user?.role).toBe('THERAPIST')
    })
  })
```

**Step 2: Run test to verify it fails**

```bash
cd frontend && npm run test:unit -- --reporter=verbose 2>&1 | grep -A5 "updateToken"
```
Expected: FAIL — `store.updateToken is not a function`.

**Step 3: Add `updateToken()` to `auth.ts`**

In `auth.ts`, add after `fetchCurrentUser` (before the `return` block):

```typescript
  async function updateToken(newToken: string) {
    setToken(newToken)
    await fetchCurrentUser()
  }
```

Add `updateToken` to the return object:

```typescript
  return {
    token,
    user,
    isAuthenticated,
    isAdmin,
    isTherapist,
    setToken,
    setUser,
    logout,
    fetchCurrentUser,
    updateToken,
  }
```

**Step 4: Run tests to verify they pass**

```bash
cd frontend && npm run test:unit -- --reporter=verbose 2>&1 | grep -E "(PASS|FAIL|updateToken)"
```
Expected: PASS.

**Step 5: Commit**

```bash
git add frontend/src/stores/auth.ts frontend/src/stores/__tests__/auth.test.ts
git commit -m "#15 feat(auth-store): add updateToken for immediate role reflection"
```

---

### Task 6: Frontend profile store — `surveyCompleted` + survey/skip actions

**Files:**
- Modify: `frontend/src/stores/profile.ts`

**Step 1: Add `surveyCompleted` to `UserProfile` interface**

In `profile.ts`, add `surveyCompleted: boolean` to `UserProfile` after `onboardingCompleted`:

```typescript
export interface UserProfile {
  id: number
  userId: number
  displayName: string | null
  avatarUrl: string | null
  timezone: string | null
  notificationPrefs: NotificationPrefs | null
  telegramChatId: string | null
  whatsappNumber: string | null
  tutorialCompleted: boolean
  onboardingCompleted: boolean
  surveyCompleted: boolean
}
```

**Step 2: Add `submitSurvey()` and `skipSurvey()` actions**

In `profile.ts`, add two new actions after `updateProfile` (before `clearError`):

```typescript
  async function submitSurvey(data: {
    moodBaseline: number
    anxietyLevel: number
    sleepQuality: number
    lifeAreas: string[]
  }) {
    saving.value = true
    error.value = null
    try {
      await api.post('/onboarding/survey', data)
      if (profile.value) {
        profile.value.surveyCompleted = true
        profile.value.onboardingCompleted = true
      }
    } catch (err) {
      error.value = 'Failed to submit survey'
      throw err
    } finally {
      saving.value = false
    }
  }

  async function skipSurvey() {
    saving.value = true
    error.value = null
    try {
      await api.post('/onboarding/skip')
      if (profile.value) {
        profile.value.onboardingCompleted = true
      }
    } catch (err) {
      error.value = 'Failed to skip survey'
      throw err
    } finally {
      saving.value = false
    }
  }
```

Add both to the return object:

```typescript
  return {
    profile,
    loading,
    saving,
    error,
    fetchProfile,
    updateProfile,
    submitSurvey,
    skipSurvey,
    clearError,
  }
```

**Step 3: Run frontend tests**

```bash
cd frontend && npm run test:unit 2>&1 | tail -10
```
Expected: all tests still passing (no new tests needed — store actions are tested via component tests later).

**Step 4: Commit**

```bash
git add frontend/src/stores/profile.ts
git commit -m "#15 feat(profile-store): add surveyCompleted field, submitSurvey, skipSurvey"
```

---

### Task 7: `OnboardingView.vue` — account type step + skip button

**Files:**
- Modify: `frontend/src/views/OnboardingView.vue`

The current `choose` mode only has "Quick Survey". Replace it with a two-button layout: "I'm a Patient" and "I'm a Therapist". Add a "Skip for now" button on the survey step.

**Step 1: Replace the `<script setup>` section**

Replace the entire `<script setup>` block with:

```typescript
<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useProfileStore } from '@/stores/profile'
import api from '@/services/api'

const router = useRouter()
const authStore = useAuthStore()
const profileStore = useProfileStore()
const mode = ref<'choose' | 'survey' | 'done'>('choose')

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

async function selectPatient() {
  mode.value = 'survey'
}

async function selectTherapist() {
  submitting.value = true
  error.value = null
  try {
    const res = await api.patch('/auth/me/role', { role: 'THERAPIST' })
    await authStore.updateToken(res.data.token)
  } catch {
    error.value = 'Could not set therapist role. Please try again.'
    submitting.value = false
    return
  }
  submitting.value = false
  mode.value = 'survey'
}

async function submitSurvey() {
  submitting.value = true
  error.value = null
  try {
    await profileStore.submitSurvey({
      moodBaseline: moodBaseline.value,
      anxietyLevel: anxietyLevel.value,
      sleepQuality: sleepQuality.value,
      lifeAreas: lifeAreas.value,
    })
    mode.value = 'done'
    setTimeout(() => router.push({ name: 'dashboard' }), 1500)
  } catch {
    error.value = 'Something went wrong. Please try again.'
  } finally {
    submitting.value = false
  }
}

async function skipSurvey() {
  submitting.value = true
  error.value = null
  try {
    await profileStore.skipSurvey()
    router.push({ name: 'dashboard' })
  } catch {
    error.value = 'Something went wrong. Please try again.'
  } finally {
    submitting.value = false
  }
}
</script>
```

**Step 2: Replace the `choose` mode template block**

Replace the existing `<div v-if="mode === 'choose'">` block with:

```html
      <div v-if="mode === 'choose'">
        <h1 class="onboarding-title">Welcome to MindTrack</h1>
        <p class="onboarding-subtitle">How will you be using MindTrack?</p>
        <div v-if="error" class="error-msg">{{ error }}</div>
        <div class="path-options">
          <button class="path-btn" :disabled="submitting" @click="selectPatient">
            <span class="path-icon">👤</span>
            <strong>I'm a Patient</strong>
            <span class="path-desc">Track my own mental health</span>
          </button>
          <button class="path-btn" :disabled="submitting" @click="selectTherapist">
            <span class="path-icon">🩺</span>
            <strong>I'm a Therapist</strong>
            <span class="path-desc">I work with patients</span>
          </button>
        </div>
      </div>
```

**Step 3: Add "Skip for now" button to the survey template**

In the survey `<div v-else-if="mode === 'survey'">` block, replace the submit button area:

```html
        <div class="survey-actions">
          <button class="submit-btn" :disabled="submitting" @click="submitSurvey">
            {{ submitting ? 'Setting up your goals...' : 'Create My Goals' }}
          </button>
          <button class="skip-btn" :disabled="submitting" @click="skipSurvey">
            Skip for now
          </button>
        </div>
```

**Step 4: Add CSS for survey actions and skip button**

In the `<style scoped>` block, add after `.submit-btn:disabled`:

```css
.survey-actions {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  margin-top: var(--space-4);
}
.skip-btn {
  width: 100%;
  padding: var(--space-2);
  background: transparent;
  color: var(--color-gray-500);
  border: none;
  font-size: 0.875rem;
  cursor: pointer;
}
.skip-btn:hover {
  color: var(--color-gray-700);
  text-decoration: underline;
}
.skip-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
```

**Step 5: Run frontend tests**

```bash
cd frontend && npm run test:unit 2>&1 | tail -10
```
Expected: all tests passing.

**Step 6: Commit**

```bash
git add frontend/src/views/OnboardingView.vue
git commit -m "#15 feat(onboarding): add account type step and survey skip button"
```

---

### Task 8: `ProfileView.vue` — Account Type + Wellness Baseline sections

**Files:**
- Modify: `frontend/src/views/ProfileView.vue`

**Step 1: Add `authStore` import and role-change logic to the script**

In `ProfileView.vue`, add to imports:

```typescript
import { useAuthStore } from '@/stores/auth'
import api from '@/services/api'
```

Add after the existing `const store = useProfileStore()` line:

```typescript
const authStore = useAuthStore()
const roleChanging = ref(false)
const roleError = ref<string | null>(null)
const showInlineSurvey = ref(false)
const surveyMood = ref(5)
const surveyAnxiety = ref(5)
const surveySleep = ref(5)
const surveyAreas = ref<string[]>([])
const LIFE_AREA_OPTIONS = ['Work', 'Relationships', 'Health', 'Fitness', 'Mindfulness', 'Hobbies']
const surveySubmitting = ref(false)
const surveyError = ref<string | null>(null)

function toggleSurveyArea(area: string) {
  const idx = surveyAreas.value.indexOf(area)
  if (idx >= 0) surveyAreas.value.splice(idx, 1)
  else surveyAreas.value.push(area)
}

async function switchRole() {
  const newRole = authStore.user?.role === 'THERAPIST' ? 'USER' : 'THERAPIST'
  roleChanging.value = true
  roleError.value = null
  try {
    const res = await api.patch('/auth/me/role', { role: newRole })
    await authStore.updateToken(res.data.token)
  } catch {
    roleError.value = 'Could not change role. Please try again.'
  } finally {
    roleChanging.value = false
  }
}

async function handleSurveySubmit() {
  surveySubmitting.value = true
  surveyError.value = null
  try {
    await store.submitSurvey({
      moodBaseline: surveyMood.value,
      anxietyLevel: surveyAnxiety.value,
      sleepQuality: surveySleep.value,
      lifeAreas: surveyAreas.value,
    })
    showInlineSurvey.value = false
  } catch {
    surveyError.value = 'Failed to submit survey.'
  } finally {
    surveySubmitting.value = false
  }
}
```

**Step 2: Add Account Type section to the template**

In the `<form>` block, add after the Personal Information section (after the first `</section>`):

```html
      <!-- Account Type -->
      <section class="form-section">
        <h2>Account Type</h2>
        <p class="field-description">
          {{ authStore.user?.role === 'THERAPIST' ? 'You are registered as a Therapist.' : 'You are registered as a Patient.' }}
        </p>
        <div v-if="roleError" class="error-message"><p>{{ roleError }}</p></div>
        <button
          type="button"
          class="btn btn-secondary"
          :disabled="roleChanging"
          @click="switchRole"
        >
          {{ roleChanging ? 'Changing...' : (authStore.user?.role === 'THERAPIST' ? 'Switch to Patient' : 'Switch to Therapist') }}
        </button>
      </section>
```

**Step 3: Add Wellness Baseline section to the template**

Add after the Account Type section:

```html
      <!-- Wellness Baseline -->
      <section id="wellness-baseline" class="form-section">
        <h2>Wellness Baseline</h2>
        <div class="baseline-status">
          <span :class="['baseline-badge', store.profile?.surveyCompleted ? 'badge-done' : 'badge-pending']">
            {{ store.profile?.surveyCompleted ? 'Completed' : 'Not yet completed' }}
          </span>
          <button
            type="button"
            class="btn btn-secondary"
            @click="showInlineSurvey = !showInlineSurvey"
          >
            {{ store.profile?.surveyCompleted ? 'Redo Survey' : 'Complete Survey' }}
          </button>
        </div>

        <div v-if="showInlineSurvey" class="inline-survey">
          <div v-if="surveyError" class="error-message"><p>{{ surveyError }}</p></div>
          <div class="form-group">
            <label class="form-group-label">Current mood (1–10): {{ surveyMood }}</label>
            <input v-model.number="surveyMood" type="range" min="1" max="10" class="slider" />
          </div>
          <div class="form-group">
            <label class="form-group-label">Anxiety level (1–10): {{ surveyAnxiety }}</label>
            <input v-model.number="surveyAnxiety" type="range" min="1" max="10" class="slider" />
          </div>
          <div class="form-group">
            <label class="form-group-label">Sleep quality (1–10): {{ surveySleep }}</label>
            <input v-model.number="surveySleep" type="range" min="1" max="10" class="slider" />
          </div>
          <div class="form-group">
            <label class="form-group-label">Life areas to improve</label>
            <div class="chip-group">
              <button
                v-for="area in LIFE_AREA_OPTIONS"
                :key="area"
                type="button"
                class="chip"
                :class="{ 'chip-active': surveyAreas.includes(area) }"
                @click="toggleSurveyArea(area)"
              >
                {{ area }}
              </button>
            </div>
          </div>
          <button
            type="button"
            class="btn btn-primary"
            :disabled="surveySubmitting"
            @click="handleSurveySubmit"
          >
            {{ surveySubmitting ? 'Saving...' : 'Save Baseline' }}
          </button>
        </div>
      </section>
```

**Step 4: Add CSS for the new sections**

In the `<style scoped>` block, add before the closing `</style>`:

```css
.field-description {
  font-size: var(--font-size-sm);
  color: var(--color-gray-600);
  margin-bottom: var(--space-3);
}

.baseline-status {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.baseline-badge {
  display: inline-block;
  padding: var(--space-1) var(--space-3);
  border-radius: 99px;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
}

.badge-done {
  background: #f0fdf4;
  color: var(--color-success);
  border: 1px solid #bbf7d0;
}

.badge-pending {
  background: #fefce8;
  color: #854d0e;
  border: 1px solid #fde047;
}

.inline-survey {
  border-top: 1px solid var(--color-gray-100);
  padding-top: var(--space-4);
  margin-top: var(--space-2);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.form-group-label {
  display: block;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-gray-700);
  margin-bottom: var(--space-1);
}

.slider {
  width: 100%;
}

.chip-group {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-top: var(--space-2);
}

.chip {
  padding: var(--space-1) var(--space-3);
  border-radius: 99px;
  border: 1px solid var(--color-gray-300);
  background: white;
  cursor: pointer;
  font-size: var(--font-size-sm);
  transition: all 0.15s;
}

.chip-active {
  background: var(--color-primary);
  color: white;
  border-color: var(--color-primary);
}
```

**Step 5: Run frontend tests**

```bash
cd frontend && npm run test:unit 2>&1 | tail -10
```
Expected: all tests passing.

**Step 6: Commit**

```bash
git add frontend/src/views/ProfileView.vue
git commit -m "#15 feat(profile): add Account Type and Wellness Baseline sections"
```

---

### Task 9: `DashboardView.vue` — survey prompt card

**Files:**
- Modify: `frontend/src/views/DashboardView.vue`

**Step 1: Add `profileStore` import and prompt card**

In `DashboardView.vue`, add the profile store import if not already present:

```typescript
import { useProfileStore } from '@/stores/profile'
const profileStore = useProfileStore()
```

If `profileStore` is already imported (it may be from previous work), just ensure it's accessible in the template.

**Step 2: Add the prompt card to the template**

In `DashboardView.vue`, add a prompt card at the top of the main content area (before the summary cards), inside the `v-if="store.summary"` block:

```html
        <!-- Wellness baseline prompt -->
        <div
          v-if="profileStore.profile && !profileStore.profile.surveyCompleted"
          class="survey-prompt"
        >
          <div class="survey-prompt-text">
            <strong>Complete your wellness baseline</strong>
            <span>Set your mood, sleep, and life area goals to personalise your dashboard.</span>
          </div>
          <router-link to="/profile#wellness-baseline" class="btn btn-primary btn-sm">
            Start survey →
          </router-link>
        </div>
```

**Step 3: Add CSS**

In the `<style scoped>` block of `DashboardView.vue`, add:

```css
.survey-prompt {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  background: #fffbeb;
  border: 1px solid #fde68a;
  border-radius: var(--border-radius-lg);
  padding: var(--space-4) var(--space-5);
  margin-bottom: var(--space-6);
}

.survey-prompt-text {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  font-size: var(--font-size-sm);
  color: var(--color-gray-700);
}

.survey-prompt-text strong {
  color: var(--color-gray-900);
}
```

**Step 4: Run frontend tests**

```bash
cd frontend && npm run test:unit 2>&1 | tail -10
```
Expected: all tests passing.

**Step 5: Commit**

```bash
git add frontend/src/views/DashboardView.vue
git commit -m "#15 feat(dashboard): add wellness baseline survey prompt card"
```

---

### Task 10: Final integration

**Step 1: Run full backend suite**

```bash
cd backend && mvn verify 2>&1 | tail -20
```
Expected: BUILD SUCCESS, Checkstyle clean.

**Step 2: Run full frontend suite**

```bash
cd frontend && npm run test:unit && npm run lint 2>&1 | tail -20
```
Expected: all tests passing, lint clean.

**Step 3: Verify test counts**

Backend: confirm count is higher than 345 (new tests added in Tasks 1, 3, 4).
Frontend: confirm count is higher than 283 (new test added in Task 5).

**Step 4: Commit if anything was auto-fixed by lint**

Only if `npm run lint -- --fix` made changes:

```bash
git add -p
git commit -m "#15 chore(lint): auto-fix formatting"
```
