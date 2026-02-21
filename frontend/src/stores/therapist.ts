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
  const currentPatient = ref<PatientDetail | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetchPatients() {
    loading.value = true
    error.value = null
    try {
      const response = await api.get('/therapist/patients')
      patients.value = response.data
    } catch (err) {
      error.value = 'Failed to load patients'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function fetchPatientDetail(patientId: number) {
    loading.value = true
    error.value = null
    try {
      const response = await api.get(`/therapist/patients/${patientId}`)
      currentPatient.value = response.data
      return response.data
    } catch (err) {
      error.value = 'Failed to load patient details'
      throw err
    } finally {
      loading.value = false
    }
  }

  function clearPatient() {
    currentPatient.value = null
  }

  function clearError() {
    error.value = null
  }

  return {
    patients,
    currentPatient,
    loading,
    error,
    fetchPatients,
    fetchPatientDetail,
    clearPatient,
    clearError,
  }
})
