<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useGoalsStore, type GoalStatus, type MilestoneForm } from '@/stores/goals'

const route = useRoute()
const router = useRouter()
const store = useGoalsStore()

const goalId = Number(route.params.id)
const showDeleteModal = ref(false)
const showMilestoneForm = ref(false)

const milestoneForm = reactive<MilestoneForm>({
  title: '',
  targetDate: null,
  notes: '',
})

onMounted(async () => {
  await store.fetchGoal(goalId)
})

function goBack() {
  router.push({ name: 'goals' })
}

function navigateToEdit() {
  router.push({ name: 'goal-edit', params: { id: goalId } })
}

async function handleDelete() {
  await store.deleteGoal(goalId)
  router.push({ name: 'goals' })
}

async function handleStatusChange(status: GoalStatus) {
  await store.updateStatus(goalId, status)
}

async function handleAddMilestone() {
  if (!milestoneForm.title.trim()) return
  await store.addMilestone(goalId, { ...milestoneForm })
  milestoneForm.title = ''
  milestoneForm.targetDate = null
  milestoneForm.notes = ''
  showMilestoneForm.value = false
}

async function handleToggleMilestone(milestoneId: number) {
  await store.toggleMilestone(goalId, milestoneId)
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
  return date.toLocaleDateString('en-US', {
    weekday: 'long',
    month: 'long',
    day: 'numeric',
    year: 'numeric',
  })
}

function progressPercent(total: number, completed: number): number {
  if (total === 0) return 0
  return Math.round((completed / total) * 100)
}

const availableStatuses: GoalStatus[] = [
  'NOT_STARTED',
  'IN_PROGRESS',
  'COMPLETED',
  'PAUSED',
  'CANCELLED',
]
</script>

