import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useGoalsStore } from '../goals'

vi.mock('@/services/api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}))

const sampleGoal = {
  id: 1,
  title: 'Learn meditation',
  description: 'Practice daily mindfulness',
  category: 'Health',
  targetDate: '2025-06-01',
  status: 'IN_PROGRESS' as const,
  totalMilestones: 2,
  completedMilestones: 1,
  milestones: [
    {
      id: 10,
      goalId: 1,
      title: 'Complete beginner course',
      targetDate: '2025-03-01',
      completedAt: '2025-02-20T10:00:00',
      completed: true,
      notes: 'Headspace basics',
      createdAt: '2025-01-01T10:00:00',
    },
    {
      id: 11,
      goalId: 1,
      title: '30-day streak',
      targetDate: '2025-04-01',
      completedAt: null,
      completed: false,
      notes: null,
      createdAt: '2025-01-01T10:00:00',
    },
  ],
  createdAt: '2025-01-01T10:00:00',
  updatedAt: '2025-01-01T10:00:00',
}

const sampleGoal2 = {
  id: 2,
  title: 'Run a 5K',
  description: null,
  category: 'Fitness',
  targetDate: null,
  status: 'COMPLETED' as const,
  totalMilestones: 0,
  completedMilestones: 0,
  milestones: [],
  createdAt: '2025-01-02T10:00:00',
  updatedAt: '2025-01-02T10:00:00',
}

describe('useGoalsStore', () => {
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
    const store = useGoalsStore()
    expect(store.goals).toEqual([])
    expect(store.currentGoal).toBeNull()
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
  })

  it('fetches goals', async () => {
    api.get.mockResolvedValue({ data: [sampleGoal, sampleGoal2] })
    const store = useGoalsStore()

    await store.fetchGoals()

    expect(api.get).toHaveBeenCalledWith('/goals')
    expect(store.goals).toHaveLength(2)
  })

  it('computes active and completed goals', async () => {
    api.get.mockResolvedValue({ data: [sampleGoal, sampleGoal2] })
    const store = useGoalsStore()
    await store.fetchGoals()

    expect(store.activeGoals).toHaveLength(1)
    expect(store.activeGoals[0].id).toBe(1)
    expect(store.completedGoals).toHaveLength(1)
    expect(store.completedGoals[0].id).toBe(2)
  })

  it('fetches single goal', async () => {
    api.get.mockResolvedValue({ data: sampleGoal })
    const store = useGoalsStore()

    const result = await store.fetchGoal(1)

    expect(api.get).toHaveBeenCalledWith('/goals/1')
    expect(result).toEqual(sampleGoal)
    expect(store.currentGoal).toEqual(sampleGoal)
  })

  it('creates goal', async () => {
    const newGoal = { ...sampleGoal, id: 3 }
    api.post.mockResolvedValue({ data: newGoal })
    const store = useGoalsStore()

    const result = await store.createGoal({
      title: 'Learn meditation',
      description: 'Practice daily mindfulness',
      category: 'Health',
      targetDate: '2025-06-01',
    })

    expect(api.post).toHaveBeenCalledWith('/goals', expect.any(Object))
    expect(result).toEqual(newGoal)
    expect(store.goals).toHaveLength(1)
  })

  it('updates goal', async () => {
    api.get.mockResolvedValue({ data: [sampleGoal] })
    const updated = { ...sampleGoal, title: 'Updated title' }
    api.put.mockResolvedValue({ data: updated })
    const store = useGoalsStore()
    await store.fetchGoals()

    await store.updateGoal(1, {
      title: 'Updated title',
      description: 'Practice daily mindfulness',
      category: 'Health',
      targetDate: '2025-06-01',
    })

    expect(api.put).toHaveBeenCalledWith('/goals/1', expect.any(Object))
    expect(store.goals[0].title).toBe('Updated title')
  })

  it('updates goal status', async () => {
    api.get.mockResolvedValue({ data: [sampleGoal] })
    const updated = { ...sampleGoal, status: 'COMPLETED' as const }
    api.patch.mockResolvedValue({ data: updated })
    const store = useGoalsStore()
    await store.fetchGoals()

    await store.updateStatus(1, 'COMPLETED')

    expect(api.patch).toHaveBeenCalledWith('/goals/1/status', { status: 'COMPLETED' })
    expect(store.goals[0].status).toBe('COMPLETED')
  })

  it('updates currentGoal on status change', async () => {
    api.get.mockResolvedValue({ data: sampleGoal })
    const updated = { ...sampleGoal, status: 'PAUSED' as const }
    api.patch.mockResolvedValue({ data: updated })
    const store = useGoalsStore()
    await store.fetchGoal(1)

    await store.updateStatus(1, 'PAUSED')

    expect(store.currentGoal?.status).toBe('PAUSED')
  })

  it('deletes goal', async () => {
    api.get.mockResolvedValue({ data: [sampleGoal, sampleGoal2] })
    api.delete.mockResolvedValue({})
    const store = useGoalsStore()
    await store.fetchGoals()

    await store.deleteGoal(1)

    expect(api.delete).toHaveBeenCalledWith('/goals/1')
    expect(store.goals).toHaveLength(1)
    expect(store.goals[0].id).toBe(2)
  })

  it('adds milestone', async () => {
    api.get.mockResolvedValue({ data: sampleGoal })
    const newMilestone = {
      id: 12,
      goalId: 1,
      title: 'Attend retreat',
      targetDate: '2025-05-01',
      completedAt: null,
      completed: false,
      notes: '',
      createdAt: '2025-02-01T10:00:00',
    }
    api.post.mockResolvedValue({ data: newMilestone })
    const store = useGoalsStore()
    await store.fetchGoal(1)
    // Also put goal in goals list with its own milestones copy
    store.goals = [{ ...sampleGoal, milestones: [...sampleGoal.milestones] }]

    await store.addMilestone(1, {
      title: 'Attend retreat',
      targetDate: '2025-05-01',
      notes: '',
    })

    expect(api.post).toHaveBeenCalledWith('/goals/1/milestones', expect.any(Object))
    expect(store.currentGoal?.milestones).toHaveLength(3)
    expect(store.currentGoal?.totalMilestones).toBe(3)
    expect(store.goals[0].milestones).toHaveLength(3)
    expect(store.goals[0].totalMilestones).toBe(3)
  })

  it('toggles milestone completion', async () => {
    api.get.mockResolvedValue({ data: sampleGoal })
    const toggled = {
      ...sampleGoal.milestones[1],
      completed: true,
      completedAt: '2025-02-15T10:00:00',
    }
    api.patch.mockResolvedValue({ data: toggled })
    const store = useGoalsStore()
    await store.fetchGoal(1)
    store.goals = [{ ...sampleGoal, milestones: [...sampleGoal.milestones] }]

    await store.toggleMilestone(1, 11)

    expect(api.patch).toHaveBeenCalledWith('/goals/1/milestones/11/toggle')
    expect(store.currentGoal?.completedMilestones).toBe(2)
  })

  it('sets error on fetch failure', async () => {
    api.get.mockRejectedValue(new Error('Network error'))
    const store = useGoalsStore()

    await expect(store.fetchGoals()).rejects.toThrow('Network error')

    expect(store.error).toBe('Failed to load goals')
  })

  it('clears error', () => {
    const store = useGoalsStore()
    store.error = 'Some error'
    store.clearError()
    expect(store.error).toBeNull()
  })
})
