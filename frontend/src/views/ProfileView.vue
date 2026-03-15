<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useProfileStore, type ProfileForm } from '@/stores/profile'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const store = useProfileStore()
const authStore = useAuthStore()
const roleChanging = ref(false)
const roleError = ref<string | null>(null)
const roleSuccess = ref(false)
const accountIsPatient = ref(true)
const accountIsTherapist = ref(false)
const showInlineSurvey = ref(false)
const surveyMood = ref(5)
const surveyAnxiety = ref(5)
const surveySleep = ref(5)
const surveyDepression = ref(5)
const surveyStress = ref(5)
const surveyEating = ref(5)
const surveyAreas = ref<string[]>([])
const LIFE_AREA_OPTIONS = ['Work', 'Relationships', 'Health', 'Fitness', 'Mindfulness', 'Hobbies']
const surveySubmitting = ref(false)
const surveyError = ref<string | null>(null)
const successMessage = ref('')

function toggleSurveyArea(area: string) {
  const idx = surveyAreas.value.indexOf(area)
  if (idx >= 0) surveyAreas.value.splice(idx, 1)
  else surveyAreas.value.push(area)
}

function surveyScoreClass(value: number): string {
  if (value >= 7) return 'score-green'
  if (value >= 4) return 'score-amber'
  return 'score-red'
}

async function saveRoles() {
  if (!accountIsPatient.value && !accountIsTherapist.value) {
    roleError.value = 'At least one role must be selected.'
    return
  }
  roleChanging.value = true
  roleError.value = null
  roleSuccess.value = false
  try {
    await store.updateRoles(accountIsPatient.value, accountIsTherapist.value)
    roleSuccess.value = true
    setTimeout(() => {
      roleSuccess.value = false
    }, 3000)
  } catch {
    roleError.value = 'Could not update roles. Please try again.'
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
      depressionScore: surveyDepression.value,
      stressLevel: surveyStress.value,
      eatingHabits: surveyEating.value,
      lifeAreas: surveyAreas.value,
    })
    showInlineSurvey.value = false
  } catch {
    surveyError.value = 'Failed to submit survey.'
  } finally {
    surveySubmitting.value = false
  }
}

const form = ref<ProfileForm>({
  displayName: null,
  avatarUrl: null,
  timezone: null,
  notificationPrefs: null,
  telegramChatId: null,
  whatsappNumber: null,
})

const emailNotifications = ref(false)
const pushNotifications = ref(false)
const reminderTime = ref('09:00')

const timezones = [
  'America/New_York',
  'America/Chicago',
  'America/Denver',
  'America/Los_Angeles',
  'America/Sao_Paulo',
  'Europe/London',
  'Europe/Paris',
  'Europe/Berlin',
  'Europe/Amsterdam',
  'Europe/Madrid',
  'Europe/Rome',
  'Asia/Tokyo',
  'Asia/Shanghai',
  'Asia/Kolkata',
  'Asia/Dubai',
  'Australia/Sydney',
  'Pacific/Auckland',
  'UTC',
]

onMounted(async () => {
  try {
    await store.fetchProfile()
    populateForm()
  } catch {
    // Error handled by store
  }
})

watch(
  () => store.profile,
  () => populateForm(),
)

function populateForm() {
  if (!store.profile) return
  form.value.displayName = store.profile.displayName
  form.value.avatarUrl = store.profile.avatarUrl
  form.value.timezone = store.profile.timezone
  form.value.telegramChatId = store.profile.telegramChatId
  form.value.whatsappNumber = store.profile.whatsappNumber

  accountIsPatient.value = store.profile.isPatient ?? true
  accountIsTherapist.value = store.profile.isTherapist ?? false

  const prefs = store.profile.notificationPrefs
  if (prefs) {
    emailNotifications.value = !!prefs.emailNotifications
    pushNotifications.value = !!prefs.pushNotifications
    reminderTime.value = (prefs.reminderTime as string) || '09:00'
  }
}

