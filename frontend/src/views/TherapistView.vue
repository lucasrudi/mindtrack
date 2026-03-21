<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useTherapistStore } from '@/stores/therapist'

const store = useTherapistStore()
const activeTab = ref<'interviews' | 'activities' | 'goals' | 'journal'>('interviews')
const requestEmail = ref('')
const requestLink = ref<string | null>(null)
const requestMessage = ref<string | null>(null)
const requesting = ref(false)
const copyFeedback = ref(false)
const selectedPatientId = ref<number | null>(null)

const overviewCards = computed(() => {
  const patients = store.patients
  const totalPatients = patients.length
  const totalInterviews = patients.reduce((sum, patient) => sum + patient.interviewCount, 0)
  const totalActiveGoals = patients.reduce((sum, patient) => sum + patient.activeGoalCount, 0)
  const totalActivities = patients.reduce((sum, patient) => sum + patient.activityCount, 0)

  return [
    {
      label: 'Assigned patients',
      value: totalPatients,
      detail: 'Active therapist relationships',
    },
    {
      label: 'Interviews',
      value: totalInterviews,
      detail: 'Across all assigned patients',
    },
    {
      label: 'Active goals',
      value: totalActiveGoals,
      detail: 'In progress right now',
    },
    {
      label: 'Activities',
      value: totalActivities,
      detail: 'Tracked across the caseload',
    },
  ]
})

const overviewSummary = computed(() => {
  const patients = store.patients
  if (!patients.length) {
    return 'No patient data yet'
  }

  const totalInterviews = patients.reduce((sum, patient) => sum + patient.interviewCount, 0)
  const averageInterviews = totalInterviews / patients.length
  const latestInterviewDates = patients
    .map((patient) => patient.lastInterviewDate)
    .filter((date): date is string => Boolean(date))
    .sort()
  const latestInterview = latestInterviewDates.length
    ? latestInterviewDates[latestInterviewDates.length - 1]
    : null

  const interviewText =
    averageInterviews === 1
      ? '1 interview per patient'
      : `${averageInterviews.toFixed(1)} interviews per patient`
  const latestText = latestInterview
    ? `Most recent interview ${formatDate(latestInterview)}`
    : 'No interviews logged yet'

  return `${interviewText} · ${latestText}`
})

const selectedPatientSummary = computed(() => {
  const patient = store.currentPatient
  if (!patient) {
    return []
  }

  const activeGoals = patient.goals.filter((goal) => goal.status === 'IN_PROGRESS').length
  const sharedEntries = patient.sharedJournalEntries.length
  const recentInterview = patient.interviews[0]?.interviewDate ?? null

  return [
    {
      label: 'Interviews',
      value: patient.interviews.length,
      detail: recentInterview ? `Latest on ${formatDate(recentInterview)}` : 'No interviews yet',
    },
    {
      label: 'Active goals',
      value: activeGoals,
      detail: `${patient.goals.length} total goals`,
    },
    {
      label: 'Activities',
      value: patient.activities.length,
      detail: `${patient.activities.filter((activity) => activity.active).length} active`,
    },
    {
      label: 'Shared journal',
      value: sharedEntries,
      detail: sharedEntries ? 'Visible to the therapist' : 'Nothing shared yet',
    },
  ]
})

const selectedPatientName = computed(() => store.currentPatient?.patientName ?? 'Select a patient')
const selectedPatientEmail = computed(
  () => store.currentPatient?.patientEmail ?? 'Choose a patient to review their notes',
)

onMounted(async () => {
  try {
    await store.fetchPatients()
    await store.fetchPendingPatients()
  } catch {
    // Error handled by store
  }
})

async function sendPatientRequest() {
  if (!requestEmail.value.trim()) {
    requestMessage.value = 'Enter a patient email address.'
    return
  }
  requesting.value = true
  requestMessage.value = null
  requestLink.value = null
  try {
    const response = await store.requestPatient(requestEmail.value.trim())
    requestLink.value = response.url
    requestMessage.value = 'Request sent. Share the link with your patient.'
    requestEmail.value = ''
    await store.fetchPendingPatients()
  } catch {
    requestMessage.value = 'Could not send the request.'
  } finally {
    requesting.value = false
  }
}

