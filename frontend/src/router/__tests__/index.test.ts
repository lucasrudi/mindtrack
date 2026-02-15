import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createApp } from 'vue'
import { createPinia, setActivePinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import router from '../index'

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
  beforeEach(async () => {
    // Create a Vue app with pinia and router so they share context
    const pinia = createPinia()
    const app = createApp({ template: '<div />' })
    app.use(pinia)
    app.use(router)
    setActivePinia(pinia)
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

  it('redirects authenticated user from landing to dashboard', async () => {
    const auth = useAuthStore()
    auth.setToken('fake-token')

    await router.push('/')
    await router.isReady()
    expect(router.currentRoute.value.name).toBe('dashboard')
  })

  it('allows authenticated user to access protected route', async () => {
    const auth = useAuthStore()
    auth.setToken('fake-token')

    await router.push('/journal')
    await router.isReady()
    expect(router.currentRoute.value.name).toBe('journal')
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