async function handleSave() {
  successMessage.value = ''
  form.value.notificationPrefs = {
    emailNotifications: emailNotifications.value,
    pushNotifications: pushNotifications.value,
    reminderTime: reminderTime.value,
  }

  try {
    await store.updateProfile(form.value)
    successMessage.value = 'Profile saved successfully'
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch {
    // Error handled by store
  }
}

function unlinkTelegram() {
  form.value.telegramChatId = null
}

function unlinkWhatsapp() {
  form.value.whatsappNumber = null
}

async function replayTutorial() {
  try {
    await store.updateProfile({ tutorialCompleted: false } as unknown as Parameters<
      typeof store.updateProfile
    >[0])
    router.push('/dashboard')
  } catch {
    // Error handled by store
  }
}

const showDeleteConfirm = ref(false)
const deleteConfirmText = ref('')
const deleting = ref(false)
const deleteError = ref<string | null>(null)

function cancelDelete() {
  showDeleteConfirm.value = false
  deleteConfirmText.value = ''
}

async function handleDeleteAccount() {
  if (deleteConfirmText.value !== 'DELETE') return
  deleting.value = true
  deleteError.value = null
  try {
    await authStore.deleteAccount()
    router.push('/login')
  } catch {
    deleteError.value = 'Could not delete account. Please try again.'
    deleting.value = false
  }
}
</script>

<template>
  <div class="profile-view">
    <header class="page-header">
      <div>
        <h1>Profile Settings</h1>
        <p class="subtitle">Manage your account preferences</p>
      </div>
    </header>

    <div v-if="store.error" class="error-message">
      <p>{{ store.error }}</p>
      <button class="btn btn-secondary" @click="store.clearError()">Dismiss</button>
    </div>

    <div v-if="successMessage" class="success-message">
      <p>{{ successMessage }}</p>
    </div>

    <div v-if="store.loading" class="loading">
      <p>Loading profile...</p>
    </div>

    <form v-else class="profile-form" @submit.prevent="handleSave">
      <!-- Personal Info -->
      <section class="form-section">
        <h2>Personal Information</h2>
        <div class="form-group">
          <label for="displayName">Display Name</label>
          <input
            id="displayName"
            v-model="form.displayName"
            type="text"
            class="form-input"
            placeholder="Your display name"
          />
        </div>
        <div class="form-group">
          <label for="avatarUrl">Avatar URL</label>
          <input
            id="avatarUrl"
            v-model="form.avatarUrl"
            type="url"
            class="form-input"
            placeholder="https://example.com/avatar.jpg"
          />
          <div v-if="form.avatarUrl" class="avatar-preview">
            <img :src="form.avatarUrl" alt="Avatar preview" class="avatar-img" />
          </div>
        </div>
      </section>

      <!-- Account Type -->
      <section class="form-section">
        <h2>Account Type</h2>
        <p class="field-description">Select the roles that apply to you. You can be both.</p>
        <div v-if="roleError" class="error-message">
          <p>{{ roleError }}</p>
        </div>
        <div v-if="roleSuccess" class="success-message">
          <p>Roles updated successfully.</p>
        </div>
        <div class="role-toggle-group">
          <label class="role-toggle-label">
            <input v-model="accountIsPatient" type="checkbox" :disabled="roleChanging" />
            <div>
              <span class="role-toggle-name">Patient</span>
              <span class="role-toggle-desc">Track my own mental health</span>
            </div>
          </label>
          <label class="role-toggle-label">
            <input v-model="accountIsTherapist" type="checkbox" :disabled="roleChanging" />
            <div>
              <span class="role-toggle-name">Therapist</span>
              <span class="role-toggle-desc">I work with patients</span>
            </div>
          </label>
        </div>
        <button type="button" class="btn btn-secondary" :disabled="roleChanging" @click="saveRoles">
          {{ roleChanging ? 'Saving...' : 'Save' }}
        </button>
      </section>

      <!-- Wellness Baseline -->
      <section id="wellness-baseline" class="form-section">
        <h2>Wellness Baseline</h2>
        <div class="baseline-status">
          <span
            :class="[
              'baseline-badge',
              store.profile?.surveyCompleted ? 'badge-done' : 'badge-pending',
            ]"
          >
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
          <div v-if="surveyError" class="error-message">
            <p>{{ surveyError }}</p>
          </div>

          <div class="survey-categories">
            <!-- Mental Health -->
            <div class="health-card">
              <div class="health-card-title">Mental Health</div>
              <div class="health-metric">
                <div class="metric-header">
                  <span class="metric-label">Anxiety</span>
                  <span class="metric-score" :class="surveyScoreClass(surveyAnxiety)">{{
                    surveyAnxiety
                  }}</span>
                </div>
                <input
                  v-model.number="surveyAnxiety"
                  type="range"
                  min="1"
                  max="10"
                  class="metric-slider"
                />
              </div>
              <div class="health-metric">
                <div class="metric-header">
                  <span class="metric-label">Depression</span>
                  <span class="metric-score" :class="surveyScoreClass(surveyDepression)">{{
                    surveyDepression
                  }}</span>
                </div>
                <input
                  v-model.number="surveyDepression"
                  type="range"
                  min="1"
                  max="10"
                  class="metric-slider"
                />
              </div>
              <div class="health-metric">
                <div class="metric-header">
                  <span class="metric-label">Stress</span>
                  <span class="metric-score" :class="surveyScoreClass(surveyStress)">{{
                    surveyStress
                  }}</span>
                </div>
                <input
                  v-model.number="surveyStress"
                  type="range"
                  min="1"
                  max="10"
                  class="metric-slider"
                />
              </div>
            </div>

            <!-- Sleep -->
            <div class="health-card">
              <div class="health-card-title">Sleep</div>
              <div class="health-metric">
                <div class="metric-header">
                  <span class="metric-label">Sleep quality</span>
                  <span class="metric-score" :class="surveyScoreClass(surveySleep)">{{
                    surveySleep
                  }}</span>
                </div>
                <input
                  v-model.number="surveySleep"
                  type="range"
                  min="1"
                  max="10"
                  class="metric-slider"
                />
              </div>
            </div>

            <!-- Nutrition -->
            <div class="health-card">
              <div class="health-card-title">Nutrition</div>
              <div class="health-metric">
                <div class="metric-header">
                  <span class="metric-label">Eating habits</span>
                  <span class="metric-score" :class="surveyScoreClass(surveyEating)">{{
                    surveyEating
                  }}</span>
                </div>
                <input
                  v-model.number="surveyEating"
                  type="range"
                  min="1"
                  max="10"
                  class="metric-slider"
                />
              </div>
            </div>

            <!-- Emotional -->
            <div class="health-card">
              <div class="health-card-title">Emotional</div>
              <div class="health-metric">
                <div class="metric-header">
                  <span class="metric-label">Overall mood</span>
                  <span class="metric-score" :class="surveyScoreClass(surveyMood)">{{
                    surveyMood
                  }}</span>
                </div>
                <input
                  v-model.number="surveyMood"
                  type="range"
                  min="1"
                  max="10"
                  class="metric-slider"
                />
              </div>
            </div>
          </div>

          <div class="form-group">
            <span class="form-group-label">Life areas to improve</span>
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

      <!-- Timezone -->
      <section class="form-section">
        <h2>Timezone</h2>
        <div class="form-group">
          <label for="timezone">Preferred Timezone</label>
          <select id="timezone" v-model="form.timezone" class="form-select">
            <option :value="null">Select a timezone</option>
            <option v-for="tz in timezones" :key="tz" :value="tz">{{ tz }}</option>
          </select>
        </div>
      </section>

      <!-- Notifications -->
      <section class="form-section">
        <h2>Notifications</h2>
        <div class="checkbox-group">
          <label class="checkbox-label">
            <input v-model="emailNotifications" type="checkbox" />
            <span>Email notifications</span>
          </label>
          <label class="checkbox-label">
            <input v-model="pushNotifications" type="checkbox" />
            <span>Push notifications</span>
          </label>
        </div>
        <div class="form-group">
          <label for="reminderTime">Daily reminder time</label>
          <input id="reminderTime" v-model="reminderTime" type="time" class="form-input" />
        </div>
      </section>

      <!-- Messaging -->
      <section class="form-section">
        <h2>Messaging Integration</h2>
        <div class="form-group">
          <label for="telegramChatId">Telegram Chat ID</label>
          <div class="input-with-action">
            <input
              id="telegramChatId"
              v-model="form.telegramChatId"
              type="text"
              class="form-input"
              placeholder="Your Telegram chat ID"
            />
            <button
              v-if="form.telegramChatId"
              type="button"
              class="btn btn-sm btn-danger"
              @click="unlinkTelegram"
            >
              Unlink
            </button>
          </div>
        </div>
        <div class="form-group">
          <label for="whatsappNumber">WhatsApp Number</label>
          <div class="input-with-action">
            <input
              id="whatsappNumber"
              v-model="form.whatsappNumber"
              type="text"
              class="form-input"
              placeholder="+1234567890"
            />
            <button
              v-if="form.whatsappNumber"
              type="button"
              class="btn btn-sm btn-danger"
              @click="unlinkWhatsapp"
            >
              Unlink
            </button>
          </div>
        </div>
      </section>

      <!-- Tutorial -->
      <section class="form-section">
        <h2>Tutorial</h2>
        <p class="tutorial-description">
          Take a guided tour of MindTrack's features. The tutorial will walk you through the key
          areas of the app.
        </p>
        <button type="button" class="btn btn-secondary" @click="replayTutorial">
          Replay Tutorial
        </button>
      </section>

      <div class="form-actions">
        <button type="submit" class="btn btn-primary" :disabled="store.saving">
          {{ store.saving ? 'Saving...' : 'Save Changes' }}
        </button>
      </div>

      <!-- Danger Zone -->
      <section class="form-section danger-zone">
        <h2>Danger Zone</h2>
        <p class="field-description">
          Permanently delete your account and all associated data. This action cannot be undone.
          Your data will be anonymised immediately and permanently removed after 30 days.
        </p>
        <button
          v-if="!showDeleteConfirm"
          type="button"
          class="btn btn-danger-outline"
          @click="showDeleteConfirm = true"
        >
          Delete Account
        </button>

        <div v-if="showDeleteConfirm" class="delete-confirm-panel">
          <p class="delete-warning">
            This will immediately anonymise your account and schedule all data for deletion. You
            will be logged out and will not be able to log back in. Type <strong>DELETE</strong> to
            confirm.
          </p>
          <div v-if="deleteError" class="error-message">
            <p>{{ deleteError }}</p>
          </div>
          <div class="delete-confirm-actions">
            <input
              v-model="deleteConfirmText"
              type="text"
              class="form-input delete-confirm-input"
              placeholder="Type DELETE to confirm"
              :disabled="deleting"
            />
            <button
              type="button"
              class="btn btn-danger"
              :disabled="deleteConfirmText !== 'DELETE' || deleting"
              @click="handleDeleteAccount"
            >
              {{ deleting ? 'Deleting...' : 'Confirm Delete' }}
            </button>
            <button
              type="button"
              class="btn btn-secondary"
              :disabled="deleting"
              @click="cancelDelete"
            >
              Cancel
            </button>
          </div>
        </div>
      </section>
    </form>
  </div>
