import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useProfileStore } from '../profile'

const mockProfile = {
  id: 1,
  userId: 10,
  displayName: 'John Doe',
  avatarUrl: 'https://example.com/avatar.jpg',
  timezone: 'America/New_York',
  notificationPrefs: { emailNotifications: true, pushNotifications: false, reminderTime: '09:00' },
  telegramChatId: '123456',
  whatsappNumber: '+1234567890',
  tutorialCompleted: true,
}

vi.mock('@/services/api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}))

describe('useProfileStore', () => {
  let api: {
    get: ReturnType<typeof vi.fn>
    put: ReturnType<typeof vi.fn>
  }

  beforeEach(async () => {
    setActivePinia(createPinia())
    const module = await import('@/services/api')
    api = module.default as unknown as typeof api
    vi.clearAllMocks()
  })

  describe('fetchProfile', () => {
    it('fetches user profile', async () => {
      api.get.mockResolvedValueOnce({ data: mockProfile })
      const store = useProfileStore()

      await store.fetchProfile()

      expect(api.get).toHaveBeenCalledWith('/profile')
      expect(store.profile).toEqual(mockProfile)
      expect(store.loading).toBe(false)
    })

    it('sets loading state during fetch', async () => {
      let resolvePromise: (value: unknown) => void
      const promise = new Promise((resolve) => {
        resolvePromise = resolve
      })
      api.get.mockReturnValueOnce(promise)
      const store = useProfileStore()

      const fetchPromise = store.fetchProfile()
      expect(store.loading).toBe(true)

      resolvePromise!({ data: mockProfile })
      await fetchPromise
      expect(store.loading).toBe(false)
    })

    it('sets error on failure', async () => {
      api.get.mockRejectedValueOnce(new Error('Network error'))
      const store = useProfileStore()

      await expect(store.fetchProfile()).rejects.toThrow()
      expect(store.error).toBe('Failed to load profile')
      expect(store.loading).toBe(false)
    })
  })

  describe('updateProfile', () => {
    it('updates profile and stores result', async () => {
      const updatedProfile = { ...mockProfile, displayName: 'Jane Doe' }
      api.put.mockResolvedValueOnce({ data: updatedProfile })
      const store = useProfileStore()

      const form = {
        displayName: 'Jane Doe',
        avatarUrl: null,
        timezone: null,
        notificationPrefs: null,
        telegramChatId: null,
        whatsappNumber: null,
      }

      await store.updateProfile(form)

      expect(api.put).toHaveBeenCalledWith('/profile', form)
      expect(store.profile).toEqual(updatedProfile)
      expect(store.saving).toBe(false)
    })

    it('sets saving state during update', async () => {
      let resolvePromise: (value: unknown) => void
      const promise = new Promise((resolve) => {
        resolvePromise = resolve
      })
      api.put.mockReturnValueOnce(promise)
      const store = useProfileStore()

      const form = {
        displayName: null,
        avatarUrl: null,
        timezone: null,
        notificationPrefs: null,
        telegramChatId: null,
        whatsappNumber: null,
      }

      const updatePromise = store.updateProfile(form)
      expect(store.saving).toBe(true)

      resolvePromise!({ data: mockProfile })
      await updatePromise
      expect(store.saving).toBe(false)
    })

    it('sets error on failure', async () => {
      api.put.mockRejectedValueOnce(new Error('Save error'))
      const store = useProfileStore()

      const form = {
        displayName: null,
        avatarUrl: null,
        timezone: null,
        notificationPrefs: null,
        telegramChatId: null,
        whatsappNumber: null,
      }

      await expect(store.updateProfile(form)).rejects.toThrow()
      expect(store.error).toBe('Failed to save profile')
      expect(store.saving).toBe(false)
    })
  })

  describe('clearError', () => {
    it('clears the error state', () => {
      const store = useProfileStore()
      store.error = 'Some error'

      store.clearError()

      expect(store.error).toBeNull()
    })
  })
})
