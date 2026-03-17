import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import InterviewFormView from '../InterviewFormView.vue'

const mockPush = vi.fn()
const routeState = vi.hoisted(() => ({
  name: 'interview-new',
  params: {} as Record<string, string>,
}))
vi.mock('vue-router', () => ({
  useRoute: () => routeState,
  useRouter: () => ({ push: mockPush }),
}))

const mockGet = vi.fn()
const mockPost = vi.fn()
const mockPut = vi.fn()
const mockDelete = vi.fn()
vi.mock('@/services/api', () => ({
  default: {
    get: (...args: unknown[]) => mockGet(...args),
    post: (...args: unknown[]) => mockPost(...args),
    put: (...args: unknown[]) => mockPut(...args),
    delete: (...args: unknown[]) => mockDelete(...args),
  },
}))

describe('InterviewFormView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockPush.mockClear()
    routeState.name = 'interview-new'
    routeState.params = {}
    mockGet.mockReset()
    mockPost.mockReset()
    mockPut.mockReset()
    mockDelete.mockReset()
  })

  it('renders new interview form', () => {
    const wrapper = mount(InterviewFormView)
    expect(wrapper.find('h1').text()).toBe('New Interview')
  })

  it('renders all form fields', () => {
    const wrapper = mount(InterviewFormView)

    expect(wrapper.find('#interview-date').exists()).toBe(true)
    expect(wrapper.find('#mood-before').exists()).toBe(true)
    expect(wrapper.find('#mood-after').exists()).toBe(true)
    expect(wrapper.find('#topic-input').exists()).toBe(true)
    expect(wrapper.find('#medication-changes').exists()).toBe(true)
    expect(wrapper.find('#recommendations').exists()).toBe(true)
    expect(wrapper.find('#notes').exists()).toBe(true)
  })

  it('adds topic when add button is clicked', async () => {
    const wrapper = mount(InterviewFormView)

    await wrapper.find('#topic-input').setValue('anxiety')
    await wrapper.find('.topic-input-row .btn').trigger('click')

    const chips = wrapper.findAll('.topic-chip')
    expect(chips).toHaveLength(1)
    expect(chips[0].text()).toContain('anxiety')
  })

  it('removes topic when remove button clicked', async () => {
    const wrapper = mount(InterviewFormView)

    await wrapper.find('#topic-input').setValue('anxiety')
    await wrapper.find('.topic-input-row .btn').trigger('click')
    expect(wrapper.findAll('.topic-chip')).toHaveLength(1)

    await wrapper.find('.chip-remove').trigger('click')
    expect(wrapper.findAll('.topic-chip')).toHaveLength(0)
  })

  it('does not add duplicate topics', async () => {
    const wrapper = mount(InterviewFormView)

    await wrapper.find('#topic-input').setValue('anxiety')
    await wrapper.find('.topic-input-row .btn').trigger('click')
    await wrapper.find('#topic-input').setValue('anxiety')
    await wrapper.find('.topic-input-row .btn').trigger('click')

    expect(wrapper.findAll('.topic-chip')).toHaveLength(1)
  })

  it('navigates back on cancel', async () => {
    const wrapper = mount(InterviewFormView)
    const cancelBtn = wrapper.findAll('.form-actions .btn').find((b) => b.text() === 'Cancel')

    await cancelBtn!.trigger('click')

    expect(mockPush).toHaveBeenCalledWith({ name: 'interviews' })
  })

  it('submits form and navigates to detail', async () => {
    mockPost.mockResolvedValue({
      data: { id: 5, interviewDate: '2025-01-15' },
    })

    const wrapper = mount(InterviewFormView)
    await wrapper.find('#interview-date').setValue('2025-01-15')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(mockPost).toHaveBeenCalledWith('/interviews', expect.any(Object))
    expect(mockPush).toHaveBeenCalledWith({ name: 'interview-detail', params: { id: 5 } })
  })

  it('has back button', () => {
    const wrapper = mount(InterviewFormView)
    expect(wrapper.find('.btn-back').exists()).toBe(true)
    expect(wrapper.find('.btn-back').text()).toContain('Back')
  })

  it('adds a topic on Enter key', async () => {
    const wrapper = mount(InterviewFormView)

    await wrapper.find('#topic-input').setValue('sleep')
    await wrapper.find('#topic-input').trigger('keydown', { key: 'Enter' })

    expect(wrapper.findAll('.topic-chip')).toHaveLength(1)
    expect(wrapper.text()).toContain('sleep')
  })

  it('loads an existing interview and updates it', async () => {
    routeState.name = 'interview-edit'
    routeState.params = { id: '9' }
    mockGet.mockResolvedValueOnce({
      data: {
        id: 9,
        interviewDate: '2025-01-20',
        moodBefore: 4,
        moodAfter: 6,
        topics: ['anxiety'],
        medicationChanges: 'Lower dose',
        recommendations: 'Walk more',
        notes: 'Follow up',
        hasAudio: true,
        transcriptionText: 'Transcript',
        audioExpiresAt: null,
      },
    })
    mockPut.mockResolvedValueOnce({
      data: { id: 9, interviewDate: '2025-01-20' },
    })

    const wrapper = mount(InterviewFormView, {
      global: {
        stubs: {
          AudioSection: { template: '<div class="audio-section-stub" />' },
        },
      },
    })
    await flushPromises()

    expect(wrapper.find('h1').text()).toBe('Edit Interview')
    expect((wrapper.find('#medication-changes').element as HTMLTextAreaElement).value).toBe(
      'Lower dose',
    )
    expect(wrapper.find('.audio-section-stub').exists()).toBe(true)

    await wrapper.find('#notes').setValue('Updated notes')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(mockPut).toHaveBeenCalledWith('/interviews/9', expect.any(Object))
    expect(mockPush).toHaveBeenCalledWith({ name: 'interview-detail', params: { id: 9 } })
  })

  it('goes back to the detail page while editing', async () => {
    routeState.name = 'interview-edit'
    routeState.params = { id: '11' }
    mockGet.mockResolvedValueOnce({
      data: {
        id: 11,
        interviewDate: '2025-01-20',
        moodBefore: null,
        moodAfter: null,
        topics: [],
        medicationChanges: null,
        recommendations: null,
        notes: null,
        hasAudio: false,
        transcriptionText: null,
        audioExpiresAt: null,
      },
    })

    const wrapper = mount(InterviewFormView)
    await flushPromises()

    await wrapper.find('.btn-back').trigger('click')

    expect(mockPush).toHaveBeenCalledWith({ name: 'interview-detail', params: { id: 11 } })
  })

  it('shows an error banner when save fails and allows dismissing it', async () => {
    mockPost.mockRejectedValueOnce(new Error('save failed'))

    const wrapper = mount(InterviewFormView)
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.find('.error-banner').exists()).toBe(true)
    await wrapper.find('.error-dismiss').trigger('click')
    expect(wrapper.find('.error-banner').exists()).toBe(false)
    expect(mockPush).not.toHaveBeenCalled()
  })
})