</template>

<style scoped>
.profile-view {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-8) var(--space-6);
}

.page-header {
  margin-bottom: var(--space-6);
}

.page-header h1 {
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-gray-900);
  margin: 0;
}

.subtitle {
  color: var(--color-gray-500);
  margin-top: var(--space-1);
}

.error-message {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: var(--color-error);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--border-radius);
  margin-bottom: var(--space-6);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.success-message {
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  color: var(--color-success);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--border-radius);
  margin-bottom: var(--space-6);
}

.loading {
  text-align: center;
  padding: var(--space-12);
  color: var(--color-gray-500);
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-8);
}

.form-section {
  background: var(--color-white);
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius-lg);
  padding: var(--space-6);
}

.form-section h2 {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-900);
  margin: 0 0 var(--space-4);
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--color-gray-100);
}

.form-group {
  margin-bottom: var(--space-4);
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-group label {
  display: block;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-gray-700);
  margin-bottom: var(--space-1);
}

.form-input,
.form-select {
  width: 100%;
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-gray-300);
  border-radius: var(--border-radius);
  font-size: var(--font-size-sm);
  color: var(--color-gray-900);
  background: var(--color-white);
  transition: border-color var(--transition-fast);
}

.form-input:focus,
.form-select:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-50);
}

.form-input::placeholder {
  color: var(--color-gray-400);
}

