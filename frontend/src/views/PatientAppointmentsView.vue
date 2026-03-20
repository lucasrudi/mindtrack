<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useAppointmentStore } from '@/stores/appointments'

const appointmentStore = useAppointmentStore()

onMounted(async () => {
  try {
    await appointmentStore.fetchPatientAppointments()
  } catch {
    // Store-level error handles the failure state.
  }
})

const upcomingAppointments = computed(() => {
  const now = new Date()
  return appointmentStore.appointments.filter((appointment) => new Date(appointment.startAt) >= now)
})

const pastAppointments = computed(() => {
  const now = new Date()
  return appointmentStore.appointments.filter((appointment) => new Date(appointment.startAt) < now)
})

function formatDateTime(value: string): string {
  return new Intl.DateTimeFormat('en-US', {
    weekday: 'short',
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  }).format(new Date(value))
}

function formatStatus(status: string): string {
  return status.replaceAll('_', ' ').toLowerCase()
}

async function cancelAppointment(appointmentId: number) {
  try {
    await appointmentStore.cancelAppointmentForPatient(appointmentId)
  } catch {
    // Store-level error handles the failure state.
  }
}
</script>

<template>
  <div class="patient-appointments-view">
    <header class="page-header">
      <div>
        <h1>My Appointments</h1>
        <p class="subtitle">Track upcoming sessions and cancel if plans change.</p>
      </div>
    </header>

    <div v-if="appointmentStore.error" class="notice notice-error">
      <p>{{ appointmentStore.error }}</p>
      <button class="btn btn-secondary" @click="appointmentStore.clearError()">Dismiss</button>
    </div>

    <div v-if="appointmentStore.notice" class="notice notice-success">
      <p>{{ appointmentStore.notice }}</p>
      <button class="btn btn-secondary" @click="appointmentStore.clearNotice()">Dismiss</button>
    </div>

    <div v-if="appointmentStore.loading" class="empty-state">Loading appointments...</div>

    <div v-else class="appointments-layout">
      <section class="panel">
        <div class="panel-header">
          <h2>Upcoming</h2>
        </div>
        <div v-if="upcomingAppointments.length === 0" class="empty-state">
          No upcoming appointments scheduled.
        </div>
        <div v-else class="appointments-list">
          <article
            v-for="appointment in upcomingAppointments"
            :key="appointment.id"
            class="appointment-card"
          >
            <div class="status-row">
              <strong>{{ appointment.therapistName || 'Therapist' }}</strong>
              <span class="status-pill">{{ formatStatus(appointment.status) }}</span>
            </div>
            <p>{{ formatDateTime(appointment.startAt) }}</p>
            <p>{{ appointment.reason || 'No reason supplied' }}</p>
            <button
              v-if="appointment.status === 'SCHEDULED'"
              class="btn btn-secondary"
              :disabled="appointmentStore.cancelling === appointment.id"
              @click="cancelAppointment(appointment.id)"
            >
              {{
                appointmentStore.cancelling === appointment.id
                  ? 'Cancelling...'
                  : 'Cancel appointment'
              }}
            </button>
          </article>
        </div>
      </section>

      <section class="panel">
        <div class="panel-header">
          <h2>Past</h2>
        </div>
        <div v-if="pastAppointments.length === 0" class="empty-state">
          No past appointments yet.
        </div>
        <div v-else class="appointments-list">
          <article
            v-for="appointment in pastAppointments"
            :key="appointment.id"
            class="appointment-card appointment-card--muted"
          >
            <div class="status-row">
              <strong>{{ appointment.therapistName || 'Therapist' }}</strong>
              <span class="status-pill">{{ formatStatus(appointment.status) }}</span>
            </div>
            <p>{{ formatDateTime(appointment.startAt) }}</p>
            <p>{{ appointment.reason || 'No reason supplied' }}</p>
          </article>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.appointments-layout {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: var(--space-6);
}

.panel {
  background: var(--color-white);
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius-lg);
  padding: var(--space-6);
}

.panel-header {
  margin-bottom: var(--space-4);
}

.appointments-list {
  display: grid;
  gap: var(--space-4);
}

.appointment-card {
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius);
  padding: var(--space-4);
  background: #fafaf7;
}

.appointment-card--muted {
  opacity: 0.8;
}

.status-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
}

.status-pill {
  border-radius: 999px;
  background: var(--color-gray-100);
  color: var(--color-gray-700);
  padding: 0.15rem 0.55rem;
  font-size: var(--font-size-xs);
  text-transform: capitalize;
}

.notice {
  margin-bottom: var(--space-4);
}

.empty-state {
  color: var(--color-gray-500);
}
</style>
