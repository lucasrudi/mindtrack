import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { usePatientStore } from '../patient'

const mockRequests = [
  {
    relationshipId: 1,
    therapistId: 10,
    therapistName: 'Dr. Smith',
    therapistEmail: 'smith@example.com',
    status: 'PENDING',
    createdAt: '2026-01-01T10:00:00',
  },
  {
    relationshipId: 2,
    therapistId: 11,
    therapistName: 'Dr. Jones',
    therapistEmail: 'jones@example.com',
    status: 'ACTIVE',
    createdAt: '2026-01-02T10:00:00',
  },
]

vi.mock('@/services/api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
  },
}))

describe('usePatientStore', () => {
  let api: {
    get: ReturnType<typeof vi.fn>
    post: ReturnType<typeof vi.fn>
  }

  beforeEach(async () => {
    setActivePinia(createPinia())
    const module = await import('@/services/api')
    api = module.default as unknown as typeof api
    vi.clearAllMocks()
  })

  describe('fetchRequests', () => {
    it('fetches therapist requests', async () => {
      api.get.mockResolvedValueOnce({ data: mockRequests })
      const store = usePatientStore()

      await store.fetchRequests()

      expect(api.get).toHaveBeenCalledWith('/patient/requests')
      expect(store.requests).toEqual(mockRequests)
      expect(store.loading).toBe(false)
      expect(store.error).toBeNull()
    })

    it('sets loading state during fetch', async () => {
      let resolve: (value: unknown) => void
      const promise = new Promise((r) => {
        resolve = r
      })
      api.get.mockReturnValueOnce(promise)
      const store = usePatientStore()

      const fetchPromise = store.fetchRequests()
      expect(store.loading).toBe(true)

      resolve!({ data: mockRequests })
      await fetchPromise
      expect(store.loading).toBe(false)
    })

    it('sets error on failure', async () => {
      api.get.mockRejectedValueOnce(new Error('Network error'))
      const store = usePatientStore()

      await store.fetchRequests()

      expect(store.error).toBe('Failed to load therapist requests')
      expect(store.loading).toBe(false)
    })
  })

  describe('acceptRequest', () => {
    it('updates status to ACTIVE locally on success', async () => {
      api.get.mockResolvedValueOnce({ data: mockRequests })
      api.post.mockResolvedValueOnce({})
      const store = usePatientStore()
      await store.fetchRequests()

      await store.acceptRequest(1)

      expect(api.post).toHaveBeenCalledWith('/patient/requests/1/accept')
      const updated = store.requests.find((r) => r.relationshipId === 1)
      expect(updated?.status).toBe('ACTIVE')
    })

    it('sets error and rethrows on failure', async () => {
      api.get.mockResolvedValueOnce({ data: mockRequests })
      api.post.mockRejectedValueOnce(new Error('Server error'))
      const store = usePatientStore()
      await store.fetchRequests()

      await expect(store.acceptRequest(1)).rejects.toThrow()
      expect(store.error).toBe('Failed to accept request')
    })
  })

  describe('rejectRequest', () => {
    it('removes the request locally on success', async () => {
      api.get.mockResolvedValueOnce({ data: mockRequests })
      api.post.mockResolvedValueOnce({})
      const store = usePatientStore()
      await store.fetchRequests()

      await store.rejectRequest(1)

      expect(api.post).toHaveBeenCalledWith('/patient/requests/1/reject')
      expect(store.requests.find((r) => r.relationshipId === 1)).toBeUndefined()
      expect(store.requests).toHaveLength(1)
    })

    it('sets error and rethrows on failure', async () => {
      api.get.mockResolvedValueOnce({ data: mockRequests })
      api.post.mockRejectedValueOnce(new Error('Server error'))
      const store = usePatientStore()
      await store.fetchRequests()

      await expect(store.rejectRequest(1)).rejects.toThrow()
      expect(store.error).toBe('Failed to reject request')
    })
  })

  describe('clearError', () => {
    it('clears the error state', async () => {
      api.get.mockRejectedValueOnce(new Error('error'))
      const store = usePatientStore()
      await store.fetchRequests()
      expect(store.error).not.toBeNull()

      store.clearError()
      expect(store.error).toBeNull()
    })
  })
})