.avatar-preview {
  margin-top: var(--space-2);
}

.avatar-img {
  width: 64px;
  height: 64px;
  border-radius: var(--border-radius-full);
  object-fit: cover;
  border: 2px solid var(--color-gray-200);
}

.checkbox-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--font-size-sm);
  color: var(--color-gray-700);
  cursor: pointer;
}

.checkbox-label input[type='checkbox'] {
  cursor: pointer;
}

.input-with-action {
  display: flex;
  gap: var(--space-2);
  align-items: center;
}

.input-with-action .form-input {
  flex: 1;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
}

/* Buttons */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-3) var(--space-5);
  border-radius: var(--border-radius);
  font-weight: var(--font-weight-medium);
  font-size: var(--font-size-sm);
  cursor: pointer;
  border: none;
  transition: all var(--transition-fast);
}

.btn-sm {
  padding: var(--space-2) var(--space-3);
  font-size: var(--font-size-xs);
}

.btn-primary {
  background: var(--color-primary);
  color: var(--color-white);
}

.btn-primary:hover {
  background: var(--color-primary-dark);
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
  background: var(--color-gray-100);
  color: var(--color-gray-700);
}

.btn-secondary:hover {
  background: var(--color-gray-200);
}

.btn-danger {
  background: #fef2f2;
  color: var(--color-error);
}

