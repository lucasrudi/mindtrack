import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

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

  const isAuthenticated = computed(() => !!user.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const isTherapist = computed(() => user.value?.isTherapist === true)
  const isPatient = computed(() => user.value?.isPatient === true)

  function setUser(newUser: User) {
    user.value = newUser
  }

  async function logout() {
    user.value = null
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

  async function deleteAccount() {
    const { default: api } = await import('@/services/api')
    await api.delete('/auth/account')
    user.value = null
  }

  return {
    user,
    isAuthenticated,
    isAdmin,
    isTherapist,
    isPatient,
    setUser,
    logout,
    fetchCurrentUser,
    deleteAccount,
  }
})