<template>
  <div class="detail-view">
    <div class="detail-header">
      <button class="btn-back" @click="goBack">&larr; Back to Goals</button>
    </div>

    <div v-if="store.error" class="error-banner" role="alert">
      {{ store.error }}
      <button class="error-dismiss" @click="store.clearError()">&times;</button>
    </div>

    <div v-if="store.loading" class="loading">
      <p>Loading goal...</p>
    </div>

    <template v-else-if="store.currentGoal">
      <article class="goal-detail">
        <div class="goal-top">
          <div>
            <h1 class="goal-title">{{ store.currentGoal.title }}</h1>
            <div class="goal-meta">
              <span v-if="store.currentGoal.category" class="goal-category">
                {{ store.currentGoal.category }}
              </span>
              <span v-if="store.currentGoal.targetDate" class="goal-date">
                Target: {{ formatDate(store.currentGoal.targetDate) }}
              </span>
            </div>
          </div>
          <span :class="['status-badge', statusClass(store.currentGoal.status)]">
            {{ statusLabel(store.currentGoal.status) }}
          </span>
        </div>

        <p v-if="store.currentGoal.description" class="goal-description">
          {{ store.currentGoal.description }}
        </p>

        <!-- Status controls -->
        <div class="status-controls">
          <span class="status-label">Status:</span>
          <div class="status-buttons">
            <button
              v-for="s in availableStatuses"
              :key="s"
              :class="['btn-status', statusClass(s), { active: store.currentGoal.status === s }]"
              @click="handleStatusChange(s)"
            >
              {{ statusLabel(s) }}
            </button>
          </div>
        </div>

        <!-- Progress -->
        <div v-if="store.currentGoal.totalMilestones > 0" class="progress-section">
          <div class="progress-info">
            <span class="progress-label">Progress</span>
            <span class="progress-value">
              {{ store.currentGoal.completedMilestones }}/{{
                store.currentGoal.totalMilestones
              }}
              milestones ({{
                progressPercent(
                  store.currentGoal.totalMilestones,
                  store.currentGoal.completedMilestones,
                )
              }}%)
            </span>
          </div>
          <div class="progress-bar">
            <div
              class="progress-fill"
              :style="{
                width:
                  progressPercent(
                    store.currentGoal.totalMilestones,
                    store.currentGoal.completedMilestones,
                  ) + '%',
              }"
            />
          </div>
        </div>

        <!-- Milestones -->
        <div class="milestones-section">
          <div class="milestones-header">
            <h2>Milestones</h2>
            <button class="btn btn-sm btn-primary" @click="showMilestoneForm = !showMilestoneForm">
              {{ showMilestoneForm ? 'Cancel' : '+ Add Milestone' }}
            </button>
          </div>

          <!-- Add milestone form -->
          <form
            v-if="showMilestoneForm"
            class="milestone-form"
            @submit.prevent="handleAddMilestone"
          >
            <div class="form-group">
              <input
                v-model="milestoneForm.title"
                type="text"
                class="form-input"
                placeholder="Milestone title *"
                required
              />
            </div>
            <div class="form-row">
              <div class="form-group">
                <input v-model="milestoneForm.targetDate" type="date" class="form-input" />
              </div>
              <div class="form-group flex-1">
                <input
                  v-model="milestoneForm.notes"
                  type="text"
                  class="form-input"
                  placeholder="Notes (optional)"
                />
              </div>
            </div>
            <button type="submit" class="btn btn-primary btn-sm">Add</button>
          </form>

          <!-- Milestone list -->
          <div v-if="store.currentGoal.milestones.length" class="milestone-list">
            <div
              v-for="milestone in store.currentGoal.milestones"
              :key="milestone.id"
              :class="['milestone-item', { completed: milestone.completed }]"
            >
              <button
                class="milestone-toggle"
                :title="milestone.completed ? 'Mark incomplete' : 'Mark complete'"
                @click="handleToggleMilestone(milestone.id)"
              >
                <span class="checkbox">{{ milestone.completed ? '\u2713' : '' }}</span>
              </button>
              <div class="milestone-content">
                <span class="milestone-title">{{ milestone.title }}</span>
                <span v-if="milestone.targetDate" class="milestone-date">
                  {{ formatDate(milestone.targetDate) }}
                </span>
                <span v-if="milestone.notes" class="milestone-notes">{{ milestone.notes }}</span>
              </div>
            </div>
          </div>

          <p v-else class="milestones-empty">No milestones yet. Add one to track your progress.</p>
        </div>

        <!-- Goal actions -->
        <div class="goal-actions">
          <button class="btn btn-secondary" @click="navigateToEdit">Edit Goal</button>
          <button class="btn btn-danger" @click="showDeleteModal = true">Delete Goal</button>
        </div>
      </article>
    </template>

    <!-- Delete confirmation modal -->
    <div v-if="showDeleteModal" class="modal-overlay" @click.self="showDeleteModal = false">
      <dialog class="modal" open>
        <h2>Delete Goal</h2>
        <p>
          Are you sure you want to delete this goal and all its milestones? This cannot be undone.
        </p>
        <div class="modal-actions">
          <button class="btn btn-secondary" @click="showDeleteModal = false">Cancel</button>
          <button class="btn btn-danger" @click="handleDelete">Delete</button>
        </div>
      </dialog>
    </div>
  </div>
</template>

<style scoped>
.detail-view {
  max-width: 700px;
  margin: 0 auto;
  padding: var(--space-8) var(--space-6);
}

.detail-header {
  margin-bottom: var(--space-6);
}

.btn-back {
  background: none;
  border: none;
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  cursor: pointer;
  padding: var(--space-1) 0;
}

.btn-back:hover {
  color: var(--color-primary-dark);
}

.error-banner {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: var(--color-error);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--border-radius);
  margin-bottom: var(--space-6);
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: var(--font-size-sm);
}

.error-dismiss {
  background: none;
  border: none;
  color: var(--color-error);
  font-size: var(--font-size-lg);
  cursor: pointer;
}

.loading {
  text-align: center;
  padding: var(--space-12);
}

.goal-detail {
  background: var(--color-white);
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius-lg);
  padding: var(--space-8);
}

.goal-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--space-4);
}

.goal-title {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-gray-900);
  margin-bottom: var(--space-2);
}

