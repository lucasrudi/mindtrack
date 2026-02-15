import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import api from '@/services/api'

export type ActivityType =
  | 'EXERCISE'
  | 'MEDITATION'
  | 'SOCIAL'
  | 'THERAPY'
  | 'MEDICATION'
  | 'HOBBY'
  | 'SELF_CARE'
  | 'OTHER'

export interface Activity {
  id: number
  type: ActivityType
  name: string
  description: string | null
  frequency: string | null
  linkedInterviewId: number | null
  active: boolean
  createdAt: string
}

export interface ActivityLog {
  id: number
  activityId: number
  activityName: string
  logDate: string
  completed: boolean
  notes: string | null
  moodRating: number | null
  createdAt: string
}

export interface DailyChecklistItem {
  activityId: number
  activityName: string
  activityType: string
  date: string
  logId: number | null
  completed: boolean
  notes: string | null
  moodRating: number | null
}

export interface ActivityForm {
  type: ActivityType
  name: string
  description: string
  frequency: string
  linkedInterviewId: number | null
}

export interface ActivityLogForm {
  logDate: string
  completed: boolean
  notes: string
  moodRating: number | null
}

export const useActivitiesStore = defineStore('activities', () => {
  const activities = ref<Activity[]>([])
  const checklist = ref<DailyChecklistItem[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  const activeActivities = computed(() => activities.value.filter((a) => a.active))
  const inactiveActivities = computed(() => activities.value.filter((a) => !a.active))

  async function fetchActivities(activeOnly?: boolean) {
    loading.value = true
    error.value = null
    try {
      const params = activeOnly !== undefined ? { active: activeOnly } : {}
      const response = await api.get('/activities', { params })
      activities.value = response.data
    } catch (err) {
      error.value = 'Failed to load activities'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function createActivity(form: ActivityForm) {
    loading.value = true
    error.value = null
    try {
      const response = await api.post('/activities', form)
      activities.value.unshift(response.data)
      return response.data
    } catch (err) {
      error.value = 'Failed to create activity'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function updateActivity(id: number, form: ActivityForm) {
    loading.value = true
    error.value = null
    try {
      const response = await api.put(`/activities/${id}`, form)
      const index = activities.value.findIndex((a) => a.id === id)
      if (index !== -1) {
        activities.value[index] = response.data
      }
      return response.data
    } catch (err) {
      error.value = 'Failed to update activity'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function toggleActive(id: number) {
    error.value = null
    try {
      const response = await api.patch(`/activities/${id}/toggle`)
      const index = activities.value.findIndex((a) => a.id === id)
      if (index !== -1) {
        activities.value[index] = response.data
      }
      return response.data
    } catch (err) {
      error.value = 'Failed to toggle activity'
      throw err
    }
  }

  async function deleteActivity(id: number) {
    loading.value = true
    error.value = null
    try {
      await api.delete(`/activities/${id}`)
      activities.value = activities.value.filter((a) => a.id !== id)
    } catch (err) {
      error.value = 'Failed to delete activity'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function fetchChecklist(date: string) {
    loading.value = true
    error.value = null
    try {
      const response = await api.get('/activities/checklist', { params: { date } })
      checklist.value = response.data
    } catch (err) {
      error.value = 'Failed to load checklist'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function logActivity(activityId: number, form: ActivityLogForm) {
    error.value = null
    try {
      const response = await api.post(`/activities/${activityId}/logs`, form)
      const item = checklist.value.find((c) => c.activityId === activityId)
      if (item) {
        item.logId = response.data.id
        item.completed = response.data.completed
        item.notes = response.data.notes
        item.moodRating = response.data.moodRating
      }
      return response.data
    } catch (err) {
      error.value = 'Failed to log activity'
      throw err
    }
  }

  function clearError() {
    error.value = null
  }

  return {
    activities,
    checklist,
    loading,
    error,
    activeActivities,
    inactiveActivities,
    fetchActivities,
    createActivity,
    updateActivity,
    toggleActive,
    deleteActivity,
    fetchChecklist,
    logActivity,
    clearError,
  }
})
