import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useActivitiesStore } from '../activities'

const mockActivities = [
  {
    id: 1,
    type: 'EXERCISE' as const,
    name: 'Morning jog',
    description: '30 minutes',
    frequency: 'Daily',
    linkedInterviewId: null,
    active: true,
    createdAt: '2025-01-15T10:00:00',
  },
  {
    id: 2,
    type: 'MEDITATION' as const,
    name: 'Evening meditation',
    description: null,
    frequency: null,
    linkedInterviewId: null,
    active: false,
    createdAt: '2025-01-14T10:00:00',
  },
]

vi.mock('@/services/api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}))

describe('useActivitiesStore', () => {
  let api: {
    get: ReturnType<typeof vi.fn>
    post: ReturnType<typeof vi.fn>
    put: ReturnType<typeof vi.fn>
    patch: ReturnType<typeof vi.fn>
    delete: ReturnType<typeof vi.fn>
  }

  beforeEach(async () => {
    setActivePinia(createPinia())
    const module = await import('@/services/api')
    api = module.default as unknown as typeof api
    vi.clearAllMocks()
  })

  it('initializes with empty state', () => {
    const store = useActivitiesStore()
    expect(store.activities).toEqual([])
    expect(store.checklist).toEqual([])
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
  })

  it('fetches activities', async () => {
    api.get.mockResolvedValue({ data: mockActivities })
    const store = useActivitiesStore()

    await store.fetchActivities()

    expect(api.get).toHaveBeenCalledWith('/activities', { params: {} })
    expect(store.activities).toEqual(mockActivities)
  })

  it('fetches active activities only', async () => {
    api.get.mockResolvedValue({ data: [mockActivities[0]] })
    const store = useActivitiesStore()

    await store.fetchActivities(true)

    expect(api.get).toHaveBeenCalledWith('/activities', { params: { active: true } })
  })

  it('computes active and inactive activities', async () => {
    api.get.mockResolvedValue({ data: mockActivities })
    const store = useActivitiesStore()
    await store.fetchActivities()

    expect(store.activeActivities).toHaveLength(1)
    expect(store.activeActivities[0].name).toBe('Morning jog')
    expect(store.inactiveActivities).toHaveLength(1)
    expect(store.inactiveActivities[0].name).toBe('Evening meditation')
  })

  it('creates activity', async () => {
    const newActivity = { ...mockActivities[0], id: 3 }
    api.post.mockResolvedValue({ data: newActivity })
    const store = useActivitiesStore()

    const result = await store.createActivity({
      type: 'EXERCISE',
      name: 'Morning jog',
      description: '',
      frequency: 'Daily',
      linkedInterviewId: null,
    })

    expect(api.post).toHaveBeenCalledWith('/activities', expect.any(Object))
    expect(result).toEqual(newActivity)
    expect(store.activities).toHaveLength(1)
  })

  it('toggles activity active status', async () => {
    api.get.mockResolvedValue({ data: mockActivities })
    api.patch.mockResolvedValue({ data: { ...mockActivities[0], active: false } })
    const store = useActivitiesStore()
    await store.fetchActivities()

    await store.toggleActive(1)

    expect(api.patch).toHaveBeenCalledWith('/activities/1/toggle')
    expect(store.activities[0].active).toBe(false)
  })

  it('deletes activity', async () => {
    api.get.mockResolvedValue({ data: mockActivities })
    api.delete.mockResolvedValue({})
    const store = useActivitiesStore()
    await store.fetchActivities()

    await store.deleteActivity(1)

    expect(api.delete).toHaveBeenCalledWith('/activities/1')
    expect(store.activities).toHaveLength(1)
    expect(store.activities[0].id).toBe(2)
  })

  it('fetches daily checklist', async () => {
    const checklistData = [
      {
        activityId: 1,
        activityName: 'Morning jog',
        activityType: 'EXERCISE',
        date: '2025-01-15',
        logId: null,
        completed: false,
        notes: null,
        moodRating: null,
      },
    ]
    api.get.mockResolvedValue({ data: checklistData })
    const store = useActivitiesStore()

    await store.fetchChecklist('2025-01-15')

    expect(api.get).toHaveBeenCalledWith('/activities/checklist', {
      params: { date: '2025-01-15' },
    })
    expect(store.checklist).toEqual(checklistData)
  })

  it('logs activity and updates checklist', async () => {
    const store = useActivitiesStore()
    store.checklist = [
      {
        activityId: 1,
        activityName: 'Morning jog',
        activityType: 'EXERCISE',
        date: '2025-01-15',
        logId: null,
        completed: false,
        notes: null,
        moodRating: null,
      },
    ]

    api.post.mockResolvedValue({
      data: { id: 10, completed: true, notes: '', moodRating: null },
    })

    await store.logActivity(1, {
      logDate: '2025-01-15',
      completed: true,
      notes: '',
      moodRating: null,
    })

    expect(api.post).toHaveBeenCalledWith('/activities/1/logs', expect.any(Object))
    expect(store.checklist[0].completed).toBe(true)
    expect(store.checklist[0].logId).toBe(10)
  })

  it('sets error on fetch failure', async () => {
    api.get.mockRejectedValue(new Error('Network error'))
    const store = useActivitiesStore()

    await expect(store.fetchActivities()).rejects.toThrow('Network error')

    expect(store.error).toBe('Failed to load activities')
  })

  it('clears error', () => {
    const store = useActivitiesStore()
    store.error = 'Some error'
    store.clearError()
    expect(store.error).toBeNull()
  })
})
