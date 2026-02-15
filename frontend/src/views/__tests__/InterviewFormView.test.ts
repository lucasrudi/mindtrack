import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import InterviewFormView from '../InterviewFormView.vue'

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRoute: () => ({ name: 'interview-new', params: {} }),
  useRouter: () => ({ push: mockPush }),
}))

vi.mock('@/services/api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}))

describe('InterviewFormView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockPush.mockClear()
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
    const { default: api } = await import('@/services/api')
    const mockApi = api as unknown as { post: ReturnType<typeof vi.fn> }
    mockApi.post.mockResolvedValue({
      data: { id: 5, interviewDate: '2025-01-15' },
    })

    const wrapper = mount(InterviewFormView)
    await wrapper.find('#interview-date').setValue('2025-01-15')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(mockApi.post).toHaveBeenCalledWith('/interviews', expect.any(Object))
    expect(mockPush).toHaveBeenCalledWith({ name: 'interview-detail', params: { id: 5 } })
  })

  it('has back button', () => {
    const wrapper = mount(InterviewFormView)
    expect(wrapper.find('.btn-back').exists()).toBe(true)
    expect(wrapper.find('.btn-back').text()).toContain('Back')
  })
})
