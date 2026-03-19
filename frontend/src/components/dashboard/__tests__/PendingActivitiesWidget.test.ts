import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import PendingActivitiesWidget from '../PendingActivitiesWidget.vue'
import type { DailyChecklistItem } from '@/stores/activities'

const mockGet = vi.fn().mockResolvedValue({ data: [] })
const mockPost = vi.fn().mockResolvedValue({ data: {} })

vi.mock('@/services/api', () => ({
  default: {
    get: (...args: unknown[]) => mockGet(...args),
    post: (...args: unknown[]) => mockPost(...args),
  },
}))

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: { template: '<div />' } },
    { path: '/activities/new', name: 'activity-new', component: { template: '<div />' } },
  ],
})

function makeChecklistItem(overrides: Partial<DailyChecklistItem> = {}): DailyChecklistItem {
  return {
    activityId: 1,
    activityName: 'Morning Run',
    activityType: 'EXERCISE',
    date: '2026-03-19',
    logId: null,
    completed: false,
    notes: null,
    moodRating: null,
    ...overrides,
  }
}

describe('PendingActivitiesWidget', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockGet.mockReset().mockResolvedValue({ data: [] })
    mockPost.mockReset().mockResolvedValue({
      data: {
        id: 10,
        activityId: 1,
        logDate: '2026-03-19',
        completed: true,
        notes: '',
        moodRating: null,
      },
    })
  })

  it("calls fetchChecklist on mount with today's date", async () => {
    mount(PendingActivitiesWidget, { global: { plugins: [router] } })
    await flushPromises()
    expect(mockGet).toHaveBeenCalledWith(
      '/activities/checklist',
      expect.objectContaining({
        params: expect.objectContaining({ date: expect.stringMatching(/^\d{4}-\d{2}-\d{2}$/) }),
      }),
    )
  })

  it('shows empty state when all items are completed', async () => {
    mockGet.mockResolvedValue({
      data: [makeChecklistItem({ completed: true })],
    })
    const wrapper = mount(PendingActivitiesWidget, { global: { plugins: [router] } })
    await flushPromises()
    expect(wrapper.find('[data-testid="empty-state"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Plan activities for your goals')
  })

  it('shows empty state when checklist is empty', async () => {
    mockGet.mockResolvedValue({ data: [] })
    const wrapper = mount(PendingActivitiesWidget, { global: { plugins: [router] } })
    await flushPromises()
    expect(wrapper.find('[data-testid="empty-state"]').exists()).toBe(true)
  })

  it('shows pending items filtered from checklist', async () => {
    mockGet.mockResolvedValue({
      data: [
        makeChecklistItem({ activityId: 1, activityName: 'Morning Run', completed: false }),
        makeChecklistItem({ activityId: 2, activityName: 'Meditation', completed: true }),
        makeChecklistItem({ activityId: 3, activityName: 'Journaling', completed: false }),
      ],
    })
    const wrapper = mount(PendingActivitiesWidget, { global: { plugins: [router] } })
    await flushPromises()
    expect(wrapper.find('[data-testid="checklist"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Morning Run')
    expect(wrapper.text()).not.toContain('Meditation')
    expect(wrapper.text()).toContain('Journaling')
  })

  it('shows error state when fetch fails', async () => {
    mockGet.mockRejectedValue(new Error('Network error'))
    const wrapper = mount(PendingActivitiesWidget, { global: { plugins: [router] } })
    await flushPromises()
    expect(wrapper.find('[data-testid="error-state"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Failed to load checklist')
  })

  it('calls logActivity when check button is clicked', async () => {
    mockGet.mockResolvedValue({
      data: [makeChecklistItem({ activityId: 5, activityName: 'Yoga', completed: false })],
    })
    const wrapper = mount(PendingActivitiesWidget, { global: { plugins: [router] } })
    await flushPromises()

    const btn = wrapper.find('[data-testid="check-btn-5"]')
    expect(btn.exists()).toBe(true)
    await btn.trigger('click')
    await flushPromises()

    expect(mockPost).toHaveBeenCalledWith(
      '/activities/5/logs',
      expect.objectContaining({ completed: true }),
    )
  })

  it('renders a link to /activities/new in the empty state', async () => {
    mockGet.mockResolvedValue({ data: [] })
    const wrapper = mount(PendingActivitiesWidget, { global: { plugins: [router] } })
    await flushPromises()
    const link = wrapper.find('[data-testid="empty-state"] a')
    expect(link.exists()).toBe(true)
    expect(link.attributes('href')).toContain('/activities/new')
  })
})
