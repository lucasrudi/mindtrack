<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useGoalsStore, type GoalStatus, type GoalValidationStatus } from '@/stores/goals'

const router = useRouter()
const store = useGoalsStore()

onMounted(() => {
  store.fetchGoals()
})

function navigateToNew() {
  router.push({ name: 'goal-new' })
}

function navigateToDetail(id: number) {
  router.push({ name: 'goal-detail', params: { id } })
}

function progressPercent(total: number, completed: number): number {
  if (total === 0) return 0
  return Math.round((completed / total) * 100)
}

function statusLabel(status: GoalStatus): string {
  const labels: Record<GoalStatus, string> = {
    NOT_STARTED: 'Not Started',
    IN_PROGRESS: 'In Progress',
    COMPLETED: 'Completed',
    PAUSED: 'Paused',
    CANCELLED: 'Cancelled',
  }
  return labels[status]
}

function statusClass(status: GoalStatus): string {
  const classes: Record<GoalStatus, string> = {
    NOT_STARTED: 'status-not-started',
    IN_PROGRESS: 'status-in-progress',
    COMPLETED: 'status-completed',
    PAUSED: 'status-paused',
    CANCELLED: 'status-cancelled',
  }
  return classes[status]
}

function formatDate(dateStr: string | null): string {
  if (!dateStr) return ''
  const date = new Date(dateStr + 'T00:00:00')
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

function validationLabel(status: GoalValidationStatus): string {
  const labels: Record<GoalValidationStatus, string> = {
    PENDING_VALIDATION: '⬜ Awaiting review',
    VALIDATED: '✅ Validated',
    OVERRIDDEN: '✏️ Modified',
    REJECTED: '❌ Not approved',
  }
  return labels[status] ?? status
}
</script>

<template>
  <div class="goals-view">
    <header class="page-header">
      <div>
        <h1>Goals</h1>
        <p class="subtitle">Set goals and track your progress with milestones</p>
      </div>
      <button class="btn btn-primary" @click="navigateToNew">+ New Goal</button>
    </header>

    <div v-if="store.error" class="error-message">
      <p>{{ store.error }}</p>
      <button class="btn btn-secondary" @click="store.clearError()">Dismiss</button>
    </div>

    <div v-if="store.loading && !store.goals.length" class="loading">
      <p>Loading goals...</p>
    </div>

    <div v-else-if="!store.goals.length" class="empty-state">
      <h2>No goals yet</h2>
      <p>Create your first goal to start tracking your progress.</p>
      <button class="btn btn-primary" @click="navigateToNew">Create a goal</button>
    </div>

    <template v-else>
      <div v-if="store.activeGoals.length" class="goals-section">
        <h2 class="section-title">Active</h2>
        <div class="goals-list">
          <div
            v-for="goal in store.activeGoals"
            :key="goal.id"
            class="goal-card"
            @click="navigateToDetail(goal.id)"
          >
            <div class="goal-header">
              <h3 class="goal-title">{{ goal.title }}</h3>
              <div class="goal-badges">
                <span :class="['status-badge', statusClass(goal.status)]">
                  {{ statusLabel(goal.status) }}
                </span>
                <span
                  v-if="goal.validationStatus"
                  class="validation-chip"
                  :class="`validation-chip--${goal.validationStatus.toLowerCase()}`"
                >
                  {{ validationLabel(goal.validationStatus) }}
                </span>
              </div>
            </div>
            <p v-if="goal.description" class="goal-desc">{{ goal.description }}</p>
            <div class="goal-meta">
              <span v-if="goal.category" class="goal-category">{{ goal.category }}</span>
              <span v-if="goal.targetDate" class="goal-date">
                Target: {{ formatDate(goal.targetDate) }}
              </span>
            </div>
            <div v-if="goal.totalMilestones > 0" class="progress-section">
              <div class="progress-bar">
                <div
                  class="progress-fill"
                  :style="{
                    width: progressPercent(goal.totalMilestones, goal.completedMilestones) + '%',
                  }"
                />
              </div>
              <span class="progress-text">
                {{ goal.completedMilestones }}/{{ goal.totalMilestones }} milestones
              </span>
            </div>
          </div>
        </div>
      </div>

      <div v-if="store.completedGoals.length" class="goals-section">
        <h2 class="section-title">Completed</h2>
        <div class="goals-list">
          <div
            v-for="goal in store.completedGoals"
            :key="goal.id"
            class="goal-card completed"
            @click="navigateToDetail(goal.id)"
          >
            <div class="goal-header">
              <h3 class="goal-title">{{ goal.title }}</h3>
              <span :class="['status-badge', statusClass(goal.status)]">
                {{ statusLabel(goal.status) }}
              </span>
            </div>
            <div class="goal-meta">
              <span v-if="goal.category" class="goal-category">{{ goal.category }}</span>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.goals-view {
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

.btn {
  display: inline-flex;
  align-items: center;
  padding: var(--space-3) var(--space-5);
  border-radius: var(--border-radius);
  font-weight: var(--font-weight-medium);
  font-size: var(--font-size-sm);
  cursor: pointer;
  border: none;
  transition: all var(--transition-fast);
}

.btn-primary {
  background: var(--color-primary);
  color: var(--color-white);
}

.btn-primary:hover {
  background: var(--color-primary-dark);
}

.btn-secondary {
  background: var(--color-gray-100);
  color: var(--color-gray-700);
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

.loading,
.empty-state {
  text-align: center;
  padding: var(--space-12) var(--space-6);
}

.empty-state h2 {
  font-size: var(--font-size-xl);
  color: var(--color-gray-700);
  margin-bottom: var(--space-2);
}

.empty-state p {
  color: var(--color-gray-500);
  margin-bottom: var(--space-6);
}

.goals-section {
  margin-bottom: var(--space-8);
}

.section-title {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-500);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: var(--space-3);
}

.goals-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.goal-card {
  padding: var(--space-5);
  background: var(--color-white);
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.goal-card:hover {
  border-color: var(--color-primary);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.goal-card.completed {
  opacity: 0.7;
}

.goal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-2);
}

.goal-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-900);
}

