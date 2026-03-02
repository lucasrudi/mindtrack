<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useAnalyticsStore } from '@/stores/analytics'
import { useProfileStore } from '@/stores/profile'
import { useTutorial } from '@/composables/useTutorial'
import MoodTrendChart from '@/components/charts/MoodTrendChart.vue'
import ActivityCompletionChart from '@/components/charts/ActivityCompletionChart.vue'
import GoalProgressChart from '@/components/charts/GoalProgressChart.vue'
import TutorialOverlay from '@/components/tutorial/TutorialOverlay.vue'

const store = useAnalyticsStore()
const profileStore = useProfileStore()
const { start: startTutorial } = useTutorial()
const surveyPromptDismissed = ref(false)

const presets = [
  { label: '7 days', days: 7 },
  { label: '30 days', days: 30 },
  { label: '90 days', days: 90 },
]

function setPreset(days: number) {
  const to = new Date()
  const from = new Date()
  from.setDate(from.getDate() - days)
  store.setDateRange(from.toISOString().split('T')[0], to.toISOString().split('T')[0])
}

function isActivePreset(days: number): boolean {
  const to = new Date()
  const from = new Date()
  from.setDate(from.getDate() - days)
  return (
    store.dateRange.from === from.toISOString().split('T')[0] &&
    store.dateRange.to === to.toISOString().split('T')[0]
  )
}

function formatRate(rate: number | null): string {
  if (rate === null || rate === undefined) return '0%'
  return `${Math.round(rate)}%`
}

function formatMood(mood: number | null): string {
  if (mood === null || mood === undefined || mood === 0) return '--'
  return mood.toFixed(1)
}

onMounted(async () => {
  try {
    await store.fetchAll()
  } catch {
    // Error state handled by store
  }

  // Check if tutorial should be shown
  try {
    await profileStore.fetchProfile()
    if (profileStore.profile && !profileStore.profile.tutorialCompleted) {
      startTutorial()
    }
  } catch {
    // Profile fetch failure shouldn't block dashboard
  }
})

watch(
  () => store.dateRange,
  async () => {
    try {
      await store.fetchAll()
    } catch {
      // Error state handled by store
    }
  },
  { deep: true },
)
</script>

<template>
  <div class="dashboard-view">
    <header class="page-header">
      <div>
        <h1>Dashboard</h1>
        <p class="subtitle">Your mental health overview</p>
      </div>
      <div class="date-range">
        <button
          v-for="preset in presets"
          :key="preset.days"
          :class="['preset-btn', { active: isActivePreset(preset.days) }]"
          @click="setPreset(preset.days)"
        >
          {{ preset.label }}
        </button>
      </div>
    </header>

    <div
      v-if="profileStore.profile && !profileStore.profile.surveyCompleted && !surveyPromptDismissed"
      class="survey-prompt"
      data-testid="survey-prompt"
    >
      <div class="survey-prompt-content">
        <span class="survey-prompt-icon">📋</span>
        <div class="survey-prompt-text">
          <strong>Complete your wellness baseline</strong>
          <span>It only takes 2 minutes and helps us personalise your goals.</span>
        </div>
        <router-link to="/profile#wellness-baseline" class="survey-prompt-link">
          Complete now →
        </router-link>
      </div>
      <button
        class="survey-prompt-dismiss"
        aria-label="Dismiss"
        @click="surveyPromptDismissed = true"
      >
        ✕
      </button>
    </div>

    <div v-if="store.error" class="error-message">
      <p>{{ store.error }}</p>
      <button class="btn btn-secondary btn-sm" @click="store.clearError()">Dismiss</button>
    </div>

    <div v-if="store.loading" class="loading">
      <p>Loading your dashboard...</p>
    </div>

    <template v-else-if="store.summary">
      <!-- Summary Cards -->
      <div class="summary-cards">
        <div class="summary-card">
          <span class="card-label">Average Mood</span>
          <span class="card-value card-value--mood">
            {{ formatMood(store.summary.averageMood) }}
          </span>
          <span class="card-unit">/10</span>
        </div>
        <div class="summary-card">
          <span class="card-label">Activity Rate</span>
          <span class="card-value card-value--activity">
            {{ formatRate(store.summary.activityCompletionRate) }}
          </span>
          <span class="card-detail"> {{ store.summary.totalActivitiesLogged }} logged </span>
        </div>
        <div class="summary-card">
          <span class="card-label">Goals</span>
          <span class="card-value card-value--goals">
            {{ store.summary.completedGoals }}/{{ store.summary.totalGoals }}
          </span>
          <span class="card-detail"> {{ store.summary.activeGoals }} active </span>
        </div>
        <div class="summary-card">
          <span class="card-label">Journal Entries</span>
          <span class="card-value card-value--journal">
            {{ store.summary.totalJournalEntries }}
          </span>
          <span class="card-detail">in this period</span>
        </div>
      </div>

      <!-- Goal Validation Cards -->
      <div class="validation-cards">
        <div class="validation-card">
          <span class="vcard-icon">✅</span>
          <span class="vcard-count">{{ store.summary.validatedGoals }}</span>
          <span class="vcard-label">Validated Goals</span>
        </div>
        <div class="validation-card">
          <span class="vcard-icon">⬜</span>
          <span class="vcard-count">{{ store.summary.pendingValidationGoals }}</span>
          <span class="vcard-label">Pending Review</span>
        </div>
      </div>

      <!-- Charts -->
      <div class="charts-section">
        <div class="chart-card chart-card--full">
          <h2 class="chart-title">Mood Trends</h2>
          <MoodTrendChart :data="store.moodTrends" />
        </div>
        <div class="charts-row">
          <div class="chart-card">
            <h2 class="chart-title">Activity Completion</h2>
            <ActivityCompletionChart :data="store.activityStats" />
          </div>
          <div class="chart-card">
            <h2 class="chart-title">Goal Progress</h2>
            <GoalProgressChart :data="store.goalProgress" />
          </div>
        </div>
      </div>
    </template>

    <TutorialOverlay />
  </div>
