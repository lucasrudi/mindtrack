<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useActivitiesStore, type ActivityLogForm } from '@/stores/activities'

const router = useRouter()
const store = useActivitiesStore()

const activeTab = ref<'checklist' | 'activities'>('checklist')
const today = new Date().toISOString().split('T')[0]
const checklistDate = ref(today)

onMounted(() => {
  store.fetchActivities()
  store.fetchChecklist(checklistDate.value)
})

function onDateChange() {
  store.fetchChecklist(checklistDate.value)
}

async function toggleChecklistItem(activityId: number, currentCompleted: boolean) {
  const form: ActivityLogForm = {
    logDate: checklistDate.value,
    completed: !currentCompleted,
    notes: '',
    moodRating: null,
  }
  await store.logActivity(activityId, form)
}

async function toggleActivityActive(id: number) {
  await store.toggleActive(id)
}

function navigateToNew() {
  router.push({ name: 'activity-new' })
}

function navigateToEdit(id: number) {
  router.push({ name: 'activity-edit', params: { id } })
}

const typeLabels: Record<string, string> = {
  EXERCISE: 'Exercise',
  MEDITATION: 'Meditation',
  SOCIAL: 'Social',
  THERAPY: 'Therapy',
  MEDICATION: 'Medication',
  HOBBY: 'Hobby',
  SELF_CARE: 'Self Care',
  OTHER: 'Other',
}
</script>

<template>
  <div class="activities-view">
    <header class="page-header">
      <div>
        <h1>Activities</h1>
        <p class="subtitle">Track your daily habits and therapeutic homework</p>
      </div>
      <button class="btn btn-primary" @click="navigateToNew">+ New Activity</button>
    </header>

    <div class="tabs">
      <button
        :class="['tab', { active: activeTab === 'checklist' }]"
        @click="activeTab = 'checklist'"
      >
        Daily Checklist
      </button>
      <button
        :class="['tab', { active: activeTab === 'activities' }]"
        @click="activeTab = 'activities'"
      >
        All Activities
      </button>
    </div>

    <div v-if="store.error" class="error-message">
      <p>{{ store.error }}</p>
      <button class="btn btn-secondary" @click="store.clearError()">Dismiss</button>
    </div>

    <!-- Daily Checklist Tab -->
    <div v-if="activeTab === 'checklist'" class="checklist-panel">
      <div class="date-picker-row">
        <label for="checklist-date">Date:</label>
        <input
          id="checklist-date"
          v-model="checklistDate"
          type="date"
          class="form-input"
          @change="onDateChange"
        />
      </div>

      <div v-if="store.loading" class="loading">
        <p>Loading checklist...</p>
      </div>

      <div v-else-if="!store.checklist.length" class="empty-state">
        <h2>No active activities</h2>
        <p>Create activities to start tracking your daily progress.</p>
        <button class="btn btn-primary" @click="navigateToNew">Create an activity</button>
      </div>

      <div v-else class="checklist-list">
        <div
          v-for="item in store.checklist"
          :key="item.activityId"
          :class="['checklist-item', { completed: item.completed }]"
        >
          <button
            class="check-btn"
            :aria-label="`Mark ${item.activityName} as ${item.completed ? 'incomplete' : 'complete'}`"
            @click="toggleChecklistItem(item.activityId, item.completed)"
          >
            <span v-if="item.completed" class="check-icon">✓</span>
            <span v-else class="check-icon empty">○</span>
          </button>
          <div class="checklist-info">
            <span class="checklist-name">{{ item.activityName }}</span>
            <span class="checklist-type">{{
              typeLabels[item.activityType] || item.activityType
            }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- All Activities Tab -->
    <div v-if="activeTab === 'activities'" class="activities-panel">
      <div v-if="store.loading && !store.activities.length" class="loading">
        <p>Loading activities...</p>
      </div>

      <div v-else-if="!store.activities.length" class="empty-state">
        <h2>No activities yet</h2>
        <p>Create your first activity to start tracking.</p>
        <button class="btn btn-primary" @click="navigateToNew">Create an activity</button>
      </div>

      <template v-else>
        <div v-if="store.activeActivities.length" class="activity-section">
          <h2 class="section-title">Active</h2>
          <div class="activity-list">
            <div
              v-for="activity in store.activeActivities"
              :key="activity.id"
              class="activity-card"
            >
              <div class="activity-info">
                <span class="activity-name">{{ activity.name }}</span>
                <span class="activity-type-badge">{{
                  typeLabels[activity.type] || activity.type
                }}</span>
                <span v-if="activity.frequency" class="activity-freq">{{
                  activity.frequency
                }}</span>
              </div>
              <p v-if="activity.description" class="activity-desc">{{ activity.description }}</p>
              <div class="activity-actions">
                <button class="btn-link" @click="navigateToEdit(activity.id)">Edit</button>
                <button class="btn-link" @click="toggleActivityActive(activity.id)">
                  Deactivate
                </button>
              </div>
            </div>
          </div>
        </div>

        <div v-if="store.inactiveActivities.length" class="activity-section">
          <h2 class="section-title">Inactive</h2>
          <div class="activity-list">
            <div
              v-for="activity in store.inactiveActivities"
              :key="activity.id"
              class="activity-card inactive"
            >
              <div class="activity-info">
                <span class="activity-name">{{ activity.name }}</span>
                <span class="activity-type-badge">{{
                  typeLabels[activity.type] || activity.type
                }}</span>
              </div>
              <div class="activity-actions">
                <button class="btn-link" @click="toggleActivityActive(activity.id)">
                  Reactivate
                </button>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.activities-view {
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

.tabs {
  display: flex;
  gap: var(--space-1);
  margin-bottom: var(--space-6);
  border-bottom: 2px solid var(--color-gray-200);
}

.tab {
  padding: var(--space-3) var(--space-5);
  background: none;
  border: none;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-gray-500);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  transition: all var(--transition-fast);
}

.tab.active {
  color: var(--color-primary);
  border-bottom-color: var(--color-primary);
}

.tab:hover:not(.active) {
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

/* Checklist styles */
.date-picker-row {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-6);
}

.date-picker-row label {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-gray-700);
}

