import { ref } from 'vue'
import { defineStore } from 'pinia'
import api from '@/services/api'
import { useAuthStore } from '@/stores/auth'

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
  tutorialCompleted: boolean
  onboardingCompleted: boolean
  surveyCompleted: boolean
  isPatient: boolean
  isTherapist: boolean
  aiConsentGiven: boolean
}

export interface ProfileForm {
  displayName: string | null
  avatarUrl: string | null
  timezone: string | null
  notificationPrefs: NotificationPrefs | null
  telegramChatId: string | null
  whatsappNumber: string | null
  tutorialCompleted?: boolean
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

  async function updateProfile(form: Partial<ProfileForm>) {
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

  async function submitSurvey(data: {
    moodBaseline: number
    anxietyLevel: number
    sleepQuality: number
    lifeAreas: string[]
    depressionScore?: number
    stressLevel?: number
    eatingHabits?: number
  }) {
    saving.value = true
    error.value = null
    try {
      await api.post('/onboarding/survey', data)
      if (profile.value) {
        profile.value.surveyCompleted = true
        profile.value.onboardingCompleted = true
      }
    } catch (err) {
      error.value = 'Failed to submit survey'
      throw err
    } finally {
      saving.value = false
    }
  }

  async function skipSurvey() {
    saving.value = true
    error.value = null
    try {
      await api.post('/onboarding/skip')
      if (profile.value) {
        profile.value.onboardingCompleted = true
      }
    } catch (err) {
      error.value = 'Failed to skip survey'
      throw err
    } finally {
      saving.value = false
    }
  }

  async function updateRoles(isPatient: boolean, isTherapist: boolean) {
    saving.value = true
    error.value = null
    try {
      const authStore = useAuthStore()
      await api.patch('/auth/me/roles', { isPatient, isTherapist })
      await authStore.fetchCurrentUser()
      if (profile.value) {
        profile.value.isPatient = isPatient
        profile.value.isTherapist = isTherapist
      }
    } catch (err) {
      error.value = 'Failed to update roles'
      throw err
    } finally {
      saving.value = false
    }
  }

  async function giveAiConsent() {
    saving.value = true
    error.value = null
    try {
      await api.post('/ai/consent')
      if (profile.value) {
        profile.value.aiConsentGiven = true
      }
    } catch (err) {
      error.value = 'Failed to record AI consent'
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
    submitSurvey,
    skipSurvey,
    updateRoles,
    giveAiConsent,
    clearError,
  }
})