</template>

<style scoped>
.dashboard-view {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-8) var(--space-6);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
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

.date-range {
  display: flex;
  gap: var(--space-2);
}

.preset-btn {
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-gray-300);
  border-radius: var(--border-radius-sm);
  background: var(--color-white);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-gray-600);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.preset-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.preset-btn.active {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: var(--color-white);
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

.loading {
  text-align: center;
  padding: var(--space-12);
  color: var(--color-gray-500);
}

/* Summary Cards */
.summary-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
  margin-bottom: var(--space-8);
}

.summary-card {
  background: var(--color-white);
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius-lg);
  padding: var(--space-5);
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.card-label {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-500);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.card-value {
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  line-height: 1;
}

.card-value--mood {
  color: #6366f1;
}

.card-value--activity {
  color: #10b981;
}

.card-value--goals {
  color: #f59e0b;
}

.card-value--journal {
  color: #8b5cf6;
}

.card-unit {
  font-size: var(--font-size-sm);
  color: var(--color-gray-400);
}

.card-detail {
  font-size: var(--font-size-sm);
  color: var(--color-gray-500);
}

/* Charts */
.charts-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.chart-card {
  background: var(--color-white);
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius-lg);
  padding: var(--space-5);
}

.chart-card--full {
  width: 100%;
}

.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-6);
}

.chart-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-900);
  margin: 0 0 var(--space-4) 0;
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

.btn-secondary {
  background: var(--color-gray-100);
  color: var(--color-gray-700);
}

.btn-secondary:hover {
  background: var(--color-gray-200);
}

/* Goal Validation Cards */
.validation-cards {
  display: flex;
  gap: var(--space-4);
  margin-bottom: var(--space-8);
  flex-wrap: wrap;
}

.validation-card {
  flex: 1;
  min-width: 140px;
  background: var(--color-white);
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius-lg);
  padding: var(--space-4);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-1);
}

.vcard-icon {
  font-size: 1.5rem;
}

.vcard-count {
  font-size: 2rem;
  font-weight: 700;
  color: var(--color-gray-900);
}

.vcard-label {
  font-size: 0.75rem;
  color: var(--color-gray-500);
  text-align: center;
}

/* Survey Prompt */
.survey-prompt {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  background: #fffbeb;
  border: 1px solid #fde68a;
  border-radius: 10px;
  padding: var(--space-4) var(--space-5);
  margin-bottom: var(--space-6);
}

.survey-prompt-content {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex: 1;
}

.survey-prompt-icon {
  font-size: 1.5rem;
  flex-shrink: 0;
}

.survey-prompt-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 0.9rem;
  color: var(--color-gray-700);
}

.survey-prompt-link {
  font-size: 0.875rem;
  font-weight: 600;
  color: #b45309;
  text-decoration: none;
  white-space: nowrap;
}

.survey-prompt-link:hover {
  text-decoration: underline;
}

.survey-prompt-dismiss {
  background: none;
  border: none;
  cursor: pointer;
  color: var(--color-gray-500);
  font-size: 1rem;
  padding: 2px 4px;
  line-height: 1;
  flex-shrink: 0;
}

.survey-prompt-dismiss:hover {
  color: var(--color-gray-700);
}

/* Responsive */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: var(--space-4);
  }

  .summary-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .charts-row {
    grid-template-columns: 1fr;
  }
}
</style>
