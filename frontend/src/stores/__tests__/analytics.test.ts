import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAnalyticsStore } from '../analytics'

vi.mock('@/services/api', () => ({
  default: {
    get: vi.fn(),
  },
}))

const mockSummary = {
  totalJournalEntries: 5,
  averageMood: 7.5,
  totalActivitiesLogged: 10,
  activityCompletionRate: 80,
  totalGoals: 3,
  completedGoals: 1,
  activeGoals: 2,
}

const mockMoodTrends = [
  { date: '2025-01-05', averageMood: 7, entryCount: 2 },
  { date: '2025-01-10', averageMood: 8.5, entryCount: 1 },
]

const mockActivityStats = [
  { activityType: 'EXERCISE', totalLogs: 5, completedLogs: 4, completionRate: 80 },
  { activityType: 'MEDITATION', totalLogs: 3, completedLogs: 3, completionRate: 100 },
]

const mockGoalProgress = [
  { status: 'IN_PROGRESS', count: 2 },
  { status: 'COMPLETED', count: 1 },
]

describe('useAnalyticsStore', () => {
  let api: { get: ReturnType<typeof vi.fn> }

  beforeEach(async () => {
    setActivePinia(createPinia())
    const module = await import('@/services/api')
    api = module.default as unknown as typeof api
    vi.clearAllMocks()
  })

  describe('fetchSummary', () => {
    it('fetches dashboard summary', async () => {
      api.get.mockResolvedValueOnce({ data: mockSummary })
      const store = useAnalyticsStore()

      await store.fetchSummary()

      expect(store.summary).toEqual(mockSummary)
    })

    it('sets error on failure', async () => {
      api.get.mockRejectedValueOnce(new Error('Network error'))
      const store = useAnalyticsStore()

      await expect(store.fetchSummary()).rejects.toThrow()
      expect(store.error).toBe('Failed to load dashboard summary')
    })
  })

  describe('fetchMoodTrends', () => {
    it('fetches mood trends', async () => {
      api.get.mockResolvedValueOnce({ data: mockMoodTrends })
      const store = useAnalyticsStore()

      await store.fetchMoodTrends()

      expect(store.moodTrends).toEqual(mockMoodTrends)
    })

    it('sets error on failure', async () => {
      api.get.mockRejectedValueOnce(new Error('Error'))
      const store = useAnalyticsStore()

      await expect(store.fetchMoodTrends()).rejects.toThrow()
      expect(store.error).toBe('Failed to load mood trends')
    })
  })

  describe('fetchActivityStats', () => {
    it('fetches activity stats', async () => {
      api.get.mockResolvedValueOnce({ data: mockActivityStats })
      const store = useAnalyticsStore()

      await store.fetchActivityStats()

      expect(store.activityStats).toEqual(mockActivityStats)
    })

    it('sets error on failure', async () => {
      api.get.mockRejectedValueOnce(new Error('Error'))
      const store = useAnalyticsStore()

      await expect(store.fetchActivityStats()).rejects.toThrow()
      expect(store.error).toBe('Failed to load activity stats')
    })
  })

  describe('fetchGoalProgress', () => {
    it('fetches goal progress', async () => {
      api.get.mockResolvedValueOnce({ data: mockGoalProgress })
      const store = useAnalyticsStore()

      await store.fetchGoalProgress()

      expect(store.goalProgress).toEqual(mockGoalProgress)
    })

    it('sets error on failure', async () => {
      api.get.mockRejectedValueOnce(new Error('Error'))
      const store = useAnalyticsStore()

      await expect(store.fetchGoalProgress()).rejects.toThrow()
      expect(store.error).toBe('Failed to load goal progress')
    })
  })

  describe('fetchAll', () => {
    it('fetches all data and sets loading state', async () => {
      api.get.mockResolvedValueOnce({ data: mockSummary })
      api.get.mockResolvedValueOnce({ data: mockMoodTrends })
      api.get.mockResolvedValueOnce({ data: mockActivityStats })
      api.get.mockResolvedValueOnce({ data: mockGoalProgress })
      const store = useAnalyticsStore()

      await store.fetchAll()

      expect(store.summary).toEqual(mockSummary)
      expect(store.moodTrends).toEqual(mockMoodTrends)
      expect(store.activityStats).toEqual(mockActivityStats)
      expect(store.goalProgress).toEqual(mockGoalProgress)
      expect(store.loading).toBe(false)
    })

    it('sets loading false even on partial failure', async () => {
      api.get.mockRejectedValueOnce(new Error('Error'))
      api.get.mockResolvedValueOnce({ data: mockMoodTrends })
      api.get.mockResolvedValueOnce({ data: mockActivityStats })
      api.get.mockResolvedValueOnce({ data: mockGoalProgress })
      const store = useAnalyticsStore()

      await store.fetchAll()

      expect(store.loading).toBe(false)
      expect(store.error).toBe('Failed to load dashboard summary')
    })
  })

  describe('setDateRange', () => {
    it('sets the date range', () => {
      const store = useAnalyticsStore()

      store.setDateRange('2025-01-01', '2025-01-31')

      expect(store.dateRange.from).toBe('2025-01-01')
      expect(store.dateRange.to).toBe('2025-01-31')
    })
  })

  describe('clearError', () => {
    it('clears the error state', () => {
      const store = useAnalyticsStore()
      store.error = 'Some error'

      store.clearError()

      expect(store.error).toBeNull()
    })
  })
})
