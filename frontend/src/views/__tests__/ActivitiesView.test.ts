import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ActivitiesView from '../ActivitiesView.vue'

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush }),
}))

const mockGet = vi.fn().mockResolvedValue({ data: [] })
const mockPost = vi.fn()
const mockPatch = vi.fn()
vi.mock('@/services/api', () => ({
  default: {
    get: (...args: unknown[]) => mockGet(...args),
    post: (...args: unknown[]) => mockPost(...args),
    put: vi.fn(),
    patch: (...args: unknown[]) => mockPatch(...args),
    delete: vi.fn(),
  },
}))

const sampleActivity = {
  id: 1,
  type: 'EXERCISE',
  name: 'Morning jog',
  description: '30 minutes',
  frequency: 'Daily',
  linkedInterviewId: null,
  active: true,
  createdAt: '2025-01-15T10:00:00',
}

const sampleChecklistItem = {
  activityId: 1,
  activityName: 'Morning jog',
  activityType: 'EXERCISE',
  date: '2025-01-15',
  logId: null,
  completed: false,
  notes: null,
  moodRating: null,
}

describe('ActivitiesView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockPush.mockClear()
    mockGet.mockReset().mockResolvedValue({ data: [] })
    mockPost.mockReset()
    mockPatch.mockReset()
  })

  it('renders page header', () => {
    const wrapper = mount(ActivitiesView)
    expect(wrapper.find('h1').text()).toBe('Activities')
    expect(wrapper.find('.subtitle').text()).toContain('daily habits')
  })

  it('renders new activity button', () => {
    const wrapper = mount(ActivitiesView)
    const btn = wrapper.find('.btn-primary')
    expect(btn.exists()).toBe(true)
    expect(btn.text()).toContain('New Activity')
  })

  it('renders two tabs', () => {
    const wrapper = mount(ActivitiesView)
    const tabs = wrapper.findAll('.tab')
    expect(tabs).toHaveLength(2)
    expect(tabs[0].text()).toBe('Daily Checklist')
    expect(tabs[1].text()).toBe('All Activities')
  })

  it('shows checklist tab by default', () => {
    const wrapper = mount(ActivitiesView)
    expect(wrapper.find('.checklist-panel').exists()).toBe(true)
    expect(wrapper.find('.activities-panel').exists()).toBe(false)
  })

  it('navigates to new activity form', async () => {
    const wrapper = mount(ActivitiesView)
    await wrapper.find('.page-header .btn-primary').trigger('click')
    expect(mockPush).toHaveBeenCalledWith({ name: 'activity-new' })
  })

  it('shows empty state on checklist when no activities', async () => {
    const wrapper = mount(ActivitiesView)
    await flushPromises()
    expect(wrapper.find('.empty-state').exists()).toBe(true)
    expect(wrapper.text()).toContain('No active activities')
  })

  it('renders checklist items when data loaded', async () => {
    mockGet.mockImplementation((url: string) => {
      if (typeof url === 'string' && url.includes('checklist')) {
        return Promise.resolve({ data: [sampleChecklistItem] })
      }
      return Promise.resolve({ data: [sampleActivity] })
    })

    const wrapper = mount(ActivitiesView)
    await flushPromises()

    const items = wrapper.findAll('.checklist-item')
    expect(items).toHaveLength(1)
    expect(wrapper.text()).toContain('Morning jog')
  })

  it('switches to activities tab', async () => {
    mockGet.mockResolvedValue({ data: [sampleActivity] })

    const wrapper = mount(ActivitiesView)
    await flushPromises()

    await wrapper.findAll('.tab')[1].trigger('click')
    expect(wrapper.find('.activities-panel').exists()).toBe(true)
  })

  it('renders activity cards in activities tab', async () => {
    mockGet.mockResolvedValue({ data: [sampleActivity] })

    const wrapper = mount(ActivitiesView)
    await flushPromises()
    await wrapper.findAll('.tab')[1].trigger('click')

    const cards = wrapper.findAll('.activity-card')
    expect(cards).toHaveLength(1)
    expect(wrapper.text()).toContain('Morning jog')
    expect(wrapper.text()).toContain('Exercise')
  })

  it('has date picker for checklist', () => {
    const wrapper = mount(ActivitiesView)
    expect(wrapper.find('#checklist-date').exists()).toBe(true)
  })
})
