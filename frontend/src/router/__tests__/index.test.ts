import { describe, it, expect, beforeAll, beforeEach, vi } from 'vitest'
import { createApp } from 'vue'
import { createPinia, setActivePinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import { useProfileStore } from '@/stores/profile'
import router from '../index'

// Mock the API service to prevent the axios interceptor in api.ts from
// registering a response handler that calls auth.logout() + router.push('/')
// on 401 errors. Without this mock, the unawaited auth.logout() in the
// unauthenticated test races against subsequent tests and leaves pending
// async operations that can corrupt auth/router state.
vi.mock('@/services/api', () => ({
  default: {
    post: vi.fn().mockResolvedValue({ data: {} }),
    get: vi.fn().mockResolvedValue({ data: null }),
    delete: vi.fn().mockResolvedValue({ data: {} }),
    interceptors: { response: { use: vi.fn() } },
  },
}))

// Mock all view components — the router test only verifies guard logic and
// navigation behaviour, not component rendering. Without these mocks the
// lazy-loaded SFCs fail to resolve in the jsdom environment (sub-component
// imports inside LandingView, etc. return undefined), causing navigation
// errors that cascade and corrupt the shared router state for later tests.
const stub = { template: '<div />' }
vi.mock('@/views/LandingView.vue', () => ({ default: stub }))
vi.mock('@/views/LoginView.vue', () => ({ default: stub }))
vi.mock('@/views/DashboardView.vue', () => ({ default: stub }))
vi.mock('@/views/InterviewsView.vue', () => ({ default: stub }))
vi.mock('@/views/InterviewFormView.vue', () => ({ default: stub }))
vi.mock('@/views/InterviewDetailView.vue', () => ({ default: stub }))
vi.mock('@/views/ActivitiesView.vue', () => ({ default: stub }))
vi.mock('@/views/ActivityFormView.vue', () => ({ default: stub }))
vi.mock('@/views/JournalView.vue', () => ({ default: stub }))
vi.mock('@/views/JournalFormView.vue', () => ({ default: stub }))
vi.mock('@/views/JournalDetailView.vue', () => ({ default: stub }))
vi.mock('@/views/GoalsView.vue', () => ({ default: stub }))
vi.mock('@/views/GoalFormView.vue', () => ({ default: stub }))
vi.mock('@/views/GoalDetailView.vue', () => ({ default: stub }))
vi.mock('@/views/ChatView.vue', () => ({ default: stub }))
vi.mock('@/views/ProfileView.vue', () => ({ default: stub }))
vi.mock('@/views/AdminView.vue', () => ({ default: stub }))
vi.mock('@/views/TherapistView.vue', () => ({ default: stub }))
vi.mock('@/views/OnboardingView.vue', () => ({ default: stub }))
vi.mock('@/views/InviteView.vue', () => ({ default: stub }))

// Ensure localStorage is available in test environment
const localStorageMock = {
  getItem: vi.fn(() => null),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
  length: 0,
  key: vi.fn(() => null),
}

Object.defineProperty(globalThis, 'localStorage', {
  value: localStorageMock,
  writable: true,
})

describe('Router', () => {
  // Set up the app once — installing the router on multiple apps in beforeEach
  // breaks the singleton router: each new app.use(pinia) sets a new active
  // pinia, but the router's guard still executes against whichever pinia is
  // current, causing auth state set in a test to be invisible to the guard.
  beforeAll(() => {
    const pinia = createPinia()
    const app = createApp({ template: '<div />' })
    app.use(pinia)
    app.use(router)
    setActivePinia(pinia)
  })

  beforeEach(async () => {
    // Reset store state instead of recreating pinia each time
    const auth = useAuthStore()
    const profileStore = useProfileStore()
    auth.user = null
    auth.hasBootstrapped = true
    profileStore.profile = null
    localStorageMock.getItem.mockReturnValue(null)

    // Reset router to a known state
    await router.push('/login')
    await router.isReady()
  })

  it('has a landing route at /', () => {
    const route = router.getRoutes().find((r) => r.path === '/')
    expect(route).toBeDefined()
    expect(route?.name).toBe('landing')
  })

  it('has dashboard route with requiresAuth', () => {
    const route = router.getRoutes().find((r) => r.path === '/dashboard')
    expect(route).toBeDefined()
    expect(route?.meta.requiresAuth).toBe(true)
  })

  it('has landing route without requiresAuth', () => {
    const route = router.getRoutes().find((r) => r.path === '/')
    expect(route?.meta.requiresAuth).toBe(false)
  })

  it('redirects unauthenticated user to landing from protected route', async () => {
    const auth = useAuthStore()
    auth.logout()

    await router.push('/dashboard')
    await router.isReady()
    expect(router.currentRoute.value.name).toBe('landing')
  })

  it('waits for auth bootstrap before redirecting a protected route', async () => {
    const auth = useAuthStore()
    auth.hasBootstrapped = false

    const module = await import('@/services/api')
    const api = module.default as unknown as {
      get: ReturnType<typeof vi.fn>
      post: ReturnType<typeof vi.fn>
      delete: ReturnType<typeof vi.fn>
    }

    let resolveFetch: (value: { data: unknown }) => void = () => {
      throw new Error('Expected pending auth fetch resolver')
    }
    api.get.mockImplementationOnce(
      () =>
        new Promise<{ data: unknown }>((resolve) => {
          resolveFetch = resolve
        }),
    )

    const navigation = router.push('/dashboard')
    await new Promise((resolve) => setTimeout(resolve, 0))

    expect(router.currentRoute.value.name).toBe('login')

    resolveFetch({
      data: {
        id: '1',
        email: 'test@test.com',
        name: 'Test',
        role: 'USER',
        isPatient: true,
        isTherapist: false,
      },
    })

    await navigation
    expect(router.currentRoute.value.name).toBe('dashboard')
  })

  it('redirects authenticated user from landing to dashboard', async () => {
    const auth = useAuthStore()
    auth.setUser({
      id: '1',
      email: 'test@test.com',
      name: 'Test',
      role: 'USER',
      isPatient: true,
      isTherapist: false,
    })

    await router.push('/')
    await router.isReady()
    expect(router.currentRoute.value.name).toBe('dashboard')
  })

  it('allows authenticated user to access protected route', async () => {
    const auth = useAuthStore()
    auth.setUser({
      id: '1',
      email: 'test@test.com',
      name: 'Test',
      role: 'USER',
      isPatient: true,
      isTherapist: false,
    })

    await router.push('/journal')
    await router.isReady()
    expect(router.currentRoute.value.name).toBe('journal')
  })

  it('redirects non-admin users away from admin routes', async () => {
    const auth = useAuthStore()
    auth.setUser({
      id: '1',
      email: 'test@test.com',
      name: 'Test',
      role: 'USER',
      isPatient: true,
      isTherapist: false,
    })

    await router.push('/admin')
    await router.isReady()
    expect(router.currentRoute.value.name).toBe('dashboard')
  })

  it('redirects non-therapists away from therapist routes', async () => {
    const auth = useAuthStore()
    auth.setUser({
      id: '1',
      email: 'test@test.com',
      name: 'Test',
      role: 'USER',
      isPatient: true,
      isTherapist: false,
    })

    await router.push('/therapist')
    await router.isReady()
    expect(router.currentRoute.value.name).toBe('dashboard')
  })

  it('redirects authenticated users with incomplete onboarding to onboarding', async () => {
    const auth = useAuthStore()
    const profileStore = useProfileStore()

    auth.setUser({
      id: '1',
      email: 'test@test.com',
      name: 'Test',
      role: 'USER',
      isPatient: true,
      isTherapist: false,
    })
    profileStore.profile = {
      id: 1,
      userId: 1,
      displayName: null,
      avatarUrl: null,
      timezone: null,
      notificationPrefs: null,
      telegramChatId: null,
      whatsappNumber: null,
      tutorialCompleted: false,
      onboardingCompleted: false,
      surveyCompleted: false,
      isPatient: true,
      isTherapist: false,
      aiConsentGiven: false,
    }

    await router.push('/chat')
    await router.isReady()
    expect(router.currentRoute.value.name).toBe('onboarding')
  })

  it('has all expected protected routes', () => {
    const protectedPaths = [
      '/dashboard',
      '/interviews',
      '/activities',
      '/journal',
      '/goals',
      '/chat',
      '/profile',
      '/admin',
      '/therapist',
    ]
    for (const path of protectedPaths) {
      const route = router.getRoutes().find((r) => r.path === path)
      expect(route, `Route ${path} should exist`).toBeDefined()
      expect(route?.meta.requiresAuth, `Route ${path} should require auth`).toBe(true)
    }
  })
})
