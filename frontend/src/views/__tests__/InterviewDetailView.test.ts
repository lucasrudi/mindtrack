import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { nextTick } from 'vue'
import InterviewDetailView from '../InterviewDetailView.vue'
import { useInterviewsStore } from '@/stores/interviews'

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRoute: () => ({ params: { id: '5' } }),
  useRouter: () => ({ push: mockPush }),
}))

const mockGet = vi.fn()
const mockDelete = vi.fn()

vi.mock('@/services/api', () => ({
  default: {
    get: (...args: unknown[]) => mockGet(...args),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: (...args: unknown[]) => mockDelete(...args),
  },
}))

const sampleInterview = {
  id: 5,
  interviewDate: '2025-03-10',
  moodBefore: 4,
  moodAfter: 7,
  topics: ['anxiety', 'sleep', 'medication'],
  medicationChanges: 'Increased Sertraline to 100mg',
  recommendations: 'Practice breathing exercises daily',
  notes: 'Good progress noted',
  hasAudio: false,
  transcriptionText: null,
  audioExpiresAt: null,
  createdAt: '2025-03-10T09:00:00',
  updatedAt: '2025-03-10T09:00:00',
}

describe('InterviewDetailView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockPush.mockClear()
    mockGet.mockReset().mockResolvedValue({ data: sampleInterview })
    mockDelete.mockReset().mockResolvedValue({})
  })

  it('shows loading state before data arrives', async () => {
    mockGet.mockReturnValue(new Promise(() => {}))
    const wrapper = mount(InterviewDetailView)
    await nextTick()
    expect(wrapper.find('.loading').exists()).toBe(true)
    expect(wrapper.text()).toContain('Loading interview...')
  })

  it('renders interview details after loading', async () => {
    const wrapper = mount(InterviewDetailView)
    await flushPromises()

    // Date formatted as "Monday, March 10, 2025"
    expect(wrapper.find('h1').text()).toContain('March 10, 2025')
  })

  it('renders mood before and after', async () => {
    const wrapper = mount(InterviewDetailView)
    await flushPromises()

    const moodNumbers = wrapper.findAll('.mood-number')
    expect(moodNumbers[0].text()).toBe('4/10')
    expect(moodNumbers[1].text()).toBe('7/10')
  })

  it('renders null mood as "Not recorded"', async () => {
    mockGet.mockResolvedValue({
      data: { ...sampleInterview, moodBefore: null, moodAfter: null },
    })
    const wrapper = mount(InterviewDetailView)
    await flushPromises()

    const moodNumbers = wrapper.findAll('.mood-number')
    expect(moodNumbers[0].text()).toBe('Not recorded')
    expect(moodNumbers[1].text()).toBe('Not recorded')
  })

  it('renders topic chips', async () => {
    const wrapper = mount(InterviewDetailView)
    await flushPromises()

    const chips = wrapper.findAll('.topic-chip')
    expect(chips).toHaveLength(3)
    expect(chips[0].text()).toBe('anxiety')
    expect(chips[1].text()).toBe('sleep')
    expect(chips[2].text()).toBe('medication')
  })

  it('renders medication changes section', async () => {
    const wrapper = mount(InterviewDetailView)
    await flushPromises()

    expect(wrapper.text()).toContain('Medication changes')
    expect(wrapper.text()).toContain('Increased Sertraline to 100mg')
  })

  it('renders recommendations section', async () => {
    const wrapper = mount(InterviewDetailView)
    await flushPromises()

    expect(wrapper.text()).toContain('Recommendations')
    expect(wrapper.text()).toContain('Practice breathing exercises daily')
  })

  it('renders notes section', async () => {
    const wrapper = mount(InterviewDetailView)
    await flushPromises()

    expect(wrapper.text()).toContain('Notes')
    expect(wrapper.text()).toContain('Good progress noted')
  })

  it('hides optional sections when fields are null', async () => {
    mockGet.mockResolvedValue({
      data: {
        ...sampleInterview,
        topics: [],
        medicationChanges: null,
        recommendations: null,
        notes: null,
      },
    })
    const wrapper = mount(InterviewDetailView)
    await flushPromises()

    expect(wrapper.find('.topics-section').exists()).toBe(false)
    expect(wrapper.text()).not.toContain('Medication changes')
    expect(wrapper.text()).not.toContain('Recommendations')
    expect(wrapper.text()).not.toContain('Notes')
  })

  it('shows error message when fetch fails', async () => {
    // Spy on fetchInterview to set error state without re-throwing
    const store = useInterviewsStore()
    const spy = vi.spyOn(store, 'fetchInterview').mockImplementation(async () => {
      store.error = 'Failed to load interview'
      return undefined as never
    })
    const wrapper = mount(InterviewDetailView)
    await flushPromises()
    spy.mockRestore()

    expect(wrapper.find('.error-message').exists()).toBe(true)
    expect(wrapper.text()).toContain('Failed to load interview')
  })

  it('retry button calls fetchInterview again', async () => {
    // First call fails (set error), second call succeeds
    const store = useInterviewsStore()
    let callCount = 0
    const spy = vi.spyOn(store, 'fetchInterview').mockImplementation(async () => {
      callCount++
      if (callCount === 1) {
        store.error = 'Failed to load interview'
        return undefined as never
      }
      store.error = null
      store.currentInterview = sampleInterview
      return sampleInterview as never
    })
    const wrapper = mount(InterviewDetailView)
    await flushPromises()

    expect(wrapper.find('.error-message').exists()).toBe(true)
    await wrapper.find('.error-message .btn').trigger('click')
    await flushPromises()
    spy.mockRestore()

    expect(callCount).toBe(2)
  })

  it('navigates back when back button is clicked', async () => {
    const wrapper = mount(InterviewDetailView)
    await flushPromises()

    await wrapper.find('.btn-back').trigger('click')
    expect(mockPush).toHaveBeenCalledWith({ name: 'interviews' })
  })

  it('shows Edit and Delete buttons when interview is loaded', async () => {
    const wrapper = mount(InterviewDetailView)
    await flushPromises()

    const buttons = wrapper.findAll('.header-actions .btn')
    expect(buttons).toHaveLength(2)
    expect(buttons[0].text()).toBe('Edit')
    expect(buttons[1].text()).toBe('Delete')
  })

  it('navigates to edit page when Edit is clicked', async () => {
    const wrapper = mount(InterviewDetailView)
    await flushPromises()

    await wrapper.find('.header-actions .btn-secondary').trigger('click')
    expect(mockPush).toHaveBeenCalledWith({ name: 'interview-edit', params: { id: 5 } })
  })

  it('opens delete confirmation modal when Delete is clicked', async () => {
    const wrapper = mount(InterviewDetailView)
    await flushPromises()

    expect(wrapper.find('.modal-overlay').exists()).toBe(false)
    await wrapper.find('.header-actions .btn-danger').trigger('click')
    expect(wrapper.find('.modal-overlay').exists()).toBe(true)
    expect(wrapper.text()).toContain('Delete interview?')
  })

  it('closes delete modal when Cancel is clicked', async () => {
    const wrapper = mount(InterviewDetailView)
    await flushPromises()

    await wrapper.find('.header-actions .btn-danger').trigger('click')
    expect(wrapper.find('.modal-overlay').exists()).toBe(true)

    await wrapper.find('.modal-actions .btn-secondary').trigger('click')
    expect(wrapper.find('.modal-overlay').exists()).toBe(false)
  })

  it('deletes interview and navigates to interviews list', async () => {
    const wrapper = mount(InterviewDetailView)
    await flushPromises()

    await wrapper.find('.header-actions .btn-danger').trigger('click')
    await wrapper.find('.modal-actions .btn-danger').trigger('click')
    await flushPromises()

    expect(mockDelete).toHaveBeenCalledWith('/interviews/5')
    expect(mockPush).toHaveBeenCalledWith({ name: 'interviews' })
  })

  it('renders footer with created timestamp', async () => {
    const wrapper = mount(InterviewDetailView)
    await flushPromises()

    expect(wrapper.find('.detail-footer').exists()).toBe(true)
    expect(wrapper.find('.detail-footer').text()).toContain('Created')
  })
})
