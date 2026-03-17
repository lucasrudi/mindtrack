import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { nextTick } from 'vue'
import GoalDetailView from '../GoalDetailView.vue'
import { useGoalsStore } from '@/stores/goals'

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRoute: () => ({ params: { id: '1' } }),
  useRouter: () => ({ push: mockPush }),
}))

const mockGet = vi.fn()
const mockPost = vi.fn()
const mockPatch = vi.fn()
const mockDelete = vi.fn()

vi.mock('@/services/api', () => ({
  default: {
    get: (...args: unknown[]) => mockGet(...args),
    post: (...args: unknown[]) => mockPost(...args),
    put: vi.fn(),
    patch: (...args: unknown[]) => mockPatch(...args),
    delete: (...args: unknown[]) => mockDelete(...args),
  },
}))

const sampleGoal = {
  id: 1,
  title: 'Learn meditation',
  description: 'Practice daily for 10 minutes',
  category: 'Health',
  targetDate: '2025-12-01',
  status: 'IN_PROGRESS' as const,
  validationStatus: null,
  totalMilestones: 2,
  completedMilestones: 1,
  milestones: [
    {
      id: 10,
      goalId: 1,
      title: 'Complete intro course',
      targetDate: '2025-06-01',
      completedAt: '2025-05-15T00:00:00',
      completed: true,
      notes: 'Done!',
      createdAt: '2025-01-01T00:00:00',
    },
    {
      id: 11,
      goalId: 1,
      title: 'Practice 30 days streak',
      targetDate: null,
      completedAt: null,
      completed: false,
      notes: null,
      createdAt: '2025-01-02T00:00:00',
    },
  ],
  createdAt: '2025-01-01T10:00:00',
  updatedAt: '2025-01-01T10:00:00',
}