.btn-danger:hover {
  background: #fee2e2;
}

.tutorial-description {
  font-size: var(--font-size-sm);
  color: var(--color-gray-600);
  margin-bottom: var(--space-4);
}

.field-description {
  font-size: var(--font-size-sm);
  color: var(--color-gray-600);
  margin-bottom: var(--space-3);
}

.role-toggle-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.role-toggle-label {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  cursor: pointer;
  font-size: var(--font-size-sm);
  color: var(--color-gray-700);
}

.role-toggle-label input[type='checkbox'] {
  margin-top: 2px;
  width: 16px;
  height: 16px;
  cursor: pointer;
  flex-shrink: 0;
}

.role-toggle-name {
  display: block;
  font-weight: var(--font-weight-medium);
  color: var(--color-gray-900);
}

.role-toggle-desc {
  display: block;
  font-size: var(--font-size-xs);
  color: var(--color-gray-500);
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

.survey-categories {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.health-card {
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius-lg, 12px);
  padding: var(--space-3) var(--space-4);
  background: var(--color-gray-50, #f9fafb);
}

.health-card-title {
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--color-gray-500);
  margin-bottom: var(--space-2);
}

.health-metric {
  margin-bottom: var(--space-2);
}

.health-metric:last-child {
  margin-bottom: 0;
}

.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-1);
}

.metric-label {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-gray-700);
}

.metric-score {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  min-width: 1.5rem;
  text-align: right;
  border-radius: 4px;
  padding: 1px 6px;
}

.score-green {
  color: #166534;
  background: #dcfce7;
}

.score-amber {
  color: #92400e;
  background: #fef3c7;
}

.score-red {
  color: #991b1b;
  background: #fee2e2;
}

.metric-slider {
  width: 100%;
}

.danger-zone {
  border-color: #fecaca;
}

.danger-zone h2 {
  color: var(--color-error);
  border-bottom-color: #fecaca;
}

.btn-danger-outline {
  background: transparent;
  color: var(--color-error);
  border: 1px solid var(--color-error);
  padding: var(--space-3) var(--space-5);
  border-radius: var(--border-radius);
  font-weight: var(--font-weight-medium);
  font-size: var(--font-size-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn-danger-outline:hover {
  background: #fef2f2;
}

.delete-confirm-panel {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  margin-top: var(--space-2);
}

.delete-warning {
  font-size: var(--font-size-sm);
  color: var(--color-gray-700);
  background: #fef2f2;
  border: 1px solid #fecaca;
  padding: var(--space-3) var(--space-4);
  border-radius: var(--border-radius);
}

.delete-confirm-actions {
  display: flex;
  gap: var(--space-2);
  align-items: center;
  flex-wrap: wrap;
}

.delete-confirm-input {
  max-width: 220px;
}
</style>
