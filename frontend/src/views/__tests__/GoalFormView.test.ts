import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import GoalFormView from '../GoalFormView.vue'

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRoute: () => ({ name: 'goal-new', params: {} }),
  useRouter: () => ({ push: mockPush }),
}))

const mockPost = vi.fn().mockResolvedValue({
  data: {
    id: 1,
    title: 'Test Goal',
    description: '',
    category: '',
    targetDate: null,
    status: 'NOT_STARTED',
    totalMilestones: 0,
    completedMilestones: 0,
    milestones: [],
    createdAt: '2025-01-01T10:00:00',
    updatedAt: '2025-01-01T10:00:00',
  },
})
const mockGet = vi.fn().mockResolvedValue({ data: [] })

vi.mock('@/services/api', () => ({
  default: {
    get: (...args: unknown[]) => mockGet(...args),
    post: (...args: unknown[]) => mockPost(...args),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}))

describe('GoalFormView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockPush.mockClear()
    mockPost.mockClear()
    mockGet.mockReset().mockResolvedValue({ data: [] })
  })

  it('renders create form heading', () => {
    const wrapper = mount(GoalFormView)
    expect(wrapper.find('h1').text()).toBe('New Goal')
  })

  it('renders all form fields', () => {
    const wrapper = mount(GoalFormView)
    expect(wrapper.find('#goal-title').exists()).toBe(true)
    expect(wrapper.find('#goal-description').exists()).toBe(true)
    expect(wrapper.find('#goal-category').exists()).toBe(true)
    expect(wrapper.find('#goal-target-date').exists()).toBe(true)
  })

  it('has title field as required', () => {
    const wrapper = mount(GoalFormView)
    const titleInput = wrapper.find('#goal-title')
    expect(titleInput.attributes('required')).toBeDefined()
  })

  it('renders action buttons', () => {
    const wrapper = mount(GoalFormView)
    const buttons = wrapper.findAll('.form-actions .btn')
    expect(buttons).toHaveLength(2)
    expect(buttons[0].text()).toBe('Cancel')
    expect(buttons[1].text()).toContain('Create Goal')
  })

  it('navigates back on cancel', async () => {
    const wrapper = mount(GoalFormView)
    await wrapper.find('.btn-secondary').trigger('click')
    expect(mockPush).toHaveBeenCalledWith({ name: 'goals' })
  })

  it('navigates back on back button click', async () => {
    const wrapper = mount(GoalFormView)
    await wrapper.find('.btn-back').trigger('click')
    expect(mockPush).toHaveBeenCalledWith({ name: 'goals' })
  })

  it('submits form and navigates to goals', async () => {
    const wrapper = mount(GoalFormView)

    await wrapper.find('#goal-title').setValue('New Goal')
    await wrapper.find('#goal-description').setValue('Description')
    await wrapper.find('#goal-category').setValue('Health')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(mockPost).toHaveBeenCalledWith('/goals', expect.objectContaining({ title: 'New Goal' }))
    expect(mockPush).toHaveBeenCalledWith({ name: 'goals' })
  })
})
