import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import JournalFormView from '../JournalFormView.vue'

const mockPush = vi.fn()
const routeState = vi.hoisted(() => ({ name: 'journal-new', params: {} as Record<string, string> }))
vi.mock('vue-router', () => ({
  useRoute: () => routeState,
  useRouter: () => ({ push: mockPush }),
}))

const mockGet = vi.fn().mockResolvedValue({ data: [] })
const mockPost = vi.fn()
const mockPut = vi.fn()
const mockDelete = vi.fn()
vi.mock('@/services/api', () => ({
  default: {
    get: (...args: unknown[]) => mockGet(...args),
    post: (...args: unknown[]) => mockPost(...args),
    put: (...args: unknown[]) => mockPut(...args),
    patch: vi.fn(),
    delete: (...args: unknown[]) => mockDelete(...args),
  },
}))

describe('JournalFormView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockPush.mockClear()
    routeState.name = 'journal-new'
    routeState.params = {}
    mockGet.mockReset().mockResolvedValue({ data: [] })
    mockPost.mockReset()
    mockPut.mockReset()
    mockDelete.mockReset()
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
    mockPost.mockResolvedValue({
      data: { id: 1, entryDate: '2025-01-15', title: 'Test', tags: [] },
    })

    const wrapper = mount(JournalFormView)
    await wrapper.find('#entry-title').setValue('Test entry')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(mockPost).toHaveBeenCalledWith('/journal', expect.any(Object))
    expect(mockPush).toHaveBeenCalledWith({ name: 'journal' })
  })

  it('has sharing checkbox', () => {
    const wrapper = mount(JournalFormView)
    expect(wrapper.find('.checkbox').exists()).toBe(true)
    expect(wrapper.text()).toContain('Share with therapist')
  })

  it('adds and removes tags from the form', async () => {
    const wrapper = mount(JournalFormView)

    await wrapper.find('#tag-input').setValue('gratitude')
    await wrapper.find('#tag-input').trigger('keydown.enter')
    expect(wrapper.text()).toContain('gratitude')

    await wrapper.find('.tag-remove').trigger('click')
    expect(wrapper.text()).not.toContain('gratitude')
  })

  it('loads an existing entry for editing and updates it', async () => {
    routeState.name = 'journal-edit'
    routeState.params = { id: '7' }
    mockGet.mockResolvedValueOnce({
      data: [
        {
          id: 7,
          entryDate: '2025-01-12',
          title: 'Existing title',
          content: 'Existing content',
          mood: 8,
          tags: ['sleep'],
          sharedWithTherapist: true,
        },
      ],
    })
    mockPut.mockResolvedValueOnce({
      data: { id: 7, entryDate: '2025-01-12', title: 'Updated', tags: ['sleep'] },
    })

    const wrapper = mount(JournalFormView)
    await flushPromises()

    expect((wrapper.find('#entry-title').element as HTMLInputElement).value).toBe('Existing title')
    expect(wrapper.find('h1').text()).toBe('Edit Entry')

    await wrapper.find('#entry-title').setValue('Updated')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(mockPut).toHaveBeenCalledWith('/journal/7', expect.any(Object))
    expect(mockPush).toHaveBeenCalledWith({ name: 'journal' })
  })

  it('shows and dismisses submission errors', async () => {
    mockPost.mockRejectedValueOnce(new Error('boom'))

    const wrapper = mount(JournalFormView)
    await wrapper.find('#entry-title').setValue('Test entry')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.find('.error-banner').exists()).toBe(true)
    await wrapper.find('.error-dismiss').trigger('click')
    expect(wrapper.find('.error-banner').exists()).toBe(false)
    expect(mockPush).not.toHaveBeenCalled()
  })
})