async function selectPatient(patientId: number) {
  selectedPatientId.value = patientId
  activeTab.value = 'interviews'
  store.clearPatient()

  try {
    await store.fetchPatientDetail(patientId)
  } catch {
    // Error handled by store
  }
}

function backToOverview() {
  selectedPatientId.value = null
  activeTab.value = 'interviews'
  store.clearPatient()
}

function isSelectedPatient(patientId: number): boolean {
  return selectedPatientId.value === patientId
}

async function copyLink() {
  if (!requestLink.value) return
  try {
    await navigator.clipboard.writeText(requestLink.value)
    copyFeedback.value = true
    setTimeout(() => {
      copyFeedback.value = false
    }, 2000)
  } catch {
    copyFeedback.value = false
  }
}

function formatDate(dateStr: string | null): string {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

function formatMetric(value: number): string {
  return new Intl.NumberFormat('en-US').format(value)
}

function moodLabel(mood: number | null): string {
  if (mood === null) return '-'
  return `${mood}/10`
}

function statusClass(status: string): string {
  switch (status) {
    case 'ACTIVE':
      return 'status-active'
    case 'PENDING':
      return 'status-pending'
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
        <h1>Therapist Dashboard</h1>
        <p class="subtitle">Aggregate caseload overview with patient-specific drill-down</p>
      </div>
      <div class="header-chip">
        <span class="header-chip__label">Context</span>
        <span class="header-chip__value">Overview + selected patient</span>
      </div>
    </header>

    <div v-if="store.patientsError" class="error-message">
      <p>{{ store.patientsError }}</p>
      <button class="btn btn-secondary" @click="store.clearError()">Dismiss</button>
    </div>

    <section v-if="!store.currentPatient" class="request-panel">
      <div class="request-panel-copy">
        <p class="section-label">Requests</p>
        <h2>Request a patient</h2>
        <p>Send a therapist request to a specific user by email.</p>
      </div>
      <div class="request-form">
        <input
          v-model="requestEmail"
          type="email"
          class="form-input"
          placeholder="patient@example.com"
        />
        <button class="btn btn-primary" :disabled="requesting" @click="sendPatientRequest">
          {{ requesting ? 'Sending...' : 'Send request' }}
        </button>
      </div>
      <p v-if="requestMessage" class="request-message">{{ requestMessage }}</p>
      <div v-if="requestLink" class="request-link-row">
        <a class="request-link" :href="requestLink" target="_blank">Open request link</a>
        <button class="btn btn-secondary btn-sm" data-testid="copy-link-btn" @click="copyLink">
          {{ copyFeedback ? 'Copied!' : 'Copy link' }}
        </button>
      </div>
    </section>

    <section v-if="!store.currentPatient" class="pending-panel">
      <div class="panel-header">
        <div>
          <p class="section-label">Requests</p>
          <h2>Pending requests</h2>
        </div>
        <span class="section-count">{{ store.pendingPatients.length }}</span>
      </div>
      <div v-if="store.pendingPatients.length === 0" class="empty-state">
        <p>No pending requests right now.</p>
      </div>
      <table v-else class="data-table">
        <thead>
          <tr>
            <th>Name</th>
            <th>Email</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="patient in store.pendingPatients" :key="patient.id">
            <td class="cell-name">{{ patient.name }}</td>
            <td class="cell-email">{{ patient.email }}</td>
            <td>
              <span :class="['status-badge', statusClass(patient.status || 'PENDING')]">
                {{ patient.status || 'PENDING' }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </section>

    <section class="overview-panel" aria-label="Therapist overview">
      <div class="panel-header">
        <div>
          <p class="section-label">Overview</p>
          <h2>Assigned patient snapshot</h2>
        </div>
        <p class="panel-note">{{ overviewSummary }}</p>
      </div>

      <div v-if="store.patientsLoading && store.patients.length === 0" class="loading">
        <p>Loading patient overview...</p>
      </div>

      <div v-else class="overview-cards">
        <article v-for="card in overviewCards" :key="card.label" class="overview-card">
          <span class="card-label">{{ card.label }}</span>
          <span class="card-value">{{ formatMetric(card.value) }}</span>
          <span class="card-detail">{{ card.detail }}</span>
        </article>
      </div>
    </section>

    <div class="dashboard-grid">
      <section class="patient-list-panel">
        <div class="panel-header">
          <div>
            <p class="section-label">Patients</p>
            <h2>Assigned patients</h2>
          </div>
          <p class="panel-note">{{ formatMetric(store.patients.length) }} active assignments</p>
        </div>

        <div v-if="store.patientsLoading && store.patients.length === 0" class="loading">
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
              <th>Goals</th>
              <th>Activities</th>
              <th>Last interview</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="patient in store.patients"
              :key="patient.id"
              :class="['patient-row', { selected: isSelectedPatient(patient.id) }]"
              @click="selectPatient(patient.id)"
            >
              <td class="cell-name">{{ patient.name }}</td>
              <td class="cell-email">{{ patient.email }}</td>
              <td>{{ formatMetric(patient.interviewCount) }}</td>
              <td>{{ formatMetric(patient.activeGoalCount) }}</td>
              <td>{{ formatMetric(patient.activityCount) }}</td>
              <td class="cell-date">{{ formatDate(patient.lastInterviewDate) }}</td>
            </tr>
          </tbody>
        </table>
      </section>

      <section class="patient-detail-panel">
        <template v-if="store.detailLoading">
          <div class="loading detail-loading">
            <p>Loading selected patient...</p>
          </div>
        </template>

        <template v-else-if="store.detailError">
          <div class="detail-error">
            <p>{{ store.detailError }}</p>
            <button class="btn btn-secondary btn-sm" @click="store.clearError()">Dismiss</button>
          </div>
        </template>

        <template v-else-if="store.currentPatient">
          <div class="patient-header">
            <div>
              <p class="section-label">Selected patient</p>
              <h2>{{ selectedPatientName }}</h2>
              <span class="patient-email">{{ selectedPatientEmail }}</span>
            </div>
            <button class="btn btn-secondary back-btn" @click="backToOverview">
              Back to overview
            </button>
          </div>

          <div class="patient-scope-banner">
            Showing patient-specific insights only. Aggregate metrics remain in the overview above.
          </div>

          <div class="snapshot-cards">
            <article v-for="card in selectedPatientSummary" :key="card.label" class="snapshot-card">
              <span class="card-label">{{ card.label }}</span>
              <span class="card-value">{{ formatMetric(card.value) }}</span>
              <span class="card-detail">{{ card.detail }}</span>
            </article>
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
            <button
              :class="['tab', { active: activeTab === 'goals' }]"
              @click="activeTab = 'goals'"
            >
              Goals ({{ store.currentPatient.goals.length }})
            </button>
            <button
              :class="['tab', { active: activeTab === 'journal' }]"
              @click="activeTab = 'journal'"
            >
              Shared Journal ({{ store.currentPatient.sharedJournalEntries.length }})
            </button>
          </div>

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

          <div v-else-if="activeTab === 'activities'" class="tab-content">
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

          <div v-else-if="activeTab === 'goals'" class="tab-content">
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

          <div v-else-if="activeTab === 'journal'" class="tab-content">
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
                  <span v-if="entry.mood !== null" class="journal-mood"
                    >Mood: {{ entry.mood }}/10</span
                  >
                </div>
                <h3 v-if="entry.title" class="journal-title">{{ entry.title }}</h3>
                <p v-if="entry.content" class="journal-content">{{ entry.content }}</p>
                <div v-if="entry.tags.length" class="journal-tags">
                  <span v-for="tag in entry.tags" :key="tag" class="tag">{{ tag }}</span>
                </div>
              </div>
            </div>
          </div>
        </template>

        <template v-else>
          <div class="empty-state detail-empty">
            <p>Select a patient to view their individual insights.</p>
          </div>
        </template>
      </section>
    </div>
  </div>
</template>

<style scoped>
.therapist-view {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-8) var(--space-6) var(--space-10);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: var(--space-4);
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

.header-chip,
.patient-scope-banner {
  border-radius: var(--border-radius-full);
  padding: var(--space-2) var(--space-4);
  background: var(--color-primary-50);
  color: var(--color-primary);
  font-size: var(--font-size-sm);
}

.header-chip {
  display: inline-flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
}

.header-chip__label,
.section-label {
  font-size: var(--font-size-xs);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  font-weight: var(--font-weight-semibold);
}

.header-chip__value {
  font-weight: var(--font-weight-medium);
}

.request-panel {
  display: grid;
  gap: var(--space-4);
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: end;
  background: linear-gradient(135deg, var(--color-white), var(--color-gray-50));
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius-lg);
}

.overview-panel,
.pending-panel,
.patient-list-panel,
.patient-detail-panel {
  background: var(--color-white);
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius-lg);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}

.request-panel,
.pending-panel,
.overview-panel {
  padding: var(--space-5);
  margin-bottom: var(--space-6);
}

.request-panel-copy h2,
.panel-header h2 {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-900);
  margin: 0 0 var(--space-1);
}

.request-panel-copy p {
  margin: 0;
  color: var(--color-gray-500);
}

.request-form {
  display: flex;
  gap: var(--space-3);
  align-items: center;
}

.request-message {
  grid-column: 1 / -1;
  margin: 0;
  color: var(--color-gray-600);
}

.request-link {
  grid-column: 1 / -1;
  color: var(--color-primary);
  font-weight: var(--font-weight-medium);
}

.section-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 2rem;
  padding: 0 var(--space-2);
  border-radius: var(--border-radius-full);
  background: var(--color-gray-100);
  color: var(--color-gray-700);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
}

