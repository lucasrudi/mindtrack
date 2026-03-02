<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useProfileStore } from '@/stores/profile'

const router = useRouter()
const profileStore = useProfileStore()
const mode = ref<'choose' | 'survey' | 'done'>('choose')

const selectedPatient = ref(true)
const selectedTherapist = ref(false)

const moodBaseline = ref(5)
const anxietyLevel = ref(5)
const sleepQuality = ref(5)
const depressionScore = ref(5)
const stressLevel = ref(5)
const eatingHabits = ref(5)
const lifeAreas = ref<string[]>([])
const submitting = ref(false)
const error = ref<string | null>(null)

const LIFE_AREA_OPTIONS = ['Work', 'Relationships', 'Health', 'Fitness', 'Mindfulness', 'Hobbies']

function toggleArea(area: string) {
  const idx = lifeAreas.value.indexOf(area)
  if (idx >= 0) lifeAreas.value.splice(idx, 1)
  else lifeAreas.value.push(area)
}

function scoreClass(value: number): string {
  if (value >= 7) return 'score-green'
  if (value >= 4) return 'score-amber'
  return 'score-red'
}

async function continueToSurvey() {
  if (!selectedPatient.value && !selectedTherapist.value) {
    error.value = 'Please select at least one role to continue.'
    return
  }
  submitting.value = true
  error.value = null
  try {
    await profileStore.updateRoles(selectedPatient.value, selectedTherapist.value)
    mode.value = 'survey'
  } catch {
    error.value = 'Could not set roles. Please try again.'
  } finally {
    submitting.value = false
  }
}

async function submitSurvey() {
  submitting.value = true
  error.value = null
  try {
    await profileStore.submitSurvey({
      moodBaseline: moodBaseline.value,
      anxietyLevel: anxietyLevel.value,
      sleepQuality: sleepQuality.value,
      depressionScore: depressionScore.value,
      stressLevel: stressLevel.value,
      eatingHabits: eatingHabits.value,
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
        <p class="onboarding-subtitle">Choose your roles (you can select both)</p>
        <div v-if="error" class="error-msg">{{ error }}</div>
        <div class="role-options">
          <label class="role-option">
            <input v-model="selectedPatient" type="checkbox" class="role-checkbox" />
            <div class="role-option-content">
              <strong class="role-option-title">I'm a patient</strong>
              <span class="role-option-desc">I want to track my own mental health</span>
            </div>
          </label>
          <label class="role-option">
            <input v-model="selectedTherapist" type="checkbox" class="role-checkbox" />
            <div class="role-option-content">
              <strong class="role-option-title">I'm a therapist</strong>
              <span class="role-option-desc">I work with patients</span>
            </div>
          </label>
        </div>
        <button class="submit-btn" :disabled="submitting" @click="continueToSurvey">
          {{ submitting ? 'Setting up...' : 'Continue' }}
        </button>
      </div>

      <div v-else-if="mode === 'survey'">
        <h2 class="onboarding-title">Tell us about yourself</h2>
        <div v-if="error" class="error-msg">{{ error }}</div>

        <div class="survey-categories">
          <!-- Mental Health -->
          <div class="health-card">
            <div class="health-card-title">Mental Health</div>
            <div class="health-metric">
              <div class="metric-header">
                <span class="metric-label">Anxiety</span>
                <span class="metric-score" :class="scoreClass(anxietyLevel)">{{
                  anxietyLevel
                }}</span>
              </div>
              <input
                v-model.number="anxietyLevel"
                type="range"
                min="1"
                max="10"
                class="metric-slider"
              />
            </div>
            <div class="health-metric">
              <div class="metric-header">
                <span class="metric-label">Depression</span>
                <span class="metric-score" :class="scoreClass(depressionScore)">{{
                  depressionScore
                }}</span>
              </div>
              <input
                v-model.number="depressionScore"
                type="range"
                min="1"
                max="10"
                class="metric-slider"
              />
            </div>
            <div class="health-metric">
              <div class="metric-header">
                <span class="metric-label">Stress</span>
                <span class="metric-score" :class="scoreClass(stressLevel)">{{ stressLevel }}</span>
              </div>
              <input
                v-model.number="stressLevel"
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
                <span class="metric-score" :class="scoreClass(sleepQuality)">{{
                  sleepQuality
                }}</span>
              </div>
              <input
                v-model.number="sleepQuality"
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
                <span class="metric-score" :class="scoreClass(eatingHabits)">{{
                  eatingHabits
                }}</span>
              </div>
              <input
                v-model.number="eatingHabits"
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
                <span class="metric-score" :class="scoreClass(moodBaseline)">{{
                  moodBaseline
                }}</span>
              </div>
              <input
                v-model.number="moodBaseline"
                type="range"
                min="1"
                max="10"
                class="metric-slider"
              />
            </div>
          </div>
        </div>

        <div class="field-group">
          <label class="field-label">Life areas to improve</label>
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
.role-options {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  margin-bottom: var(--space-5);
}
.role-option {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  border: 2px solid var(--color-gray-200);
  border-radius: 12px;
  padding: var(--space-4);
  cursor: pointer;
  transition: border-color 0.2s;
}
.role-option:hover {
  border-color: var(--color-primary);
}
.role-checkbox {
  margin-top: 2px;
  width: 18px;
  height: 18px;
  cursor: pointer;
  flex-shrink: 0;
}
.role-option-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}
.role-option-title {
  font-size: 1rem;
  color: var(--color-gray-900);
}
.role-option-desc {
  font-size: 0.875rem;
  color: var(--color-gray-500);
}
.survey-categories {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  margin-bottom: var(--space-5);
}
.health-card {
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius-lg, 12px);
  padding: var(--space-4);
  background: var(--color-gray-50, #f9fafb);
}
.health-card-title {
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--color-gray-500);
  margin-bottom: var(--space-3);
}
.health-metric {
  margin-bottom: var(--space-3);
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
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--color-gray-700);
}
.metric-score {
  font-size: 0.875rem;
  font-weight: 700;
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
.field-group {
  margin-bottom: var(--space-5);
}
.field-label {
  display: block;
  font-weight: 500;
  color: var(--color-gray-700);
  margin-bottom: var(--space-2);
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
