import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import DashboardView from '../DashboardView.vue'

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() }),
}))

const mockGet = vi.fn().mockResolvedValue({ data: {} })
vi.mock('@/services/api', () => ({
  default: {
    get: (...args: unknown[]) => mockGet(...args),
  },
}))

// Mock chart components to avoid canvas rendering issues in tests
vi.mock('@/components/charts/MoodTrendChart.vue', () => ({
  default: {
    name: 'MoodTrendChart',
    props: ['data'],
    template: '<div class="mock-mood-chart">{{ data.length }} points</div>',
  },
}))

vi.mock('@/components/charts/ActivityCompletionChart.vue', () => ({
  default: {
    name: 'ActivityCompletionChart',
    props: ['data'],
    template: '<div class="mock-activity-chart">{{ data.length }} types</div>',
  },
}))

vi.mock('@/components/charts/GoalProgressChart.vue', () => ({
  default: {
    name: 'GoalProgressChart',
    props: ['data'],
    template: '<div class="mock-goal-chart">{{ data.length }} statuses</div>',
  },
}))

const mockSummary = {
  totalJournalEntries: 5,
  averageMood: 7.5,
  totalActivitiesLogged: 10,
  activityCompletionRate: 80.0,
  totalGoals: 3,
  completedGoals: 1,
  activeGoals: 2,
}

const mockMoodTrends = [
  { date: '2025-01-05', averageMood: 7.0, entryCount: 2 },
  { date: '2025-01-10', averageMood: 8.5, entryCount: 1 },
]

const mockActivityStats = [
  { activityType: 'EXERCISE', totalLogs: 5, completedLogs: 4, completionRate: 80.0 },
]

const mockGoalProgress = [
  { status: 'IN_PROGRESS', count: 2 },
  { status: 'COMPLETED', count: 1 },
]

function setupSuccessfulMocks() {
  mockGet
    .mockResolvedValueOnce({ data: mockSummary })
    .mockResolvedValueOnce({ data: mockMoodTrends })
    .mockResolvedValueOnce({ data: mockActivityStats })
    .mockResolvedValueOnce({ data: mockGoalProgress })
}

describe('DashboardView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockGet.mockReset().mockResolvedValue({ data: {} })
  })

  it('renders page header', () => {
    const wrapper = mount(DashboardView)
    expect(wrapper.find('h1').text()).toBe('Dashboard')
    expect(wrapper.find('.subtitle').text()).toContain('mental health overview')
  })

  it('shows date range preset buttons', () => {
    const wrapper = mount(DashboardView)
    const buttons = wrapper.findAll('.preset-btn')
    expect(buttons).toHaveLength(3)
    expect(buttons[0].text()).toBe('7 days')
    expect(buttons[1].text()).toBe('30 days')
    expect(buttons[2].text()).toBe('90 days')
  })

  it('renders summary cards when data loads', async () => {
    setupSuccessfulMocks()
    const wrapper = mount(DashboardView)
    await flushPromises()

    const cards = wrapper.findAll('.summary-card')
    expect(cards).toHaveLength(4)
    expect(wrapper.text()).toContain('7.5')
    expect(wrapper.text()).toContain('80%')
    expect(wrapper.text()).toContain('1/3')
    expect(wrapper.text()).toContain('5')
  })

  it('renders chart components with data', async () => {
    setupSuccessfulMocks()
    const wrapper = mount(DashboardView)
    await flushPromises()

    expect(wrapper.find('.mock-mood-chart').exists()).toBe(true)
    expect(wrapper.find('.mock-mood-chart').text()).toContain('2 points')
    expect(wrapper.find('.mock-activity-chart').exists()).toBe(true)
    expect(wrapper.find('.mock-activity-chart').text()).toContain('1 types')
    expect(wrapper.find('.mock-goal-chart').exists()).toBe(true)
    expect(wrapper.find('.mock-goal-chart').text()).toContain('2 statuses')
  })

  it('shows loading state while fetching', async () => {
    let resolveGet: (value: unknown) => void
    mockGet.mockReturnValueOnce(
      new Promise((resolve) => {
        resolveGet = resolve
      }),
    )
    const wrapper = mount(DashboardView)
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.loading').exists()).toBe(true)

    resolveGet!({ data: mockSummary })
    // Don't await remaining - loading test is sufficient
  })

  it('shows error message on failure', async () => {
    mockGet.mockRejectedValue(new Error('Network error'))
    const wrapper = mount(DashboardView)
    await flushPromises()
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.error-message').exists()).toBe(true)
  })

  it('displays chart section titles', async () => {
    setupSuccessfulMocks()
    const wrapper = mount(DashboardView)
    await flushPromises()

    const titles = wrapper.findAll('.chart-title')
    expect(titles).toHaveLength(3)
    expect(titles[0].text()).toBe('Mood Trends')
    expect(titles[1].text()).toBe('Activity Completion')
    expect(titles[2].text()).toBe('Goal Progress')
  })

  it('displays active goals count in summary', async () => {
    setupSuccessfulMocks()
    const wrapper = mount(DashboardView)
    await flushPromises()

    expect(wrapper.text()).toContain('2 active')
  })
})