.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(0, 0.95fr);
  gap: var(--space-6);
  align-items: start;
}

.patient-list-panel,
.patient-detail-panel {
  padding: var(--space-5);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: var(--space-4);
  margin-bottom: var(--space-4);
}

.panel-header h2 {
  margin: 0;
  font-size: var(--font-size-xl);
  color: var(--color-gray-900);
}

.panel-note {
  margin: 0;
  color: var(--color-gray-500);
  font-size: var(--font-size-sm);
  text-align: right;
}

.overview-cards,
.snapshot-cards {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--space-4);
}

.overview-card,
.snapshot-card {
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius-lg);
  padding: var(--space-4);
  background: linear-gradient(180deg, var(--color-white), var(--color-gray-50));
}

.card-label {
  display: block;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  letter-spacing: 0.05em;
  text-transform: uppercase;
  color: var(--color-gray-500);
}

.card-value {
  display: block;
  margin-top: var(--space-2);
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-gray-900);
}

.card-detail {
  display: block;
  margin-top: var(--space-1);
  color: var(--color-gray-500);
  font-size: var(--font-size-sm);
  line-height: 1.5;
}

.loading,
.empty-state,
.empty-tab,
.detail-error {
  text-align: center;
  padding: var(--space-8);
  color: var(--color-gray-500);
}

