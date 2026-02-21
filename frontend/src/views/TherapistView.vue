<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useTherapistStore } from '@/stores/therapist'

const store = useTherapistStore()
const activeTab = ref<'interviews' | 'activities' | 'goals' | 'journal'>('interviews')

onMounted(async () => {
  try {
    await store.fetchPatients()
  } catch {
    // Error handled by store
  }
})

async function selectPatient(patientId: number) {
  activeTab.value = 'interviews'
  try {
    await store.fetchPatientDetail(patientId)
  } catch {
    // Error handled by store
  }
}

function backToList() {
  store.clearPatient()
}

function formatDate(dateStr: string | null): string {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

function moodLabel(mood: number | null): string {
  if (mood === null) return '-'
  return `${mood}/10`
}

function statusClass(status: string): string {
  switch (status) {
    case 'COMPLETED':
      return 'status-completed'
    case 'IN_PROGRESS':
      return 'status-in-progress'
    case 'ABANDONED':
      return 'status-abandoned'
    default:
      return 'status-not-started'
  }
}
</script>

<template>
  <div class="therapist-view">
    <header class="page-header">
      <div>
        <h1>Patient Dashboard</h1>
        <p class="subtitle">View your patients' progress and records</p>
      </div>
    </header>

    <div v-if="store.error" class="error-message">
      <p>{{ store.error }}</p>
      <button class="btn btn-secondary" @click="store.clearError()">Dismiss</button>
    </div>

    <!-- Patient List -->
    <div v-if="!store.currentPatient" class="patient-list-section">
      <div v-if="store.loading" class="loading">
        <p>Loading patients...</p>
      </div>

      <div v-else-if="store.patients.length === 0" class="empty-state">
        <p>No patients assigned to you yet.</p>
      </div>

      <table v-else class="data-table">
        <thead>
          <tr>
            <th>Name</th>
            <th>Email</th>
            <th>Interviews</th>
            <th>Active Goals</th>
            <th>Activities</th>
            <th>Last Interview</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="patient in store.patients"
            :key="patient.id"
            class="patient-row"
            @click="selectPatient(patient.id)"
          >
            <td class="cell-name">{{ patient.name }}</td>
            <td class="cell-email">{{ patient.email }}</td>
            <td>{{ patient.interviewCount }}</td>
            <td>{{ patient.activeGoalCount }}</td>
            <td>{{ patient.activityCount }}</td>
            <td class="cell-date">{{ formatDate(patient.lastInterviewDate) }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Patient Detail -->
    <div v-else class="patient-detail-section">
      <button class="btn btn-secondary back-btn" @click="backToList">
        &larr; Back to patients
      </button>

      <div class="patient-header">
        <h2>{{ store.currentPatient.patientName }}</h2>
        <span class="patient-email">{{ store.currentPatient.patientEmail }}</span>
      </div>

      <div class="tabs">
        <button
          :class="['tab', { active: activeTab === 'interviews' }]"
          @click="activeTab = 'interviews'"
        >
          Interviews ({{ store.currentPatient.interviews.length }})
        </button>
        <button
          :class="['tab', { active: activeTab === 'activities' }]"
          @click="activeTab = 'activities'"
        >
          Activities ({{ store.currentPatient.activities.length }})
        </button>
        <button :class="['tab', { active: activeTab === 'goals' }]" @click="activeTab = 'goals'">
          Goals ({{ store.currentPatient.goals.length }})
        </button>
        <button
          :class="['tab', { active: activeTab === 'journal' }]"
          @click="activeTab = 'journal'"
        >
          Shared Journal ({{ store.currentPatient.sharedJournalEntries.length }})
        </button>
      </div>

      <!-- Interviews Tab -->
      <div v-if="activeTab === 'interviews'" class="tab-content">
        <div v-if="store.currentPatient.interviews.length === 0" class="empty-tab">
          No interviews recorded.
        </div>
        <table v-else class="data-table">
          <thead>
            <tr>
              <th>Date</th>
              <th>Mood Before</th>
              <th>Mood After</th>
              <th>Topics</th>
              <th>Notes</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="interview in store.currentPatient.interviews" :key="interview.id">
              <td class="cell-date">{{ formatDate(interview.interviewDate) }}</td>
              <td>{{ moodLabel(interview.moodBefore) }}</td>
              <td>{{ moodLabel(interview.moodAfter) }}</td>
              <td>
                <span v-for="topic in interview.topics" :key="topic" class="tag">
                  {{ topic }}
                </span>
                <span v-if="!interview.topics.length">-</span>
              </td>
              <td class="cell-notes">{{ interview.notes || '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Activities Tab -->
      <div v-if="activeTab === 'activities'" class="tab-content">
        <div v-if="store.currentPatient.activities.length === 0" class="empty-tab">
          No activities recorded.
        </div>
        <table v-else class="data-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Type</th>
              <th>Description</th>
              <th>Status</th>
              <th>Created</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="activity in store.currentPatient.activities" :key="activity.id">
              <td class="cell-name">{{ activity.name }}</td>
              <td>
                <span class="tag tag-type">{{ activity.type }}</span>
              </td>
              <td class="cell-notes">{{ activity.description || '-' }}</td>
              <td>
                <span :class="['status-badge', activity.active ? 'enabled' : 'disabled']">
                  {{ activity.active ? 'Active' : 'Inactive' }}
                </span>
              </td>
              <td class="cell-date">{{ formatDate(activity.createdAt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Goals Tab -->
      <div v-if="activeTab === 'goals'" class="tab-content">
        <div v-if="store.currentPatient.goals.length === 0" class="empty-tab">
          No goals recorded.
        </div>
        <table v-else class="data-table">
          <thead>
            <tr>
              <th>Title</th>
              <th>Status</th>
              <th>Target Date</th>
              <th>Milestones</th>
              <th>Created</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="goal in store.currentPatient.goals" :key="goal.id">
              <td class="cell-name">{{ goal.title }}</td>
              <td>
                <span :class="['status-badge', statusClass(goal.status)]">
                  {{ goal.status.replace('_', ' ') }}
                </span>
              </td>
              <td class="cell-date">{{ formatDate(goal.targetDate) }}</td>
              <td>{{ goal.completedMilestones }}/{{ goal.totalMilestones }}</td>
              <td class="cell-date">{{ formatDate(goal.createdAt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Shared Journal Tab -->
      <div v-if="activeTab === 'journal'" class="tab-content">
        <div v-if="store.currentPatient.sharedJournalEntries.length === 0" class="empty-tab">
          No shared journal entries.
        </div>
        <div v-else class="journal-cards">
          <div
            v-for="entry in store.currentPatient.sharedJournalEntries"
            :key="entry.id"
            class="journal-card"
          >
            <div class="journal-card-header">
              <span class="journal-date">{{ formatDate(entry.entryDate) }}</span>
              <span v-if="entry.mood !== null" class="journal-mood">Mood: {{ entry.mood }}/10</span>
            </div>
            <h3 v-if="entry.title" class="journal-title">{{ entry.title }}</h3>
            <p v-if="entry.content" class="journal-content">{{ entry.content }}</p>
            <div v-if="entry.tags.length" class="journal-tags">
              <span v-for="tag in entry.tags" :key="tag" class="tag">{{ tag }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.therapist-view {
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

.loading {
  text-align: center;
  padding: var(--space-12);
  color: var(--color-gray-500);
}

.empty-state {
  text-align: center;
  padding: var(--space-12);
  color: var(--color-gray-500);
  background: var(--color-white);
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius-lg);
}

.empty-tab {
  text-align: center;
  padding: var(--space-8);
  color: var(--color-gray-500);
  font-size: var(--font-size-sm);
}

/* Data Table */
.data-table {
  width: 100%;
  border-collapse: collapse;
  background: var(--color-white);
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius-lg);
  overflow: hidden;
}

.data-table th {
  text-align: left;
  padding: var(--space-3) var(--space-4);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-500);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  background: var(--color-gray-50);
  border-bottom: 1px solid var(--color-gray-200);
}

.data-table td {
  padding: var(--space-3) var(--space-4);
  font-size: var(--font-size-sm);
  color: var(--color-gray-700);
  border-bottom: 1px solid var(--color-gray-100);
}

.data-table tr:last-child td {
  border-bottom: none;
}

.patient-row {
  cursor: pointer;
  transition: background-color var(--transition-fast);
}

.patient-row:hover {
  background-color: var(--color-primary-50);
}

.cell-name {
  font-weight: var(--font-weight-medium);
  color: var(--color-gray-900);
}

.cell-email {
  color: var(--color-gray-500);
}

.cell-date {
  white-space: nowrap;
  color: var(--color-gray-500);
}

.cell-notes {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Patient Detail */
.back-btn {
  margin-bottom: var(--space-4);
}

.patient-header {
  display: flex;
  align-items: baseline;
  gap: var(--space-3);
  margin-bottom: var(--space-6);
}

.patient-header h2 {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-gray-900);
  margin: 0;
}

.patient-email {
  font-size: var(--font-size-sm);
  color: var(--color-gray-500);
}

/* Tabs */
.tabs {
  display: flex;
  border-bottom: 2px solid var(--color-gray-200);
  margin-bottom: var(--space-6);
}

.tab {
  padding: var(--space-3) var(--space-5);
  border: none;
  background: none;
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

/* Tags */
.tag {
  display: inline-block;
  font-size: var(--font-size-xs);
  padding: var(--space-1) var(--space-2);
  background: var(--color-primary-50);
  color: var(--color-primary);
  border-radius: var(--border-radius-full);
  font-weight: var(--font-weight-medium);
  margin-right: var(--space-1);
}

.tag-type {
  background: #f0f9ff;
  color: #0369a1;
}

/* Status Badges */
.status-badge {
  display: inline-block;
  font-size: var(--font-size-xs);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--border-radius-full);
  font-weight: var(--font-weight-medium);
  text-transform: capitalize;
}

.status-badge.enabled {
  background: #f0fdf4;
  color: var(--color-success);
}

.status-badge.disabled {
  background: var(--color-gray-100);
  color: var(--color-gray-500);
}

.status-badge.status-completed {
  background: #f0fdf4;
  color: var(--color-success);
}

.status-badge.status-in-progress {
  background: #fef3c7;
  color: #d97706;
}

.status-badge.status-abandoned {
  background: #fef2f2;
  color: var(--color-error);
}

.status-badge.status-not-started {
  background: var(--color-gray-100);
  color: var(--color-gray-500);
}

/* Journal Cards */
.journal-cards {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.journal-card {
  background: var(--color-white);
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius-lg);
  padding: var(--space-5);
}

.journal-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-2);
}

.journal-date {
  font-size: var(--font-size-sm);
  color: var(--color-gray-500);
}

.journal-mood {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
  color: var(--color-primary);
  background: var(--color-primary-50);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--border-radius-full);
}

.journal-title {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-900);
  margin: 0 0 var(--space-2);
}

.journal-content {
  font-size: var(--font-size-sm);
  color: var(--color-gray-700);
  line-height: 1.6;
  margin: 0 0 var(--space-3);
}

.journal-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-1);
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

.btn-secondary {
  background: var(--color-gray-100);
  color: var(--color-gray-700);
}

.btn-secondary:hover {
  background: var(--color-gray-200);
}
</style>
