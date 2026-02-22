import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import api from '@/services/api'

export interface Interview {
  id: number
  interviewDate: string
  moodBefore: number | null
  moodAfter: number | null
  topics: string[]
  medicationChanges: string | null
  recommendations: string | null
  notes: string | null
  hasAudio: boolean
  transcriptionText: string | null
  audioExpiresAt: string | null
  createdAt: string
  updatedAt: string
}

export interface AudioResponse {
  audioUrl: string
  transcriptionText: string | null
  audioExpiresAt: string | null
}

export interface InterviewForm {
  interviewDate: string
  moodBefore: number | null
  moodAfter: number | null
  topics: string[]
  medicationChanges: string
  recommendations: string
  notes: string
}

export const useInterviewsStore = defineStore('interviews', () => {
  const interviews = ref<Interview[]>([])
  const currentInterview = ref<Interview | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  const sortedInterviews = computed(() =>
    [...interviews.value].sort(
      (a, b) => new Date(b.interviewDate).getTime() - new Date(a.interviewDate).getTime(),
    ),
  )

  async function fetchInterviews() {
    loading.value = true
    error.value = null
    try {
      const response = await api.get('/interviews')
      interviews.value = response.data
    } catch (err) {
      error.value = 'Failed to load interviews'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function fetchInterview(id: number) {
    loading.value = true
    error.value = null
    try {
      const response = await api.get(`/interviews/${id}`)
      currentInterview.value = response.data
      return response.data
    } catch (err) {
      error.value = 'Failed to load interview'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function createInterview(form: InterviewForm) {
    loading.value = true
    error.value = null
    try {
      const response = await api.post('/interviews', form)
      interviews.value.unshift(response.data)
      return response.data
    } catch (err) {
      error.value = 'Failed to create interview'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function updateInterview(id: number, form: InterviewForm) {
    loading.value = true
    error.value = null
    try {
      const response = await api.put(`/interviews/${id}`, form)
      const index = interviews.value.findIndex((i) => i.id === id)
      if (index !== -1) {
        interviews.value[index] = response.data
      }
      currentInterview.value = response.data
      return response.data
    } catch (err) {
      error.value = 'Failed to update interview'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function deleteInterview(id: number) {
    loading.value = true
    error.value = null
    try {
      await api.delete(`/interviews/${id}`)
      interviews.value = interviews.value.filter((i) => i.id !== id)
      if (currentInterview.value?.id === id) {
        currentInterview.value = null
      }
    } catch (err) {
      error.value = 'Failed to delete interview'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function uploadAudio(id: number, file: File): Promise<AudioResponse> {
    loading.value = true
    error.value = null
    try {
      const formData = new FormData()
      formData.append('file', file)
      const response = await api.post(`/interviews/${id}/audio`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      if (currentInterview.value?.id === id) {
        currentInterview.value.hasAudio = true
        currentInterview.value.transcriptionText = response.data.transcriptionText
        currentInterview.value.audioExpiresAt = response.data.audioExpiresAt
      }
      return response.data
    } catch (err) {
      error.value = 'Failed to upload audio'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function getAudioUrl(id: number): Promise<AudioResponse> {
    error.value = null
    try {
      const response = await api.get(`/interviews/${id}/audio`)
      return response.data
    } catch (err) {
      error.value = 'Failed to load audio'
      throw err
    }
  }

  async function deleteAudio(id: number): Promise<void> {
    loading.value = true
    error.value = null
    try {
      await api.delete(`/interviews/${id}/audio`)
      if (currentInterview.value?.id === id) {
        currentInterview.value.hasAudio = false
        currentInterview.value.transcriptionText = null
        currentInterview.value.audioExpiresAt = null
      }
    } catch (err) {
      error.value = 'Failed to delete audio'
      throw err
    } finally {
      loading.value = false
    }
  }

  function clearError() {
    error.value = null
  }

  return {
    interviews,
    currentInterview,
    loading,
    error,
    sortedInterviews,
    fetchInterviews,
    fetchInterview,
    createInterview,
    updateInterview,
    deleteInterview,
    uploadAudio,
    getAudioUrl,
    deleteAudio,
    clearError,
  }
})