.status-badge {
  font-size: var(--font-size-xs);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--border-radius-full);
  font-weight: var(--font-weight-medium);
}

.status-not-started {
  background: var(--color-gray-100);
  color: var(--color-gray-600);
}

.status-in-progress {
  background: #eff6ff;
  color: #2563eb;
}

.status-completed {
  background: #f0fdf4;
  color: var(--color-success);
}

.status-paused {
  background: #fffbeb;
  color: #d97706;
}

.status-cancelled {
  background: #fef2f2;
  color: var(--color-error);
}

.goal-desc {
  font-size: var(--font-size-sm);
  color: var(--color-gray-600);
  margin-bottom: var(--space-3);
}

.goal-meta {
  display: flex;
  gap: var(--space-3);
  align-items: center;
  margin-bottom: var(--space-3);
}

.goal-category {
  font-size: var(--font-size-xs);
  color: var(--color-primary);
  background: var(--color-primary-50);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--border-radius-full);
  font-weight: var(--font-weight-medium);
}

.goal-date {
  font-size: var(--font-size-xs);
  color: var(--color-gray-500);
}

.progress-section {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.progress-bar {
  flex: 1;
  height: 6px;
  background: var(--color-gray-200);
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--color-primary);
  border-radius: 3px;
  transition: width var(--transition-fast);
}

.progress-text {
  font-size: var(--font-size-xs);
  color: var(--color-gray-500);
  white-space: nowrap;
}

.goal-badges {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.validation-chip {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 99px;
  font-size: 0.7rem;
  font-weight: 500;
}

.validation-chip--pending_validation {
  background: #f3f4f6;
  color: #6b7280;
}

.validation-chip--validated {
  background: #d1fae5;
  color: #065f46;
}

.validation-chip--overridden {
  background: #dbeafe;
  color: #1e40af;
}

.validation-chip--rejected {
  background: #fee2e2;
  color: #991b1b;
}
</style>
