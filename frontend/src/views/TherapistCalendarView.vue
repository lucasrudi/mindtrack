<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useTherapistStore } from '@/stores/therapist'
import {
  useAppointmentStore,
  type AppointmentSummary,
  type CancellationScope,
} from '@/stores/appointments'
import AppointmentCancelModal from '@/components/AppointmentCancelModal.vue'

const therapistStore = useTherapistStore()
const appointmentStore = useAppointmentStore()

const CALENDAR_COLORS = ['#2563eb', '#10b981', '#f97316', '#ef4444', '#8b5cf6', '#0f766e']

const form = reactive({
  patientId: '',
  date: '',
  time: '09:00',
  durationMinutes: 50,
  reason: '',
  notes: '',
  calendarColor: CALENDAR_COLORS[0],
  repeatWeekly: false,
  recurrenceCount: 12,
  recurrenceEndDate: '',
})

const submitError = ref<string | null>(null)

const cancelModal = reactive({
  show: false,
  appointmentId: null as number | null,
  isSeries: false,
})

function openCancelModal(appointment: AppointmentSummary) {
  cancelModal.appointmentId = appointment.id
  cancelModal.isSeries = appointment.seriesId !== null
  cancelModal.show = true
}

function closeCancelModal() {
  cancelModal.show = false
  cancelModal.appointmentId = null
  cancelModal.isSeries = false
}

async function confirmCancel(scope: CancellationScope) {
  if (cancelModal.appointmentId === null) return
  try {
    await appointmentStore.cancelAppointment(cancelModal.appointmentId, scope)
  } catch {
    // Store-level error handles the failure state.
  }
  closeCancelModal()
}

onMounted(async () => {
  form.date = formatInputDate(new Date())

  try {
    await Promise.all([therapistStore.fetchPatients(), appointmentStore.fetchAppointments()])
    if (!form.patientId && therapistStore.patients.length > 0) {
      form.patientId = String(therapistStore.patients[0].id)
    }
  } catch {
    // Store-level errors handle the failure state.
  }
})

const selectedPatient = computed(
  () => therapistStore.patients.find((patient) => String(patient.id) === form.patientId) || null,
)

watch(
  selectedPatient,
  (patient) => {
    form.calendarColor = patient?.calendarColor || defaultColorForPatient(patient?.id ?? 0)
  },
  { immediate: true },
)

const upcomingAppointments = computed(() => {
  const now = new Date()
  return appointmentStore.appointments
    .filter((appointment) => new Date(appointment.startAt) >= now)
    .sort((left, right) => left.startAt.localeCompare(right.startAt))
})

const pastAppointments = computed(() => {
  const now = new Date()
  return appointmentStore.appointments
    .filter((appointment) => new Date(appointment.startAt) < now)
    .sort((left, right) => right.startAt.localeCompare(left.startAt))
})

const upcomingGroups = computed(() => groupAppointments(upcomingAppointments.value))
const pastGroups = computed(() => groupAppointments(pastAppointments.value))

const upcomingCount = computed(() => upcomingAppointments.value.length)
const pastCount = computed(() => pastAppointments.value.length)
const bookedPatients = computed(
  () => new Set(appointmentStore.appointments.map((appointment) => appointment.patientId)).size,
)

function formatInputDate(date: Date): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function formatTimeRange(startAt: string, endAt: string): string {
  const formatter = new Intl.DateTimeFormat('en-US', {
    hour: 'numeric',
    minute: '2-digit',
  })
  return `${formatter.format(new Date(startAt))} - ${formatter.format(new Date(endAt))}`
}

function formatStatus(status: string): string {
  return status.replaceAll('_', ' ').toLowerCase()
}

function statusClass(status: string): string {
  switch (status) {
    case 'SCHEDULED':
      return 'status-scheduled'
    case 'COMPLETED':
      return 'status-completed'
    case 'CANCELLED':
      return 'status-cancelled'
    case 'NO_SHOW':
      return 'status-no-show'
    default:
      return 'status-scheduled'
  }
}

function groupAppointments(appointments: AppointmentSummary[]) {
  const groups = new Map<string, AppointmentSummary[]>()

  for (const appointment of appointments) {
    const key = appointment.startAt.slice(0, 10)
    const existing = groups.get(key) || []
    groups.set(key, [...existing, appointment])
  }

  return Array.from(groups.entries()).map(([date, items]) => ({
    date,
    label: new Intl.DateTimeFormat('en-US', {
      weekday: 'long',
      month: 'short',
      day: 'numeric',
    }).format(new Date(`${date}T12:00:00`)),
    items,
  }))
}