.goal-meta {
  display: flex;
  gap: var(--space-3);
  align-items: center;
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
  font-size: var(--font-size-sm);
  color: var(--color-gray-500);
}

.status-badge {
  font-size: var(--font-size-xs);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--border-radius-full);
  font-weight: var(--font-weight-medium);
  white-space: nowrap;
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
  color: #b45309;
}

.status-cancelled {
  background: #fef2f2;
  color: var(--color-error);
}

.goal-description {
  font-size: var(--font-size-base);
  color: var(--color-gray-700);
  line-height: 1.7;
  margin-bottom: var(--space-6);
  white-space: pre-wrap;
}

.status-controls {
  margin-bottom: var(--space-6);
  padding: var(--space-4);
  background: var(--color-gray-50);
  border-radius: var(--border-radius);
}

.status-label {
  display: block;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-gray-700);
  margin-bottom: var(--space-2);
}

.status-buttons {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.btn-status {
  font-size: var(--font-size-xs);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--border-radius-full);
  border: 1px solid transparent;
  cursor: pointer;
  transition: all var(--transition-fast);
  opacity: 0.5;
}

.btn-status.active {
  opacity: 1;
  border-color: currentColor;
}

.btn-status:hover {
  opacity: 0.8;
}

.progress-section {
  margin-bottom: var(--space-6);
}

.progress-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: var(--space-2);
}

.progress-label {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-gray-700);
}

.progress-value {
  font-size: var(--font-size-sm);
  color: var(--color-gray-500);
}

.progress-bar {
  height: 8px;
  background: var(--color-gray-200);
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--color-primary);
  border-radius: 4px;
  transition: width var(--transition-fast);
}

.milestones-section {
  margin-bottom: var(--space-6);
  padding-top: var(--space-6);
  border-top: 1px solid var(--color-gray-100);
}

.milestones-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
}

.milestones-header h2 {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-900);
}

.milestone-form {
  background: var(--color-gray-50);
  padding: var(--space-4);
  border-radius: var(--border-radius);
  margin-bottom: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.form-row {
  display: flex;
  gap: var(--space-3);
}

.flex-1 {
  flex: 1;
}

.form-input {
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-gray-300);
  border-radius: var(--border-radius);
  font-size: var(--font-size-sm);
  font-family: var(--font-sans);
  color: var(--color-gray-900);
  background: var(--color-white);
}

.form-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-50);
}

.milestone-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.milestone-item {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  padding: var(--space-3);
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius);
  background: var(--color-white);
}

.milestone-item.completed {
  opacity: 0.6;
}

.milestone-toggle {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  flex-shrink: 0;
}

.checkbox {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border: 2px solid var(--color-gray-300);
  border-radius: 4px;
  font-size: var(--font-size-xs);
  color: var(--color-primary);
  transition: all var(--transition-fast);
}

.milestone-item.completed .checkbox {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: var(--color-white);
}

.milestone-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  flex: 1;
  min-width: 0;
}

.milestone-title {
  font-size: var(--font-size-sm);
  color: var(--color-gray-900);
  font-weight: var(--font-weight-medium);
}

.milestone-item.completed .milestone-title {
  text-decoration: line-through;
  color: var(--color-gray-500);
}

.milestone-date {
  font-size: var(--font-size-xs);
  color: var(--color-gray-500);
}

.milestone-notes {
  font-size: var(--font-size-xs);
  color: var(--color-gray-500);
  font-style: italic;
}

.milestones-empty {
  text-align: center;
  color: var(--color-gray-500);
  font-size: var(--font-size-sm);
  padding: var(--space-6);
}

.goal-actions {
  display: flex;
  gap: var(--space-3);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-gray-100);
}

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

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 50;
}

.modal {
  background: var(--color-white);
  border-radius: var(--border-radius-lg);
  padding: var(--space-6);
  max-width: 400px;
  width: 90%;
}

.modal h2 {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  margin-bottom: var(--space-3);
}

.modal p {
  color: var(--color-gray-600);
  margin-bottom: var(--space-6);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
}
</style>
