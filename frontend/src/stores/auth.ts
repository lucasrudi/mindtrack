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

export type ViewMode = 'patient' | 'therapist'

const ACTIVE_VIEW_STORAGE_KEY = 'mindtrack.activeView'

function isViewMode(value: string | null): value is ViewMode {
  return value === 'patient' || value === 'therapist'
}

function readStoredView(): ViewMode | null {
  try {
    const stored = globalThis.localStorage?.getItem(ACTIVE_VIEW_STORAGE_KEY)
    return isViewMode(stored) ? stored : null
  } catch {
    return null
  }
}

function persistView(view: ViewMode) {
  try {
    globalThis.localStorage?.setItem(ACTIVE_VIEW_STORAGE_KEY, view)
  } catch {
    // Best effort only
  }
}

function clearStoredView() {
  try {
    globalThis.localStorage?.removeItem(ACTIVE_VIEW_STORAGE_KEY)
  } catch {
    // Best effort only
  }
}

function resolveDefaultView(user: User | null): ViewMode {
  if (user?.isTherapist && !user.isPatient) {
    return 'therapist'
  }

  return 'patient'
}

function resolveAvailableView(user: User | null, preferred: ViewMode | null): ViewMode {
  if (!user) {
    return preferred ?? 'patient'
  }

  if (preferred === 'therapist' && user.isTherapist) {
    return 'therapist'
  }

  if (preferred === 'patient' && user.isPatient) {
    return 'patient'
  }

  if (user.isTherapist && !user.isPatient) {
    return 'therapist'
  }

  if (user.isPatient) {
    return 'patient'
  }

  if (user.isTherapist) {
    return 'therapist'
  }

  return resolveDefaultView(user)
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const hasBootstrapped = ref(false)
  const activeView = ref<ViewMode>(readStoredView() ?? 'patient')
  let bootstrapPromise: Promise<void> | null = null

  const isAuthenticated = computed(() => !!user.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const isTherapist = computed(() => user.value?.isTherapist === true)
  const isPatient = computed(() => user.value?.isPatient === true)
  const canSwitchViews = computed(() => isTherapist.value && isPatient.value)
  const homeRouteName = computed(() =>
    activeView.value === 'therapist' ? 'therapist' : 'dashboard',
  )
  const activeViewLabel = computed(() =>
    activeView.value === 'therapist' ? 'Therapist' : 'Patient',
  )

  function syncActiveView() {
    const nextView = resolveAvailableView(user.value, readStoredView() ?? activeView.value)
    activeView.value = nextView

    if (user.value) {
      persistView(nextView)
    }
  }

  function setUser(newUser: User) {
    user.value = newUser
    syncActiveView()
  }

  function setActiveView(view: ViewMode) {
    const nextView = resolveAvailableView(user.value, view)
    activeView.value = nextView

    if (user.value) {
      persistView(nextView)
    }
  }

  async function logout() {
    user.value = null
    hasBootstrapped.value = true
    clearDashboardSessionCache()
    activeView.value = 'patient'
    clearStoredView()
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
      setUser(response.data)
    } catch {
      user.value = null
    }
  }

  async function bootstrap() {
    if (hasBootstrapped.value || user.value) {
      if (user.value) {
        syncActiveView()
      }
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
    activeView.value = 'patient'
    clearStoredView()
  }

  return {
    user,
    hasBootstrapped,
    activeView,
    isAuthenticated,
    isAdmin,
    isTherapist,
    isPatient,
    canSwitchViews,
    homeRouteName,
    activeViewLabel,
    setUser,
    setActiveView,
    logout,
    fetchCurrentUser,
    bootstrap,
    deleteAccount,
  }
})
