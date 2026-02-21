import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useTherapistStore } from '../therapist'

const mockPatients = [
  {
    id: 1,
    name: 'Patient One',
    email: 'patient1@test.com',
    interviewCount: 3,
    activeGoalCount: 2,
    activityCount: 5,
    lastInterviewDate: '2025-01-15T10:00:00',
  },
  {
    id: 2,
    name: 'Patient Two',
    email: 'patient2@test.com',
    interviewCount: 1,
    activeGoalCount: 0,
    activityCount: 2,
    lastInterviewDate: null,
  },
]

const mockPatientDetail = {
  patientId: 1,
  patientName: 'Patient One',
  patientEmail: 'patient1@test.com',
  interviews: [
    {
      id: 1,
      interviewDate: '2025-01-15',
      moodBefore: 5,
      moodAfter: 7,
      topics: ['anxiety'],
      notes: 'Good session',
      createdAt: '2025-01-15T10:00:00',
    },
  ],
  activities: [
    {
      id: 1,
      type: 'HOMEWORK',
      name: 'Breathing exercises',
      description: 'Daily practice',
      active: true,
      createdAt: '2025-01-10T10:00:00',
    },
  ],
  goals: [
    {
      id: 1,
      title: 'Reduce anxiety',
      description: null,
      status: 'IN_PROGRESS',
      targetDate: '2025-06-01',
      totalMilestones: 3,
      completedMilestones: 1,
      createdAt: '2025-01-01T10:00:00',
    },
  ],
  sharedJournalEntries: [
    {
      id: 1,
      entryDate: '2025-01-14',
      title: 'Feeling better',
      content: 'Today was good.',
      mood: 7,
      tags: ['positive'],
      createdAt: '2025-01-14T20:00:00',
    },
  ],
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

describe('useTherapistStore', () => {
  let api: {
    get: ReturnType<typeof vi.fn>
  }

  beforeEach(async () => {
    setActivePinia(createPinia())
    const module = await import('@/services/api')
    api = module.default as unknown as typeof api
    vi.clearAllMocks()
  })

  describe('fetchPatients', () => {
    it('fetches patient list', async () => {
      api.get.mockResolvedValueOnce({ data: mockPatients })
      const store = useTherapistStore()

      await store.fetchPatients()

      expect(api.get).toHaveBeenCalledWith('/therapist/patients')
      expect(store.patients).toEqual(mockPatients)
      expect(store.loading).toBe(false)
    })

    it('sets loading state during fetch', async () => {
      let resolvePromise: (value: unknown) => void
      const promise = new Promise((resolve) => {
        resolvePromise = resolve
      })
      api.get.mockReturnValueOnce(promise)
      const store = useTherapistStore()

      const fetchPromise = store.fetchPatients()
      expect(store.loading).toBe(true)

      resolvePromise!({ data: mockPatients })
      await fetchPromise
      expect(store.loading).toBe(false)
    })

    it('sets error on failure', async () => {
      api.get.mockRejectedValueOnce(new Error('Network error'))
      const store = useTherapistStore()

      await expect(store.fetchPatients()).rejects.toThrow()
      expect(store.error).toBe('Failed to load patients')
      expect(store.loading).toBe(false)
    })
  })

  describe('fetchPatientDetail', () => {
    it('fetches patient detail', async () => {
      api.get.mockResolvedValueOnce({ data: mockPatientDetail })
      const store = useTherapistStore()

      await store.fetchPatientDetail(1)

      expect(api.get).toHaveBeenCalledWith('/therapist/patients/1')
      expect(store.currentPatient).toEqual(mockPatientDetail)
      expect(store.loading).toBe(false)
    })

    it('sets loading state during detail fetch', async () => {
      let resolvePromise: (value: unknown) => void
      const promise = new Promise((resolve) => {
        resolvePromise = resolve
      })
      api.get.mockReturnValueOnce(promise)
      const store = useTherapistStore()

      const fetchPromise = store.fetchPatientDetail(1)
      expect(store.loading).toBe(true)

      resolvePromise!({ data: mockPatientDetail })
      await fetchPromise
      expect(store.loading).toBe(false)
    })

    it('sets error on failure', async () => {
      api.get.mockRejectedValueOnce(new Error('Not found'))
      const store = useTherapistStore()

      await expect(store.fetchPatientDetail(999)).rejects.toThrow()
      expect(store.error).toBe('Failed to load patient details')
      expect(store.loading).toBe(false)
    })
  })

  describe('clearPatient', () => {
    it('clears current patient', () => {
      const store = useTherapistStore()
      store.currentPatient = mockPatientDetail

      store.clearPatient()

      expect(store.currentPatient).toBeNull()
    })
  })

  describe('clearError', () => {
    it('clears the error state', () => {
      const store = useTherapistStore()
      store.error = 'Some error'

      store.clearError()

      expect(store.error).toBeNull()
    })
  })
})