.detail-loading {
  min-height: 280px;
  display: grid;
  place-items: center;
}

.detail-empty {
  min-height: 280px;
  display: grid;
  place-items: center;
}

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

.patient-row.selected {
  background: var(--color-primary-50);
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
  max-width: 220px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.patient-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-4);
  margin-bottom: var(--space-4);
}

.patient-header h2 {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-gray-900);
  margin: 0;
}

.patient-email {
  display: inline-block;
  margin-top: var(--space-1);
  font-size: var(--font-size-sm);
  color: var(--color-gray-500);
}

.patient-scope-banner {
  margin-bottom: var(--space-5);
}

.tabs {
  display: flex;
  border-bottom: 2px solid var(--color-gray-200);
  margin: var(--space-5) 0;
  overflow-x: auto;
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
  white-space: nowrap;
}

.tab.active {
  color: var(--color-primary);
  border-bottom-color: var(--color-primary);
}

.tab:hover:not(.active) {
  color: var(--color-gray-700);
}

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

.status-badge.status-active {
  background: #ecfeff;
  color: #0f766e;
}

.status-badge.status-pending {
  background: #fef3c7;
  color: #92400e;
}

.status-badge.status-in-progress {
  background: #fef3c7;
  color: #b45309;
}

.status-badge.status-abandoned {
  background: #fef2f2;
  color: var(--color-error);
}

.status-badge.status-not-started {
  background: var(--color-gray-100);
  color: var(--color-gray-500);
}

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
  gap: var(--space-2);
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

.btn-sm {
  padding: var(--space-2) var(--space-3);
  font-size: var(--font-size-xs);
}

@media (max-width: 1100px) {
  .overview-cards,
  .snapshot-cards {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .therapist-view {
    padding: var(--space-6) var(--space-4) var(--space-8);
  }

  .page-header,
  .panel-header,
  .patient-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .panel-note {
    text-align: left;
  }

  .overview-cards,
  .snapshot-cards {
    grid-template-columns: 1fr;
  }
}
</style>
