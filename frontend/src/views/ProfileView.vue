<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useProfileStore, type ProfileForm } from '@/stores/profile'

const store = useProfileStore()
const successMessage = ref('')

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

      <div class="form-actions">
        <button type="submit" class="btn btn-primary" :disabled="store.saving">
          {{ store.saving ? 'Saving...' : 'Save Changes' }}
        </button>
      </div>
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
</style>
