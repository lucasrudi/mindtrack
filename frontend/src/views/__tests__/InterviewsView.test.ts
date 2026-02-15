import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import InterviewsView from '../InterviewsView.vue'

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
    delete: vi.fn(),
  },
}))

const sampleInterview = {
  id: 1,
  interviewDate: '2025-01-15',
  moodBefore: 5,
  moodAfter: 7,
  topics: ['anxiety', 'sleep'],
  medicationChanges: null,
  recommendations: null,
  notes: 'Good session',
  hasAudio: false,
  createdAt: '2025-01-15T10:00:00',
  updatedAt: '2025-01-15T10:00:00',
}

describe('InterviewsView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockPush.mockClear()
    mockGet.mockReset().mockResolvedValue({ data: [] })
  })

  it('renders page header', () => {
    const wrapper = mount(InterviewsView)
    expect(wrapper.find('h1').text()).toBe('Interviews')
    expect(wrapper.find('.subtitle').text()).toContain('psychiatrist sessions')
  })

  it('renders new interview button', () => {
    const wrapper = mount(InterviewsView)
    const btn = wrapper.find('.btn-primary')
    expect(btn.exists()).toBe(true)
    expect(btn.text()).toContain('New Interview')
  })

  it('shows empty state when no interviews', async () => {
    const wrapper = mount(InterviewsView)
    await flushPromises()

    expect(wrapper.find('.empty-state').exists()).toBe(true)
    expect(wrapper.text()).toContain('No interviews yet')
  })

  it('navigates to new interview form', async () => {
    const wrapper = mount(InterviewsView)
    await wrapper.find('.page-header .btn-primary').trigger('click')

    expect(mockPush).toHaveBeenCalledWith({ name: 'interview-new' })
  })

  it('renders interview cards when data is loaded', async () => {
    mockGet.mockResolvedValue({ data: [sampleInterview] })

    const wrapper = mount(InterviewsView)
    await flushPromises()

    const cards = wrapper.findAll('.interview-card')
    expect(cards).toHaveLength(1)
    expect(wrapper.text()).toContain('Good session')
  })

  it('navigates to interview detail on card click', async () => {
    mockGet.mockResolvedValue({
      data: [{ ...sampleInterview, id: 42 }],
    })

    const wrapper = mount(InterviewsView)
    await flushPromises()
    await wrapper.find('.interview-card').trigger('click')

    expect(mockPush).toHaveBeenCalledWith({ name: 'interview-detail', params: { id: 42 } })
  })

  it('displays mood values in cards', async () => {
    mockGet.mockResolvedValue({ data: [sampleInterview] })

    const wrapper = mount(InterviewsView)
    await flushPromises()

    const moodValues = wrapper.findAll('.mood-value')
    expect(moodValues[0].text()).toBe('5/10')
    expect(moodValues[1].text()).toBe('7/10')
  })

  it('displays topic chips', async () => {
    mockGet.mockResolvedValue({ data: [sampleInterview] })

    const wrapper = mount(InterviewsView)
    await flushPromises()

    const chips = wrapper.findAll('.topic-chip')
    expect(chips).toHaveLength(2)
    expect(chips[0].text()).toContain('anxiety')
    expect(chips[1].text()).toContain('sleep')
  })
})
