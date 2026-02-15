import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ActivityFormView from '../ActivityFormView.vue'

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRoute: () => ({ name: 'activity-new', params: {} }),
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

describe('ActivityFormView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockPush.mockClear()
  })

  it('renders new activity form', () => {
    const wrapper = mount(ActivityFormView)
    expect(wrapper.find('h1').text()).toBe('New Activity')
  })

  it('renders all form fields', () => {
    const wrapper = mount(ActivityFormView)
    expect(wrapper.find('#activity-type').exists()).toBe(true)
    expect(wrapper.find('#activity-name').exists()).toBe(true)
    expect(wrapper.find('#activity-description').exists()).toBe(true)
    expect(wrapper.find('#activity-frequency').exists()).toBe(true)
  })

  it('renders activity type options', () => {
    const wrapper = mount(ActivityFormView)
    const options = wrapper.findAll('#activity-type option')
    expect(options.length).toBeGreaterThanOrEqual(8)
  })

  it('navigates back on cancel', async () => {
    const wrapper = mount(ActivityFormView)
    const cancelBtn = wrapper.findAll('.form-actions .btn').find((b) => b.text() === 'Cancel')

    await cancelBtn!.trigger('click')
    expect(mockPush).toHaveBeenCalledWith({ name: 'activities' })
  })

  it('submits form and navigates to activities list', async () => {
    const { default: api } = await import('@/services/api')
    const mockApi = api as unknown as { post: ReturnType<typeof vi.fn> }
    mockApi.post.mockResolvedValue({
      data: { id: 1, type: 'EXERCISE', name: 'Test', active: true },
    })

    const wrapper = mount(ActivityFormView)
    await wrapper.find('#activity-name').setValue('Morning jog')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(mockApi.post).toHaveBeenCalledWith('/activities', expect.any(Object))
    expect(mockPush).toHaveBeenCalledWith({ name: 'activities' })
  })

  it('has back button', () => {
    const wrapper = mount(ActivityFormView)
    expect(wrapper.find('.btn-back').exists()).toBe(true)
    expect(wrapper.find('.btn-back').text()).toContain('Back')
  })

  it('shows correct submit button text', () => {
    const wrapper = mount(ActivityFormView)
    const submitBtn = wrapper
      .findAll('.form-actions .btn')
      .find((b) => b.text().includes('Activity'))
    expect(submitBtn!.text()).toBe('Create Activity')
  })
})
