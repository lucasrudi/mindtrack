import { ref } from 'vue'
import { defineStore } from 'pinia'
import api from '@/services/api'

export interface TherapistRequest {
  relationshipId: number
  therapistId: number
  therapistName: string
  therapistEmail: string
  status: string
  createdAt: string
}

export const usePatientStore = defineStore('patient', () => {
  const requests = ref<TherapistRequest[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetchRequests() {
    loading.value = true
    error.value = null
    try {
      const response = await api.get('/patient/requests')
      requests.value = response.data
    } catch {
      error.value = 'Failed to load therapist requests'
    } finally {
      loading.value = false
    }
  }

  async function acceptRequest(relationshipId: number) {
    error.value = null
    try {
      await api.post(`/patient/requests/${relationshipId}/accept`)
      requests.value = requests.value.map((r) =>
        r.relationshipId === relationshipId ? { ...r, status: 'ACTIVE' } : r,
      )
    } catch {
      error.value = 'Failed to accept request'
      throw new Error('Failed to accept request')
    }
  }

  async function rejectRequest(relationshipId: number) {
    error.value = null
    try {
      await api.post(`/patient/requests/${relationshipId}/reject`)
      requests.value = requests.value.filter((r) => r.relationshipId !== relationshipId)
    } catch {
      error.value = 'Failed to reject request'
      throw new Error('Failed to reject request')
    }
  }

  function clearError() {
    error.value = null
  }

  return {
    requests,
    loading,
    error,
    fetchRequests,
    acceptRequest,
    rejectRequest,
    clearError,
  }
})
