<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { useAppointmentStore, type AppointmentSummary } from '@/stores/appointments'

const appointmentStore = useAppointmentStore()

function formatDateTime(value: string): string {
  return new Intl.DateTimeFormat('en-US', {
    weekday: 'short',
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  }).format(new Date(value))
}

function formatRelativeTime(value: string): string {
  const now = new Date()
  const target = new Date(value)
  const diffMinutes = Math.round((target.getTime() - now.getTime()) / 60000)

  if (Math.abs(diffMinutes) < 60) {
    const hours = Math.max(1, Math.round(Math.abs(diffMinutes) / 60))
    return diffMinutes >= 0
      ? `In ${hours} hour${hours === 1 ? '' : 's'}`
      : `${hours} hour${hours === 1 ? '' : 's'} ago`
  }

  const diffDays = Math.max(1, Math.round(Math.abs(diffMinutes) / 1440))
  return diffMinutes >= 0
    ? `In ${diffDays} day${diffDays === 1 ? '' : 's'}`
    : `${diffDays} day${diffDays === 1 ? '' : 's'} ago`
}

const nextAppointment = computed<AppointmentSummary | null>(() => {
  const now = new Date()
  return (
    appointmentStore.appointments.find(
      (appointment) => appointment.status === 'SCHEDULED' && new Date(appointment.startAt) >= now,
    ) ?? null
  )
})

onMounted(async () => {
  try {
    await appointmentStore.fetchPatientAppointments()
  } catch {
    // Store-level error handles the failure state.
  }
})
</script>

<template>
  <section class="next-appointment-widget" data-testid="next-appointment-widget">
    <div class="widget-header">
      <div>
        <p class="widget-kicker">Patient dashboard</p>
        <h2 class="widget-title">Next appointment</h2>
      </div>
      <span class="widget-badge" aria-hidden="true">🗓</span>
    </div>

    <div v-if="appointmentStore.loading" class="state" data-testid="loading-state">
      <p class="state-text">Loading your appointments...</p>
    </div>

    <div v-else-if="appointmentStore.error" class="state state--error" data-testid="error-state">
      <p class="state-text">{{ appointmentStore.error }}</p>
      <button class="btn btn-secondary btn-sm" @click="appointmentStore.fetchPatientAppointments()">
        Retry
      </button>
    </div>

    <div v-else-if="nextAppointment" class="appointment-card" data-testid="appointment-card">
      <div class="appointment-card__top">
        <div>
          <p class="appointment-label">With</p>
          <h3 class="appointment-name">
            {{ nextAppointment.therapistName || 'Your therapist' }}
          </h3>
        </div>
        <span class="status-pill">Scheduled</span>
      </div>

      <p class="appointment-time">{{ formatDateTime(nextAppointment.startAt) }}</p>
      <p class="appointment-relative">{{ formatRelativeTime(nextAppointment.startAt) }}</p>
      <p class="appointment-reason">
        {{ nextAppointment.reason || 'No reason supplied for this appointment.' }}
      </p>

      <div class="widget-actions">
        <RouterLink to="/appointments" class="widget-link">View all appointments</RouterLink>
      </div>
    </div>

    <div v-else class="state state--empty" data-testid="empty-state">
      <p class="state-text">No upcoming appointments scheduled.</p>
      <RouterLink to="/appointments" class="widget-link">See your appointment history →</RouterLink>
    </div>
  </section>
</template>

<style scoped>
.next-appointment-widget {
  background:
    radial-gradient(circle at top right, rgba(99, 102, 241, 0.08), transparent 36%),
    var(--color-white);
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius-lg);
  padding: var(--space-5);
}

.widget-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-4);
  margin-bottom: var(--space-4);
}

.widget-kicker {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  color: var(--color-primary);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  margin: 0 0 var(--space-1) 0;
}

.widget-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-900);
  margin: 0;
}

.widget-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 999px;
  background: #ede9fe;
  color: var(--color-primary);
  font-size: 1.1rem;
  flex-shrink: 0;
}

.state {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  padding: var(--space-3) 0 0;
}

.state--error {
  color: var(--color-error);
}

.state--empty {
  color: var(--color-gray-500);
}

.state-text {
  margin: 0;
  font-size: var(--font-size-sm);
}

.appointment-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  padding: var(--space-4);
  border-radius: var(--border-radius);
  background: var(--color-gray-50);
  border: 1px solid var(--color-gray-200);
}

.appointment-card__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-3);
}

.appointment-label {
  margin: 0 0 2px;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-500);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.appointment-name {
  margin: 0;
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-900);
}

.status-pill {
  flex-shrink: 0;
  border-radius: 999px;
  background: #eef2ff;
  color: var(--color-primary);
  padding: 0.2rem 0.6rem;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
}

.appointment-time {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--color-gray-800);
  font-weight: var(--font-weight-medium);
}

.appointment-relative,
.appointment-reason {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--color-gray-600);
  line-height: var(--line-height-relaxed);
}

.widget-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
}

.widget-link {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-primary);
  text-decoration: none;
}

.widget-link:hover {
  text-decoration: underline;
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

.btn-secondary {
  background: var(--color-gray-100);
  color: var(--color-gray-700);
}

.btn-secondary:hover {
  background: var(--color-gray-200);
}
</style>
