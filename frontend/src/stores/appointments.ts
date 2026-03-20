import { ref } from 'vue'
import { defineStore } from 'pinia'
import api from '@/services/api'

export interface AppointmentSummary {
  id: number
  therapistId: number
  therapistName: string | null
  therapistEmail: string | null
  patientId: number
  patientName: string
  patientEmail: string
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
  const cancelling = ref<number | null>(null)
  const error = ref<string | null>(null)
  const notice = ref<string | null>(null)

  function sortAppointments(nextAppointments: AppointmentSummary[]) {
    appointments.value = [...nextAppointments].sort((left, right) =>
      left.startAt.localeCompare(right.startAt),
    )
  }

  async function fetchAppointments() {
    loading.value = true
    error.value = null
    try {
      const response = await api.get('/therapist/appointments')
      sortAppointments(response.data)
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
      sortAppointments([...appointments.value, response.data])
      notice.value = 'Appointment booked'
      return response.data
    } catch (err) {
      error.value = 'Failed to book appointment'
      throw err
    } finally {
      booking.value = false
    }
  }

  async function fetchPatientAppointments() {
    loading.value = true
    error.value = null
    try {
      const response = await api.get('/patient/appointments')
      sortAppointments(response.data)
    } catch (err) {
      error.value = 'Failed to load appointments'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function cancelAppointmentForTherapist(appointmentId: number) {
    cancelling.value = appointmentId
    error.value = null
    notice.value = null
    try {
      const response = await api.patch(`/therapist/appointments/${appointmentId}/cancel`)
      sortAppointments(
        appointments.value.map((appointment) =>
          appointment.id === appointmentId ? response.data : appointment,
        ),
      )
      notice.value = 'Appointment cancelled'
      return response.data
    } catch (err) {
      error.value = 'Failed to cancel appointment'
      throw err
    } finally {
      cancelling.value = null
    }
  }

  async function cancelAppointmentForPatient(appointmentId: number) {
    cancelling.value = appointmentId
    error.value = null
    notice.value = null
    try {
      const response = await api.patch(`/patient/appointments/${appointmentId}/cancel`)
      sortAppointments(
        appointments.value.map((appointment) =>
          appointment.id === appointmentId ? response.data : appointment,
        ),
      )
      notice.value = 'Appointment cancelled'
      return response.data
    } catch (err) {
      error.value = 'Failed to cancel appointment'
      throw err
    } finally {
      cancelling.value = null
    }
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
    fetchPatientAppointments,
    bookAppointment,
    cancelAppointmentForTherapist,
    cancelAppointmentForPatient,
    clearNotice,
    clearError,
  }
})