describe('GoalDetailView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockPush.mockClear()
    mockGet.mockReset().mockResolvedValue({ data: sampleGoal })
    mockPost.mockReset()
    mockPatch.mockReset().mockResolvedValue({ data: sampleGoal })
    mockDelete.mockReset().mockResolvedValue({})
  })

  it('shows loading state before data arrives', async () => {
    // Return a promise that never resolves so we can catch the loading state
    mockGet.mockReturnValue(new Promise(() => {}))
    const wrapper = mount(GoalDetailView)
    await nextTick()
    expect(wrapper.find('.loading').exists()).toBe(true)
    expect(wrapper.text()).toContain('Loading goal...')
  })

  it('renders goal title and details after loading', async () => {
    const wrapper = mount(GoalDetailView)
    await flushPromises()

    expect(wrapper.find('.goal-title').text()).toBe('Learn meditation')
    expect(wrapper.text()).toContain('Practice daily for 10 minutes')
    expect(wrapper.find('.goal-category').text()).toBe('Health')
  })

  it('renders status badge with correct class', async () => {
    const wrapper = mount(GoalDetailView)
    await flushPromises()

    const badge = wrapper.find('.status-badge')
    expect(badge.text()).toBe('In Progress')
    expect(badge.classes()).toContain('status-in-progress')
  })

  it('renders progress bar when milestones exist', async () => {
    const wrapper = mount(GoalDetailView)
    await flushPromises()

    expect(wrapper.find('.progress-section').exists()).toBe(true)
    expect(wrapper.text()).toContain('1/2')
    expect(wrapper.text()).toContain('50%')
  })

  it('renders milestone list', async () => {
    const wrapper = mount(GoalDetailView)
    await flushPromises()

    const items = wrapper.findAll('.milestone-item')
    expect(items).toHaveLength(2)
    expect(items[0].text()).toContain('Complete intro course')
    expect(items[1].text()).toContain('Practice 30 days streak')
  })

  it('renders completed milestone with completed class', async () => {
    const wrapper = mount(GoalDetailView)
    await flushPromises()

    const completedItem = wrapper.findAll('.milestone-item')[0]
    expect(completedItem.classes()).toContain('completed')
  })

  it('shows empty milestones message when no milestones', async () => {
    mockGet.mockResolvedValue({
      data: { ...sampleGoal, milestones: [], totalMilestones: 0, completedMilestones: 0 },
    })
    const wrapper = mount(GoalDetailView)
    await flushPromises()

    expect(wrapper.find('.milestones-empty').exists()).toBe(true)
    expect(wrapper.text()).toContain('No milestones yet')
  })

  it('shows error banner when store has error', async () => {
    // Simulate error state by having the API reject; spy on fetchGoal to swallow
    // the re-throw so the unhandled rejection doesn't leak into Vitest
    mockGet.mockRejectedValue(new Error('Failed to load goal'))
    const store = useGoalsStore()
    const spy = vi.spyOn(store, 'fetchGoal').mockImplementation(async () => {
      store.error = 'Failed to load goal'
      return undefined as never
    })
    const wrapper = mount(GoalDetailView)
    await flushPromises()
    spy.mockRestore()

    expect(wrapper.find('.error-banner').exists()).toBe(true)
    expect(wrapper.find('.error-banner').text()).toContain('Failed to load goal')
  })

  it('navigates back to goals list when back button is clicked', async () => {
    const wrapper = mount(GoalDetailView)
    await flushPromises()

    await wrapper.find('.btn-back').trigger('click')
    expect(mockPush).toHaveBeenCalledWith({ name: 'goals' })
  })

  it('navigates to edit page when Edit Goal button is clicked', async () => {
    const wrapper = mount(GoalDetailView)
    await flushPromises()

    await wrapper.find('.btn.btn-secondary').trigger('click')
    expect(mockPush).toHaveBeenCalledWith({ name: 'goal-edit', params: { id: 1 } })
  })

  it('opens delete confirmation modal when Delete Goal is clicked', async () => {
    const wrapper = mount(GoalDetailView)
    await flushPromises()

    expect(wrapper.find('.modal-overlay').exists()).toBe(false)
    await wrapper.find('.btn.btn-danger').trigger('click')
    expect(wrapper.find('.modal-overlay').exists()).toBe(true)
    expect(wrapper.text()).toContain('Delete Goal')
  })

  it('closes delete modal when Cancel is clicked', async () => {
    const wrapper = mount(GoalDetailView)
    await flushPromises()

    await wrapper.find('.btn.btn-danger').trigger('click')
    expect(wrapper.find('.modal-overlay').exists()).toBe(true)

    await wrapper.find('.modal-actions .btn-secondary').trigger('click')
    expect(wrapper.find('.modal-overlay').exists()).toBe(false)
  })

  it('deletes goal and navigates to goals list', async () => {
    const wrapper = mount(GoalDetailView)
    await flushPromises()

    await wrapper.find('.btn.btn-danger').trigger('click')
    await wrapper.find('.modal-actions .btn-danger').trigger('click')
    await flushPromises()

    expect(mockDelete).toHaveBeenCalledWith('/goals/1')
    expect(mockPush).toHaveBeenCalledWith({ name: 'goals' })
  })

  it('toggles milestone when toggle button is clicked', async () => {
    mockPatch.mockResolvedValue({
      data: { ...sampleGoal.milestones[1], completed: true, completedAt: '2025-06-01T00:00:00' },
    })
    const wrapper = mount(GoalDetailView)
    await flushPromises()

    const toggleButtons = wrapper.findAll('.milestone-toggle')
    await toggleButtons[1].trigger('click')
    await flushPromises()

    expect(mockPatch).toHaveBeenCalledWith('/goals/1/milestones/11/toggle')
  })

  it('calls updateStatus when a status button is clicked', async () => {
    mockPatch.mockResolvedValue({ data: { ...sampleGoal, status: 'COMPLETED' } })
    const wrapper = mount(GoalDetailView)
    await flushPromises()

    const completedBtn = wrapper.findAll('.btn-status').find((btn) => btn.text() === 'Completed')
    await completedBtn!.trigger('click')
    await flushPromises()

    expect(mockPatch).toHaveBeenCalledWith('/goals/1/status', { status: 'COMPLETED' })
  })

  it('shows milestone form when Add Milestone button is clicked', async () => {
    const wrapper = mount(GoalDetailView)
    await flushPromises()

    expect(wrapper.find('.milestone-form').exists()).toBe(false)
    await wrapper.find('.milestones-header .btn-primary').trigger('click')
    expect(wrapper.find('.milestone-form').exists()).toBe(true)
  })

  it('hides milestone form when Cancel is clicked inside it', async () => {
    const wrapper = mount(GoalDetailView)
    await flushPromises()

    await wrapper.find('.milestones-header .btn-primary').trigger('click')
    expect(wrapper.find('.milestone-form').exists()).toBe(true)

    // Click again to cancel (button toggles)
    await wrapper.find('.milestones-header .btn-primary').trigger('click')
    expect(wrapper.find('.milestone-form').exists()).toBe(false)
  })

  it('submits milestone form and calls addMilestone', async () => {
    const newMilestone = {
      id: 12,
      goalId: 1,
      title: 'New milestone',
      targetDate: null,
      completedAt: null,
      completed: false,
      notes: null,
      createdAt: '2025-01-03T00:00:00',
    }
    mockPost.mockResolvedValue({ data: newMilestone })

    const wrapper = mount(GoalDetailView)
    await flushPromises()

    await wrapper.find('.milestones-header .btn-primary').trigger('click')
    await wrapper.find('input[placeholder="Milestone title *"]').setValue('New milestone')
    await wrapper.find('.milestone-form').trigger('submit')
    await flushPromises()

    expect(mockPost).toHaveBeenCalledWith(
      '/goals/1/milestones',
      expect.objectContaining({ title: 'New milestone' }),
    )
  })

  it('does not submit milestone form when title is blank', async () => {
    const wrapper = mount(GoalDetailView)
    await flushPromises()

    await wrapper.find('.milestones-header .btn-primary').trigger('click')
    // leave title empty
    await wrapper.find('.milestone-form').trigger('submit')
    await flushPromises()

    expect(mockPost).not.toHaveBeenCalled()
  })

  it('renders target date when present', async () => {
    const wrapper = mount(GoalDetailView)
    await flushPromises()

    expect(wrapper.text()).toContain('Target:')
  })
})
