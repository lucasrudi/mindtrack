import { ref } from 'vue'
import { defineStore } from 'pinia'
import api from '@/services/api'

export interface PatientSummary {
  id: number
  name: string
  email: string
  interviewCount: number
  activeGoalCount: number
  activityCount: number
  lastInterviewDate: string | null
  status?: string | null
}

export interface PatientDetail {
  patientId: number
  patientName: string
  patientEmail: string
  interviews: Array<{
    id: number
    interviewDate: string
    moodBefore: number | null
    moodAfter: number | null
    topics: string[]
    notes: string | null
    createdAt: string
  }>
  activities: Array<{
    id: number
    type: string
    name: string
    description: string | null
    active: boolean
    createdAt: string
  }>
  goals: Array<{
    id: number
    title: string
    description: string | null
    status: string
    targetDate: string | null
    totalMilestones: number
    completedMilestones: number
    createdAt: string
  }>
  sharedJournalEntries: Array<{
    id: number
    entryDate: string
    title: string | null
    content: string | null
    mood: number | null
    tags: string[]
    createdAt: string
  }>
}

export const useTherapistStore = defineStore('therapist', () => {
  const patients = ref<PatientSummary[]>([])
  const pendingPatients = ref<PatientSummary[]>([])
  const currentPatient = ref<PatientDetail | null>(null)
  const patientsLoading = ref(false)
  const detailLoading = ref(false)
  const patientsError = ref<string | null>(null)
  const detailError = ref<string | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetchPatients() {
    patientsLoading.value = true
    loading.value = true
    patientsError.value = null
    error.value = null
    try {
      const response = await api.get('/therapist/patients')
      patients.value = response.data
    } catch (err) {
      patientsError.value = 'Failed to load patients'
      error.value = patientsError.value
      throw err
    } finally {
      patientsLoading.value = false
      loading.value = patientsLoading.value || detailLoading.value
    }
  }

  async function fetchPendingPatients() {
    loading.value = true
    error.value = null
    try {
      const response = await api.get('/therapist/patients/pending')
      pendingPatients.value = response.data
    } catch (err) {
      error.value = 'Failed to load pending requests'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function fetchPatientDetail(patientId: number) {
    detailLoading.value = true
    loading.value = true
    detailError.value = null
    error.value = null
    try {
      const response = await api.get(`/therapist/patients/${patientId}`)
      currentPatient.value = response.data
      return response.data
    } catch (err) {
      detailError.value = 'Failed to load patient details'
      error.value = detailError.value
      throw err
    } finally {
      detailLoading.value = false
      loading.value = patientsLoading.value || detailLoading.value
    }
  }

  async function requestPatient(patientEmail: string) {
    error.value = null
    try {
      const response = await api.post('/invites/request', { patientEmail })
      return response.data as { token: string; url: string }
    } catch (err) {
      error.value = 'Failed to send patient request'
      throw err
    }
  }

  function clearPatient() {
    currentPatient.value = null
    detailError.value = null
  }

  function clearError() {
    patientsError.value = null
    detailError.value = null
    error.value = null
  }

  return {
    patients,
    pendingPatients,
    currentPatient,
    loading,
    error,
    patientsLoading,
    detailLoading,
    patientsError,
    detailError,
    fetchPatients,
    fetchPendingPatients,
    fetchPatientDetail,
    requestPatient,
    clearPatient,
    clearError,
  }
})
