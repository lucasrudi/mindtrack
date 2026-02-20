import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export interface User {
  id: string
  email: string
  name: string
  role: string
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('mindtrack_token'))
  const user = ref<User | null>(null)

  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('mindtrack_token', newToken)
  }

  function setUser(newUser: User) {
    user.value = newUser
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('mindtrack_token')
  }

  async function fetchCurrentUser() {
    if (!token.value) return

    try {
      const { default: api } = await import('@/services/api')
      const response = await api.get('/auth/me')
      user.value = response.data
    } catch {
      logout()
    }
  }

  return {
    token,
    user,
    isAuthenticated,
    isAdmin,
    setToken,
    setUser,
    logout,
    fetchCurrentUser,
  }
})