function defaultColorForPatient(patientId: number): string {
  return CALENDAR_COLORS[(patientId - 1) % CALENDAR_COLORS.length] || CALENDAR_COLORS[0]
}

async function persistCalendarColor(color: string) {
  if (!selectedPatient.value) {
    return
  }

  const updated = await therapistStore.setPatientCalendarColor(selectedPatient.value.id, color)
  appointmentStore.updatePatientCalendarColor(updated.id, updated.calendarColor || color)
}

async function chooseCalendarColor(color: string) {
  form.calendarColor = color
  await persistCalendarColor(color)
}

function buildDateTime(date: string, time: string): string {
  const raw = new Date(`${date}T${time}:00`)
  const year = raw.getFullYear()
  const month = String(raw.getMonth() + 1).padStart(2, '0')
  const day = String(raw.getDate()).padStart(2, '0')
  const hours = String(raw.getHours()).padStart(2, '0')
  const minutes = String(raw.getMinutes()).padStart(2, '0')
  const seconds = String(raw.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`
}

async function submitBooking() {
  submitError.value = null

  if (!form.patientId || !form.date || !form.time || !form.reason.trim()) {
    submitError.value = 'Choose a patient, date, time, and reason.'
    return
  }

  try {
    if (selectedPatient.value?.calendarColor !== form.calendarColor) {
      await persistCalendarColor(form.calendarColor)
    }
    await appointmentStore.bookAppointment(Number(form.patientId), {
      startAt: buildDateTime(form.date, form.time),
      durationMinutes: form.durationMinutes,
      reason: form.reason.trim(),
      notes: form.notes.trim(),
      recurrence: form.repeatWeekly ? 'WEEKLY' : 'NONE',
      recurrenceCount: form.repeatWeekly ? form.recurrenceCount : undefined,
      recurrenceEndDate:
        form.repeatWeekly && form.recurrenceEndDate ? form.recurrenceEndDate : undefined,
    })
    form.reason = ''
    form.notes = ''
    form.time = '09:00'
    form.durationMinutes = 50
    form.repeatWeekly = false
    form.recurrenceCount = 12
    form.recurrenceEndDate = ''
  } catch {
    // Store-level error handles the failure state.
  }
}
</script>

<template>
  <div class="calendar-view">
    <section class="hero-panel">
      <div class="hero-copy">
        <p class="eyebrow">Therapist calendar</p>
        <h1>Appointments that feel easy to scan, book, and revisit.</h1>
        <p class="subtitle">
          Keep upcoming sessions visible, review past visits, and reserve the next slot without
          losing context.
        </p>
      </div>

      <div class="hero-stats">
        <div class="stat-card">
          <span class="stat-value">{{ upcomingCount }}</span>
          <span class="stat-label">Upcoming</span>
        </div>
        <div class="stat-card">
          <span class="stat-value">{{ pastCount }}</span>
          <span class="stat-label">Past</span>
        </div>
        <div class="stat-card">
          <span class="stat-value">{{ bookedPatients }}</span>
          <span class="stat-label">Patients scheduled</span>
        </div>
      </div>
    </section>

    <div v-if="appointmentStore.error" class="notice notice-error">
      <p>{{ appointmentStore.error }}</p>
      <button class="btn btn-secondary" @click="appointmentStore.clearError()">Dismiss</button>
    </div>

    <div v-if="appointmentStore.notice" class="notice notice-success">
      <p>{{ appointmentStore.notice }}</p>
      <button class="btn btn-secondary" @click="appointmentStore.clearNotice()">Dismiss</button>
    </div>

    <div v-if="submitError" class="notice notice-warning">
      <p>{{ submitError }}</p>
    </div>

    <div class="calendar-layout">
      <section class="panel booking-panel">
        <div class="panel-header">
          <div>
            <p class="panel-kicker">Book a session</p>
            <h2>Reserve time with a patient</h2>
          </div>
        </div>

        <form class="booking-form" @submit.prevent="submitBooking">
          <label class="field">
            <span>Patient</span>
            <select v-model="form.patientId" :disabled="therapistStore.patients.length === 0">
              <option value="" disabled>Select a patient</option>
              <option
                v-for="patient in therapistStore.patients"
                :key="patient.id"
                :value="String(patient.id)"
              >
                {{ patient.name }} · {{ patient.email }}
              </option>
            </select>
          </label>

          <label class="field">
            <span>Calendar color</span>
            <div class="color-picker">
              <input
                v-model="form.calendarColor"
                type="color"
                :disabled="!selectedPatient"
                @change="chooseCalendarColor(form.calendarColor)"
              />
              <div class="color-options">
                <button
                  v-for="color in CALENDAR_COLORS"
                  :key="color"
                  type="button"
                  class="color-chip"
                  :class="{ 'color-chip--active': form.calendarColor === color }"
                  :aria-label="`Choose calendar color ${color}`"
                  :style="{ '--swatch-color': color }"
                  :disabled="!selectedPatient"
                  @click="chooseCalendarColor(color)"
                ></button>
              </div>
            </div>
            <small class="color-hint">Saved per patient and reused on the next booking.</small>
          </label>

          <div class="field-grid">
            <label class="field">
              <span>Date</span>
              <input v-model="form.date" type="date" />
            </label>
            <label class="field">
              <span>Start time</span>
              <input v-model="form.time" type="time" />
            </label>
          </div>

          <label class="field">
            <span>Duration</span>
            <select v-model.number="form.durationMinutes">
              <option :value="30">30 min</option>
              <option :value="45">45 min</option>
              <option :value="50">50 min (standard)</option>
              <option :value="60">60 min</option>
            </select>
          </label>

          <label class="field field-inline">
            <input v-model="form.repeatWeekly" type="checkbox" />
            <span>Repeat weekly</span>
          </label>

          <div v-if="form.repeatWeekly" class="recurrence-options">
            <label class="field">
              <span>End after (weeks)</span>
              <input v-model.number="form.recurrenceCount" type="number" min="1" max="104" />
            </label>
            <label class="field">
              <span>Or end by date</span>
              <input v-model="form.recurrenceEndDate" type="date" />
            </label>
          </div>

          <label class="field">
            <span>Reason</span>
            <input
              v-model="form.reason"
              type="text"
              placeholder="Follow-up, intake, medication review..."
            />
          </label>

          <label class="field">
            <span>Notes</span>
            <textarea
              v-model="form.notes"
              rows="4"
              placeholder="Optional context for the session..."
            ></textarea>
          </label>

          <button
            class="btn btn-primary btn-full"
            type="submit"
            :disabled="appointmentStore.booking"
          >
            {{ appointmentStore.booking ? 'Booking...' : 'Book appointment' }}
          </button>
        </form>
      </section>

      <section class="panel agenda-panel">
        <div class="panel-header">
          <div>
            <p class="panel-kicker">Agenda</p>
            <h2>Upcoming and past sessions</h2>
          </div>
        </div>

        <div v-if="appointmentStore.loading || therapistStore.loading" class="empty-state">
          Loading appointments...
        </div>

        <div v-else class="agenda-columns">
          <div class="agenda-column">
            <h3>Upcoming</h3>
            <div v-if="upcomingGroups.length === 0" class="empty-state compact">
              No upcoming appointments.
            </div>
            <div v-else class="day-stack">
              <article v-for="group in upcomingGroups" :key="group.date" class="day-group">
                <div class="day-header">{{ group.label }}</div>
                <div
                  v-for="appointment in group.items"
                  :key="appointment.id"
                  class="appointment-card"
                  :style="{
                    '--appointment-accent':
                      appointment.calendarColor || defaultColorForPatient(appointment.patientId),
                  }"
                >
                  <div class="appointment-topline">
                    <span class="appointment-time">
                      {{ formatTimeRange(appointment.startAt, appointment.endAt) }}
                    </span>
                    <span :class="['status-pill', statusClass(appointment.status)]">
                      {{ formatStatus(appointment.status) }}
                    </span>
                  </div>
                  <h4>
                    <span class="patient-swatch" aria-hidden="true"></span>
                    {{ appointment.patientName }}
                    <span v-if="appointment.seriesId" class="series-badge" title="Recurring"
                      >&#8635;</span
                    >
                  </h4>
                  <p class="appointment-reason">{{ appointment.reason || 'No reason supplied' }}</p>
                  <button
                    v-if="appointment.status === 'SCHEDULED'"
                    class="btn-cancel-appointment"
                    type="button"
                    @click="openCancelModal(appointment)"
                  >
                    Cancel
                  </button>
                </div>
              </article>
            </div>
          </div>

          <div class="agenda-column">
            <h3>Past</h3>
            <div v-if="pastGroups.length === 0" class="empty-state compact">
              No past appointments.
            </div>
            <div v-else class="day-stack">
              <article v-for="group in pastGroups" :key="group.date" class="day-group">
                <div class="day-header">{{ group.label }}</div>
                <div
                  v-for="appointment in group.items"
                  :key="appointment.id"
                  class="appointment-card muted"
                  :style="{
                    '--appointment-accent':
                      appointment.calendarColor || defaultColorForPatient(appointment.patientId),
                  }"
                >
                  <div class="appointment-topline">
                    <span class="appointment-time">
                      {{ formatTimeRange(appointment.startAt, appointment.endAt) }}
                    </span>
                    <span :class="['status-pill', statusClass(appointment.status)]">
                      {{ formatStatus(appointment.status) }}
                    </span>
                  </div>
                  <h4>
                    <span class="patient-swatch" aria-hidden="true"></span>
                    {{ appointment.patientName }}
                    <span v-if="appointment.seriesId" class="series-badge" title="Recurring"
                      >&#8635;</span
                    >
                  </h4>
                  <p class="appointment-reason">{{ appointment.reason || 'No reason supplied' }}</p>
                </div>
              </article>
            </div>
          </div>
        </div>
      </section>
    </div>

    <AppointmentCancelModal
      :show="cancelModal.show"
      :is-series="cancelModal.isSeries"
      @confirm="confirmCancel"
      @cancel="closeCancelModal"
    />
  </div>
</template>

<style scoped>
.calendar-view {
  position: relative;
  padding: var(--space-8) 0 var(--space-16);
  color: #e5eef7;
}

.calendar-view::before {
  content: '';
  position: absolute;
  inset: -40px 0 auto;
  height: 420px;
  background:
    radial-gradient(circle at top left, rgba(245, 158, 11, 0.24), transparent 40%),
    radial-gradient(circle at top right, rgba(15, 23, 42, 0.9), rgba(15, 23, 42, 0.96) 60%),
    linear-gradient(135deg, #07111f 0%, #0b1c2d 45%, #13243a 100%);
  z-index: -1;
}

.hero-panel,
.panel {
  border: 1px solid rgba(148, 163, 184, 0.18);
  box-shadow: 0 24px 60px rgb(2 6 23 / 0.22);
}

.hero-panel {
  display: grid;
  grid-template-columns: minmax(0, 1.8fr) minmax(280px, 0.9fr);
  gap: var(--space-8);
  padding: var(--space-8);
  background: rgba(15, 23, 42, 0.84);
  border-radius: 28px;
  backdrop-filter: blur(18px);
}

.eyebrow {
  margin-bottom: var(--space-3);
  color: #fbbf24;
  text-transform: uppercase;
  letter-spacing: 0.18em;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
}

.hero-copy h1 {
  max-width: 14ch;
  margin: 0;
  font-size: clamp(2.2rem, 4vw, 4.1rem);
  line-height: 0.96;
  color: var(--color-white);
}

.subtitle {
  max-width: 62ch;
  margin-top: var(--space-4);
  color: rgba(226, 232, 240, 0.92);
  line-height: 1.7;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-3);
  align-content: end;
}

.stat-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  padding: var(--space-4);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.stat-value {
  font-size: clamp(1.8rem, 3vw, 2.8rem);
  font-weight: var(--font-weight-bold);
  color: var(--color-white);
}

.stat-label {
  color: rgba(226, 232, 240, 0.9);
  font-size: var(--font-size-sm);
}

.notice {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-4);
  margin-top: var(--space-6);
  padding: var(--space-4) var(--space-5);
  border-radius: 18px;
}

.notice-error {
  background: rgba(127, 29, 29, 0.35);
  border: 1px solid rgba(248, 113, 113, 0.28);
}

.notice-success {
  background: rgba(6, 78, 59, 0.34);
  border: 1px solid rgba(52, 211, 153, 0.22);
}

.notice-warning {
  background: rgba(120, 53, 15, 0.35);
  border: 1px solid rgba(251, 191, 36, 0.22);
}

.calendar-layout {
  display: grid;
  grid-template-columns: minmax(320px, 0.95fr) minmax(0, 1.4fr);
  gap: var(--space-6);
  margin-top: var(--space-6);
}

.panel {
  padding: var(--space-6);
  border-radius: 24px;
  background: rgba(10, 16, 28, 0.88);
  backdrop-filter: blur(14px);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--space-5);
}

.panel-kicker {
  margin-bottom: var(--space-2);
  color: #93c5fd;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
}

.panel h2,
.agenda-column h3,
.appointment-card h4 {
  margin: 0;
  color: var(--color-white);
}

.booking-form {
  display: grid;
  gap: var(--space-4);
}

.field {
  display: grid;
  gap: var(--space-2);
  color: rgba(226, 232, 240, 0.92);
}

.field span {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
}

.field input,
.field select,
.field textarea {
  width: 100%;
  padding: var(--space-3) var(--space-4);
  color: var(--color-white);
  background: rgba(15, 23, 42, 0.88);
  border: 1px solid rgba(148, 163, 184, 0.22);
  border-radius: 16px;
  transition:
    border-color var(--transition-fast),
    box-shadow var(--transition-fast);
}

.field input:focus,
.field select:focus,
.field textarea:focus {
  outline: none;
  border-color: rgba(251, 191, 36, 0.7);
  box-shadow: 0 0 0 3px rgba(251, 191, 36, 0.14);
}

.color-picker {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.color-picker input[type='color'] {
  width: 64px;
  height: 44px;
  padding: 0;
  border: 0;
  border-radius: 14px;
  overflow: hidden;
  background: transparent;
}

.color-options {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.color-chip {
  width: 26px;
  height: 26px;
  padding: 0;
  border: 2px solid transparent;
  border-radius: 999px;
  background: var(--swatch-color);
  box-shadow: 0 0 0 1px rgba(148, 163, 184, 0.15);
}

.color-chip--active {
  border-color: var(--color-white);
  box-shadow:
    0 0 0 2px rgba(255, 255, 255, 0.1),
    0 0 0 4px color-mix(in srgb, var(--swatch-color) 42%, transparent);
}

.color-hint {
  color: rgba(226, 232, 240, 0.86);
}

.field-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
}

.btn-full {
  width: 100%;
}

.agenda-columns {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-5);
}

.agenda-column {
  display: grid;
  gap: var(--space-4);
}

.day-stack {
  display: grid;
  gap: var(--space-4);
}

.day-group {
  display: grid;
  gap: var(--space-3);
}

.day-header {
  color: #f8fafc;
  font-weight: var(--font-weight-semibold);
  letter-spacing: 0.02em;
}

.appointment-card {
  display: grid;
  gap: var(--space-2);
  padding: var(--space-4);
  border-radius: 18px;
  background: rgba(15, 23, 42, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.16);
  border-left: 4px solid var(--appointment-accent, #60a5fa);
}

.appointment-card.muted {
  background: rgba(15, 23, 42, 0.64);
}

.appointment-card h4 {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.patient-swatch {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: var(--appointment-accent, #60a5fa);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--appointment-accent, #60a5fa) 18%, transparent);
}

.appointment-topline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
}

.appointment-time {
  color: #fde68a;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
}

.appointment-reason {
  margin: 0;
  color: rgba(226, 232, 240, 0.9);
  line-height: 1.5;
}

.status-pill {
  padding: 0.25rem 0.7rem;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  border-radius: var(--border-radius-full);
  text-transform: capitalize;
}

.status-scheduled {
  color: #fef3c7;
  background: rgba(217, 119, 6, 0.16);
}

.status-completed {
  color: #bbf7d0;
  background: rgba(22, 163, 74, 0.16);
}

.status-cancelled {
  color: #fecaca;
  background: rgba(220, 38, 38, 0.16);
}

.status-no-show {
  color: #f5d0fe;
  background: rgba(147, 51, 234, 0.16);
}

.empty-state {
  padding: var(--space-6);
  color: rgba(226, 232, 240, 0.9);
  text-align: center;
  border: 1px dashed rgba(148, 163, 184, 0.26);
  border-radius: 18px;
}

.empty-state.compact {
  padding: var(--space-4);
}

.field-inline {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: var(--space-2);
}

.field-inline input[type='checkbox'] {
  width: auto;
  height: 18px;
  accent-color: #fbbf24;
}

.recurrence-options {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
  padding: var(--space-4);
  border-radius: 14px;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(148, 163, 184, 0.14);
}

.series-badge {
  margin-left: var(--space-1);
  font-size: var(--font-size-sm);
  color: #93c5fd;
  opacity: 0.8;
}

.btn-cancel-appointment {
  align-self: start;
  margin-top: var(--space-2);
  padding: 0.25rem 0.7rem;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  color: #fecaca;
  background: rgba(220, 38, 38, 0.12);
  border: 1px solid rgba(220, 38, 38, 0.3);
  border-radius: var(--border-radius-full);
  cursor: pointer;
  transition:
    background var(--transition-fast),
    border-color var(--transition-fast);
}

.btn-cancel-appointment:hover {
  background: rgba(220, 38, 38, 0.22);
  border-color: rgba(220, 38, 38, 0.5);
}

@media (max-width: 960px) {
  .hero-panel,
  .calendar-layout,
  .agenda-columns {
    grid-template-columns: 1fr;
  }

  .hero-stats {
    grid-template-columns: 1fr;
  }

  .field-grid,
  .recurrence-options {
    grid-template-columns: 1fr;
  }
}
</style>
