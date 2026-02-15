import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import JournalFormView from '../JournalFormView.vue'

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRoute: () => ({ name: 'journal-new', params: {} }),
  useRouter: () => ({ push: mockPush }),
}))

vi.mock('@/services/api', () => ({
  default: {
    get: vi.fn().mockResolvedValue({ data: [] }),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}))

describe('JournalFormView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockPush.mockClear()
  })

  it('renders new entry form', () => {
    const wrapper = mount(JournalFormView)
    expect(wrapper.find('h1').text()).toBe('New Journal Entry')
  })

  it('renders all form fields', () => {
    const wrapper = mount(JournalFormView)
    expect(wrapper.find('#entry-date').exists()).toBe(true)
    expect(wrapper.find('#entry-title').exists()).toBe(true)
    expect(wrapper.find('#entry-content').exists()).toBe(true)
    expect(wrapper.find('#entry-mood').exists()).toBe(true)
    expect(wrapper.find('#tag-input').exists()).toBe(true)
  })

  it('has back button', () => {
    const wrapper = mount(JournalFormView)
    expect(wrapper.find('.btn-back').exists()).toBe(true)
    expect(wrapper.find('.btn-back').text()).toContain('Back')
  })

  it('navigates back on cancel', async () => {
    const wrapper = mount(JournalFormView)
    const cancelBtn = wrapper.findAll('.form-actions .btn').find((b) => b.text() === 'Cancel')

    await cancelBtn!.trigger('click')
    expect(mockPush).toHaveBeenCalledWith({ name: 'journal' })
  })

  it('shows correct submit button text', () => {
    const wrapper = mount(JournalFormView)
    const submitBtn = wrapper.findAll('.form-actions .btn').find((b) => b.text().includes('Entry'))
    expect(submitBtn!.text()).toBe('Create Entry')
  })

  it('submits form and navigates to journal list', async () => {
    const { default: api } = await import('@/services/api')
    const mockApi = api as unknown as { post: ReturnType<typeof vi.fn> }
    mockApi.post.mockResolvedValue({
      data: { id: 1, entryDate: '2025-01-15', title: 'Test', tags: [] },
    })

    const wrapper = mount(JournalFormView)
    await wrapper.find('#entry-title').setValue('Test entry')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(mockApi.post).toHaveBeenCalledWith('/journal', expect.any(Object))
    expect(mockPush).toHaveBeenCalledWith({ name: 'journal' })
  })

  it('has sharing checkbox', () => {
    const wrapper = mount(JournalFormView)
    expect(wrapper.find('.checkbox').exists()).toBe(true)
    expect(wrapper.text()).toContain('Share with therapist')
  })
})
