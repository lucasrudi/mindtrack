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
  createdAt: string
  updatedAt: string
}

export interface AppointmentBookingRequest {
  startAt: string
  endAt: string
  reason: string
  notes: string
}

export const useAppointmentStore = defineStore('appointments', () => {
  const appointments = ref<AppointmentSummary[]>([])
  const loading = ref(false)
  const booking = ref(false)
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
      notice.value = 'Appointment booked'
      return response.data
    } catch (err) {
      error.value = 'Failed to book appointment'
      throw err
    } finally {
      booking.value = false
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
    error,
    notice,
    fetchAppointments,
    bookAppointment,
    updatePatientCalendarColor,
    clearNotice,
    clearError,
  }
})