.form-input {
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-gray-300);
  border-radius: var(--border-radius);
  font-size: var(--font-size-sm);
}

.checklist-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.checklist-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  background: var(--color-white);
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius);
  transition: all var(--transition-fast);
}

.checklist-item.completed {
  background: var(--color-gray-50);
}

.checklist-item.completed .checklist-name {
  text-decoration: line-through;
  color: var(--color-gray-400);
}

.check-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: var(--font-size-xl);
  padding: 0;
  line-height: 1;
}

.check-icon {
  color: var(--color-success);
}

.check-icon.empty {
  color: var(--color-gray-300);
}

.checklist-info {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.checklist-name {
  font-weight: var(--font-weight-medium);
  color: var(--color-gray-900);
}

.checklist-type {
  font-size: var(--font-size-xs);
  color: var(--color-gray-500);
  background: var(--color-gray-100);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--border-radius-full);
}

/* Activities list styles */
.activity-section {
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

.activity-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.activity-card {
  padding: var(--space-4);
  background: var(--color-white);
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius-lg);
}

.activity-card.inactive {
  opacity: 0.6;
}

.activity-info {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-2);
}

.activity-name {
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-900);
}

.activity-type-badge {
  font-size: var(--font-size-xs);
  color: var(--color-primary);
  background: var(--color-primary-50);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--border-radius-full);
  font-weight: var(--font-weight-medium);
}

.activity-freq {
  font-size: var(--font-size-xs);
  color: var(--color-gray-500);
}

.activity-desc {
  font-size: var(--font-size-sm);
  color: var(--color-gray-600);
  margin-bottom: var(--space-3);
}

.activity-actions {
  display: flex;
  gap: var(--space-4);
}

.btn-link {
  background: none;
  border: none;
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  cursor: pointer;
  padding: 0;
}

.btn-link:hover {
  color: var(--color-primary-dark);
}
</style>
