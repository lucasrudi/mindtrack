import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import GoalsView from '../GoalsView.vue'

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush }),
}))

const mockGet = vi.fn().mockResolvedValue({ data: [] })
vi.mock('@/services/api', () => ({
  default: {
    get: (...args: unknown[]) => mockGet(...args),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}))

const sampleGoal = {
  id: 1,
  title: 'Learn meditation',
  description: 'Practice daily',
  category: 'Health',
  targetDate: '2025-06-01',
  status: 'IN_PROGRESS' as const,
  totalMilestones: 3,
  completedMilestones: 1,
  milestones: [],
  createdAt: '2025-01-01T10:00:00',
  updatedAt: '2025-01-01T10:00:00',
}

const completedGoal = {
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

describe('GoalsView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockPush.mockClear()
    mockGet.mockReset().mockResolvedValue({ data: [] })
  })

  it('renders page header', () => {
    const wrapper = mount(GoalsView)
    expect(wrapper.find('h1').text()).toBe('Goals')
    expect(wrapper.find('.subtitle').text()).toContain('milestones')
  })

  it('renders new goal button', () => {
    const wrapper = mount(GoalsView)
    const btn = wrapper.find('.btn-primary')
    expect(btn.exists()).toBe(true)
    expect(btn.text()).toContain('New Goal')
  })

  it('shows empty state when no goals', async () => {
    const wrapper = mount(GoalsView)
    await flushPromises()
    expect(wrapper.find('.empty-state').exists()).toBe(true)
    expect(wrapper.text()).toContain('No goals yet')
  })

  it('navigates to new goal form', async () => {
    const wrapper = mount(GoalsView)
    await wrapper.find('.page-header .btn-primary').trigger('click')
    expect(mockPush).toHaveBeenCalledWith({ name: 'goal-new' })
  })

  it('renders goal cards when data loaded', async () => {
    mockGet.mockResolvedValue({ data: [sampleGoal, completedGoal] })

    const wrapper = mount(GoalsView)
    await flushPromises()

    const cards = wrapper.findAll('.goal-card')
    expect(cards).toHaveLength(2)
    expect(wrapper.text()).toContain('Learn meditation')
    expect(wrapper.text()).toContain('Run a 5K')
  })

  it('shows active and completed sections', async () => {
    mockGet.mockResolvedValue({ data: [sampleGoal, completedGoal] })

    const wrapper = mount(GoalsView)
    await flushPromises()

    const sections = wrapper.findAll('.section-title')
    expect(sections).toHaveLength(2)
    expect(sections[0].text()).toBe('Active')
    expect(sections[1].text()).toBe('Completed')
  })

  it('displays status badges', async () => {
    mockGet.mockResolvedValue({ data: [sampleGoal] })

    const wrapper = mount(GoalsView)
    await flushPromises()

    const badge = wrapper.find('.status-badge')
    expect(badge.text()).toBe('In Progress')
    expect(badge.classes()).toContain('status-in-progress')
  })

  it('displays progress bar for goals with milestones', async () => {
    mockGet.mockResolvedValue({ data: [sampleGoal] })

    const wrapper = mount(GoalsView)
    await flushPromises()

    expect(wrapper.find('.progress-section').exists()).toBe(true)
    expect(wrapper.find('.progress-text').text()).toContain('1/3 milestones')
  })

  it('displays category badge', async () => {
    mockGet.mockResolvedValue({ data: [sampleGoal] })

    const wrapper = mount(GoalsView)
    await flushPromises()

    expect(wrapper.find('.goal-category').text()).toBe('Health')
  })

  it('navigates to goal detail on card click', async () => {
    mockGet.mockResolvedValue({ data: [sampleGoal] })

    const wrapper = mount(GoalsView)
    await flushPromises()

    await wrapper.find('.goal-card').trigger('click')
    expect(mockPush).toHaveBeenCalledWith({ name: 'goal-detail', params: { id: 1 } })
  })
})
