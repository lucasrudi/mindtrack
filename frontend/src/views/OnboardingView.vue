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

<template>
  <div class="onboarding-view">
    <div class="onboarding-card">
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

        <div class="survey-actions">
          <button class="submit-btn" :disabled="submitting" @click="submitSurvey">
            {{ submitting ? 'Setting up your goals...' : 'Create My Goals' }}
          </button>
          <button class="skip-btn" :disabled="submitting" @click="skipSurvey">Skip for now</button>
        </div>
      </div>

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
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
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
.path-options {
  display: flex;
  gap: var(--space-4);
  flex-wrap: wrap;
}
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
.path-btn:hover {
  border-color: var(--color-primary);
}
.path-icon {
  font-size: 2rem;
}
.path-desc {
  font-size: 0.875rem;
  color: var(--color-gray-500);
}
.field-group {
  margin-bottom: var(--space-5);
}
.field-label {
  display: block;
  font-weight: 500;
  color: var(--color-gray-700);
  margin-bottom: var(--space-2);
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
  font-size: 0.875rem;
  transition: all 0.15s;
}
.chip.active {
  background: var(--color-primary);
  color: white;
  border-color: var(--color-primary);
}
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
.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
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
.error-msg {
  color: var(--color-error);
  background: #fef2f2;
  padding: var(--space-2) var(--space-3);
  border-radius: 8px;
  margin-bottom: var(--space-4);
}
.done-state {
  text-align: center;
  padding: var(--space-8) 0;
}
.done-icon {
  font-size: 3rem;
  display: block;
  margin-bottom: var(--space-3);
}
</style>
