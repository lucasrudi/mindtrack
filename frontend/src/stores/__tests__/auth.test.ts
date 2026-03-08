import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '../auth'

vi.mock('@/services/api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
  },
}))

describe('useAuthStore', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  describe('isAuthenticated', () => {
    it('returns false when no user', () => {
      const store = useAuthStore()
      expect(store.isAuthenticated).toBe(false)
    })

    it('returns true when user is set', () => {
      const store = useAuthStore()
      store.setUser({
        id: '1',
        email: 'test@test.com',
        name: 'Test',
        role: 'USER',
        isPatient: true,
        isTherapist: false,
      })
      expect(store.isAuthenticated).toBe(true)
    })
  })

  describe('isAdmin', () => {
    it('returns false when no user', () => {
      const store = useAuthStore()
      expect(store.isAdmin).toBe(false)
    })

    it('returns true when user has ADMIN role', () => {
      const store = useAuthStore()
      store.setUser({
        id: '1',
        email: 'admin@test.com',
        name: 'Admin',
        role: 'ADMIN',
        isPatient: false,
        isTherapist: false,
      })
      expect(store.isAdmin).toBe(true)
    })

    it('returns false when user has USER role', () => {
      const store = useAuthStore()
      store.setUser({
        id: '2',
        email: 'user@test.com',
        name: 'User',
        role: 'USER',
        isPatient: true,
        isTherapist: false,
      })
      expect(store.isAdmin).toBe(false)
    })

    it('returns false when user has THERAPIST role', () => {
      const store = useAuthStore()
      store.setUser({
        id: '3',
        email: 'therapist@test.com',
        name: 'Therapist',
        role: 'THERAPIST',
        isPatient: false,
        isTherapist: true,
      })
      expect(store.isAdmin).toBe(false)
    })
  })

  describe('isTherapist', () => {
    it('returns false when no user', () => {
      const store = useAuthStore()
      expect(store.isTherapist).toBe(false)
    })

    it('returns true when user has isTherapist=true', () => {
      const store = useAuthStore()
      store.setUser({
        id: '1',
        email: 'therapist@test.com',
        name: 'Therapist',
        role: 'USER',
        isPatient: false,
        isTherapist: true,
      })
      expect(store.isTherapist).toBe(true)
    })

    it('returns false when user has isTherapist=false', () => {
      const store = useAuthStore()
      store.setUser({
        id: '1',
        email: 'user@test.com',
        name: 'User',
        role: 'USER',
        isPatient: true,
        isTherapist: false,
      })
      expect(store.isTherapist).toBe(false)
    })
  })

  describe('isPatient', () => {
    it('returns false when no user', () => {
      const store = useAuthStore()
      expect(store.isPatient).toBe(false)
    })

    it('returns true when user has isPatient=true', () => {
      const store = useAuthStore()
      store.setUser({
        id: '1',
        email: 'patient@test.com',
        name: 'Patient',
        role: 'USER',
        isPatient: true,
        isTherapist: false,
      })
      expect(store.isPatient).toBe(true)
    })

    it('returns false when user has isPatient=false', () => {
      const store = useAuthStore()
      store.setUser({
        id: '1',
        email: 'user@test.com',
        name: 'User',
        role: 'USER',
        isPatient: false,
        isTherapist: false,
      })
      expect(store.isPatient).toBe(false)
    })
  })

  describe('setUser', () => {
    it('sets user data', () => {
      const store = useAuthStore()
      const user = {
        id: '1',
        email: 'test@test.com',
        name: 'Test',
        role: 'USER',
        isPatient: true,
        isTherapist: false,
      }
      store.setUser(user)
      expect(store.user).toEqual(user)
    })
  })

  describe('logout', () => {
    it('clears user and calls backend logout', async () => {
      const store = useAuthStore()
      store.setUser({
        id: '1',
        email: 'test@test.com',
        name: 'Test',
        role: 'USER',
        isPatient: true,
        isTherapist: false,
      })

      const module = await import('@/services/api')
      const api = module.default as unknown as {
        get: ReturnType<typeof vi.fn>
        post: ReturnType<typeof vi.fn>
      }
      api.post.mockResolvedValueOnce({})

      await store.logout()

      expect(store.user).toBeNull()
      expect(store.isAuthenticated).toBe(false)
      expect(api.post).toHaveBeenCalledWith('/auth/logout')
    })
  })

  describe('fetchCurrentUser', () => {
    it('fetches current user and sets data', async () => {
      const store = useAuthStore()

      const module = await import('@/services/api')
      const api = module.default as unknown as {
        get: ReturnType<typeof vi.fn>
        post: ReturnType<typeof vi.fn>
      }
      const userData = {
        id: '1',
        email: 'test@test.com',
        name: 'Test',
        role: 'USER',
        isPatient: true,
        isTherapist: false,
      }
      api.get.mockResolvedValueOnce({ data: userData })

      await store.fetchCurrentUser()

      expect(store.user).toEqual(userData)
    })

    it('clears user on fetch failure', async () => {
      const store = useAuthStore()
      store.setUser({
        id: '1',
        email: 'test@test.com',
        name: 'Test',
        role: 'USER',
        isPatient: true,
        isTherapist: false,
      })

      const module = await import('@/services/api')
      const api = module.default as unknown as {
        get: ReturnType<typeof vi.fn>
        post: ReturnType<typeof vi.fn>
      }
      api.get.mockRejectedValueOnce(new Error('Unauthorized'))

      await store.fetchCurrentUser()

      expect(store.user).toBeNull()
    })
  })
})
