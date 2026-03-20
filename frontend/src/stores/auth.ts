import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { clearDashboardSessionCache } from './dashboardSessionCache'

export interface User {
  id: string
  email: string
  name: string
  role: string
  isPatient: boolean
  isTherapist: boolean
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const hasBootstrapped = ref(false)
  let bootstrapPromise: Promise<void> | null = null

  const isAuthenticated = computed(() => !!user.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const isTherapist = computed(() => user.value?.isTherapist === true)
  const isPatient = computed(() => user.value?.isPatient === true)

  function setUser(newUser: User) {
    user.value = newUser
  }

  async function logout() {
    user.value = null
    hasBootstrapped.value = true
    clearDashboardSessionCache()
    try {
      const { default: api } = await import('@/services/api')
      await api.post('/auth/logout')
    } catch {
      // Best effort — local state already cleared
    }
  }

  async function fetchCurrentUser() {
    try {
      const { default: api } = await import('@/services/api')
      const response = await api.get('/auth/me')
      user.value = response.data
    } catch {
      user.value = null
    }
  }

  async function bootstrap() {
    if (hasBootstrapped.value || user.value) {
      hasBootstrapped.value = true
      return
    }

    bootstrapPromise ??= fetchCurrentUser().finally(() => {
      hasBootstrapped.value = true
      bootstrapPromise = null
    })

    await bootstrapPromise
  }

  async function deleteAccount() {
    const { default: api } = await import('@/services/api')
    await api.delete('/auth/account')
    user.value = null
    hasBootstrapped.value = true
    clearDashboardSessionCache()
  }

  return {
    user,
    hasBootstrapped,
    isAuthenticated,
    isAdmin,
    isTherapist,
    isPatient,
    setUser,
    logout,
    fetchCurrentUser,
    bootstrap,
    deleteAccount,
  }
})
