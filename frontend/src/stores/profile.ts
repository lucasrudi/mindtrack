import { ref } from 'vue'
import { defineStore } from 'pinia'
import api from '@/services/api'

export interface NotificationPrefs {
  emailNotifications?: boolean
  pushNotifications?: boolean
  reminderTime?: string
  [key: string]: unknown
}

export interface UserProfile {
  id: number
  userId: number
  displayName: string | null
  avatarUrl: string | null
  timezone: string | null
  notificationPrefs: NotificationPrefs | null
  telegramChatId: string | null
  whatsappNumber: string | null
}

export interface ProfileForm {
  displayName: string | null
  avatarUrl: string | null
  timezone: string | null
  notificationPrefs: NotificationPrefs | null
  telegramChatId: string | null
  whatsappNumber: string | null
}

export const useProfileStore = defineStore('profile', () => {
  const profile = ref<UserProfile | null>(null)
  const loading = ref(false)
  const saving = ref(false)
  const error = ref<string | null>(null)

  async function fetchProfile() {
    loading.value = true
    error.value = null
    try {
      const response = await api.get('/profile')
      profile.value = response.data
      return response.data
    } catch (err) {
      error.value = 'Failed to load profile'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function updateProfile(form: ProfileForm) {
    saving.value = true
    error.value = null
    try {
      const response = await api.put('/profile', form)
      profile.value = response.data
      return response.data
    } catch (err) {
      error.value = 'Failed to save profile'
      throw err
    } finally {
      saving.value = false
    }
  }

  function clearError() {
    error.value = null
  }

  return {
    profile,
    loading,
    saving,
    error,
    fetchProfile,
    updateProfile,
    clearError,
  }
})
