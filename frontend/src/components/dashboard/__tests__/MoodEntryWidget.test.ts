import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import MoodEntryWidget from '../MoodEntryWidget.vue'

vi.mock('@/services/api', () => ({
  default: {
    post: vi.fn(),
  },
}))

vi.mock('@/stores/auth', () => ({
  useAuthStore: vi.fn(() => ({ logout: vi.fn() })),
}))

vi.mock('@/router', () => ({
  default: { push: vi.fn() },
}))

vi.mock('@/composables/useErrorHandler', () => ({
  useErrorHandler: vi.fn(() => ({ addError: vi.fn() })),
}))

describe('MoodEntryWidget', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders the widget title', () => {
    const wrapper = mount(MoodEntryWidget)
    expect(wrapper.find('.widget-title').text()).toBe('How are you feeling?')
  })

  it('renders 10 emoji buttons', () => {
    const wrapper = mount(MoodEntryWidget)
    const buttons = wrapper.findAll('.emoji-btn')
    expect(buttons).toHaveLength(10)
  })

  it('log mood button is disabled when no mood is selected', () => {
    const wrapper = mount(MoodEntryWidget)
    const submitBtn = wrapper.find('.btn-primary')
    expect(submitBtn.attributes('disabled')).toBeDefined()
  })

  it('selects a mood when an emoji button is clicked', async () => {
    const wrapper = mount(MoodEntryWidget)
    const buttons = wrapper.findAll('.emoji-btn')
    await buttons[4].trigger('click')
    expect(buttons[4].classes()).toContain('selected')
  })

  it('shows textarea after mood is selected', async () => {
    const wrapper = mount(MoodEntryWidget)
    expect(wrapper.find('.notes-input').exists()).toBe(false)
    const buttons = wrapper.findAll('.emoji-btn')
    await buttons[0].trigger('click')
    expect(wrapper.find('.notes-input').exists()).toBe(true)
  })

  it('enables log mood button after mood is selected', async () => {
    const wrapper = mount(MoodEntryWidget)
    const buttons = wrapper.findAll('.emoji-btn')
    await buttons[2].trigger('click')
    const submitBtn = wrapper.find('.btn-primary')
    expect(submitBtn.attributes('disabled')).toBeUndefined()
  })

  it('shows submitted state after successful submission', async () => {
    const api = await import('@/services/api')
    vi.mocked(api.default.post).mockResolvedValueOnce({})

    const wrapper = mount(MoodEntryWidget)
    const buttons = wrapper.findAll('.emoji-btn')
    await buttons[6].trigger('click')
    await wrapper.find('.btn-primary').trigger('click')
    await wrapper.vm.$nextTick()
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.submitted-state').exists()).toBe(true)
    expect(wrapper.find('.submitted-text').text()).toBe('Mood logged! Keep it up.')
  })

  it('shows error message on submission failure', async () => {
    const api = await import('@/services/api')
    vi.mocked(api.default.post).mockRejectedValueOnce(new Error('Network error'))

    const wrapper = mount(MoodEntryWidget)
    const buttons = wrapper.findAll('.emoji-btn')
    await buttons[0].trigger('click')
    await wrapper.find('.btn-primary').trigger('click')
    await wrapper.vm.$nextTick()
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.error-msg').exists()).toBe(true)
    expect(wrapper.find('.error-msg').text()).toBe('Failed to save mood. Please try again.')
  })
})
