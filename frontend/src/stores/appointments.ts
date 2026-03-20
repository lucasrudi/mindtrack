import { ref } from 'vue'
import { defineStore } from 'pinia'
import api from '@/services/api'

export interface AppointmentSummary {
  id: number
  therapistId: number
  patientId: number
  patientName: string
  patientEmail: string
  calendarColor: string | null
  startAt: string
  endAt: string
  status: 'SCHEDULED' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW'
  reason: string | null
  notes: string | null
  durationMinutes: number
  recurrenceRule: string | null
  seriesId: string | null
  seriesIndex: number | null
  createdAt: string
  updatedAt: string
}

export type RecurrenceType = 'NONE' | 'WEEKLY'
export type CancellationScope = 'SINGLE' | 'THIS_AND_FOLLOWING' | 'ALL_IN_SERIES'

export interface AppointmentBookingRequest {
  startAt: string
  durationMinutes: number
  reason: string
  notes: string
  recurrence: RecurrenceType
  recurrenceCount?: number
  recurrenceEndDate?: string
}

export const useAppointmentStore = defineStore('appointments', () => {
  const appointments = ref<AppointmentSummary[]>([])
  const loading = ref(false)
  const booking = ref(false)
  const cancelling = ref(false)
  const error = ref<string | null>(null)
  const notice = ref<string | null>(null)

  async function fetchAppointments() {
    loading.value = true
    error.value = null
    try {
      const response = await api.get('/therapist/appointments')
      appointments.value = response.data
    } catch (err) {
      error.value = 'Failed to load appointments'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function bookAppointment(patientId: number, request: AppointmentBookingRequest) {
    booking.value = true
    error.value = null
    notice.value = null
    try {
      const response = await api.post(`/therapist/patients/${patientId}/appointments`, request)
      appointments.value = [...appointments.value, response.data].sort((left, right) =>
        left.startAt.localeCompare(right.startAt),
      )
      notice.value =
        request.recurrence === 'WEEKLY' ? 'Recurring series booked' : 'Appointment booked'
      return response.data
    } catch (err) {
      error.value = 'Failed to book appointment'
      throw err
    } finally {
      booking.value = false
    }
  }

  async function cancelAppointment(appointmentId: number, scope: CancellationScope) {
    cancelling.value = true
    error.value = null
    notice.value = null
    try {
      await api.delete(`/therapist/appointments/${appointmentId}`, { params: { scope } })
      if (scope === 'ALL_IN_SERIES') {
        const target = appointments.value.find((a) => a.id === appointmentId)
        const seriesId = target?.seriesId ?? null
        appointments.value = appointments.value.map((a) =>
          a.seriesId !== null && a.seriesId === seriesId
            ? { ...a, status: 'CANCELLED' as const }
            : a,
        )
      } else if (scope === 'THIS_AND_FOLLOWING') {
        const target = appointments.value.find((a) => a.id === appointmentId)
        const seriesId = target?.seriesId ?? null
        const fromIndex = target?.seriesIndex ?? null
        appointments.value = appointments.value.map((a) => {
          if (
            a.seriesId !== null &&
            a.seriesId === seriesId &&
            a.seriesIndex !== null &&
            fromIndex !== null &&
            a.seriesIndex >= fromIndex
          ) {
            return { ...a, status: 'CANCELLED' as const }
          }
          return a
        })
      } else {
        appointments.value = appointments.value.map((a) =>
          a.id === appointmentId ? { ...a, status: 'CANCELLED' as const } : a,
        )
      }
      notice.value = 'Appointment cancelled'
    } catch (err) {
      error.value = 'Failed to cancel appointment'
      throw err
    } finally {
      cancelling.value = false
    }
  }

  function updatePatientCalendarColor(patientId: number, calendarColor: string) {
    appointments.value = appointments.value.map((appointment) =>
      appointment.patientId === patientId ? { ...appointment, calendarColor } : appointment,
    )
  }

  function clearNotice() {
    notice.value = null
  }

  function clearError() {
    error.value = null
  }

  return {
    appointments,
    loading,
    booking,
    cancelling,
    error,
    notice,
    fetchAppointments,
    bookAppointment,
    cancelAppointment,
    updatePatientCalendarColor,
    clearNotice,
    clearError,
  }
})
